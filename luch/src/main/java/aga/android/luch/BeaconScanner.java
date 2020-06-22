package aga.android.luch;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import aga.android.luch.rssi.RssiFilter;
import aga.android.luch.rssi.RunningAverageRssiFilter;
import aga.android.luch.parsers.BeaconParserFactory;
import aga.android.luch.parsers.IBeaconParser;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

public final class BeaconScanner implements IScanner {

    private static final long BEACON_EVICTION_PERIODICITY_MILLIS = 1_000;

    @NonNull
    private IBleDevice bleDevice;

    @NonNull
    private final BeaconScanCallback scanCallback = new BeaconScanCallback();

    @Nullable
    private IBeaconBatchListener beaconBatchListener = null;

    @Nullable
    private IBeaconListener beaconListener = null;

    @Nullable
    private final Ranger ranger;

    @NonNull
    private final Handler uiHandler;

    @NonNull
    private final ScanExecutorProvider scheduledExecutorProvider;

    @NonNull
    private ScheduledExecutorService scheduledExecutor;

    @NonNull
    private final IBeaconParser beaconParser;

    @NonNull
    private final ITimeProvider timeProvider;

    @NonNull
    private final Map<Integer, Beacon> nearbyBeacons = new ConcurrentHashMap<>();

    @NonNull
    private final ScanDuration scanDuration;

    private long beaconExpirationDurationMillis;

    private BeaconScanner(@NonNull IBleDevice bleDevice,
                          @NonNull ScanExecutorProvider scheduledExecutorProvider,
                          @NonNull IBeaconParser beaconParser,
                          @NonNull ITimeProvider timeProvider,
                          @NonNull ScanDuration scanDuration,
                          @Nullable Ranger ranger) {
        this.bleDevice = bleDevice;
        this.beaconParser = beaconParser;
        this.timeProvider = timeProvider;
        this.scanDuration = scanDuration;
        this.uiHandler = new Handler();
        this.scheduledExecutorProvider = scheduledExecutorProvider;
        this.scheduledExecutor = scheduledExecutorProvider.provide();
        this.ranger = ranger;
    }

    @Override
    public void start() {
        scheduledExecutor.schedule(
            scanResumeJob,
            0L,
            TimeUnit.MILLISECONDS
        );

        scheduledExecutor.scheduleAtFixedRate(
            beaconsEvictionJob,
            scanDuration.scanDurationMillis,
            BEACON_EVICTION_PERIODICITY_MILLIS,
            TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void stop() {
       scheduledExecutor.schedule(
           scanStopJob,
           0L,
           TimeUnit.MILLISECONDS
       );
    }

    @Nullable
    @Override
    public Ranger getRanger() {
        return ranger;
    }

    private void evictOutdatedBeacons() {
        final Iterator<Map.Entry<Integer, Beacon>> iterator = nearbyBeacons.entrySet().iterator();

        for (; iterator.hasNext();) {
            final Beacon inMemoryBeacon = iterator.next().getValue();
            final long lastSeenAt = requireNonNull(inMemoryBeacon).getLastSeenAtSystemClock();

            if (timeProvider.elapsedRealTimeTimeMillis() - lastSeenAt
                    >= beaconExpirationDurationMillis) {

                iterator.remove();

                if (ranger != null) {
                    ranger.removeReadings(inMemoryBeacon);
                }

                if (beaconListener != null) {
                    uiHandler.post(
                        new Runnable() {
                            @Override
                            public void run() {
                                beaconListener.onBeaconExited(inMemoryBeacon);
                            }
                        }
                    );
                }
            }
        }

        uiHandler.post(deliverBeaconBatchJob);
    }

    public static final class Builder {

        private IBeaconBatchListener beaconBatchListener = null;

        private IBeaconListener beaconListener = null;

        private List<Region> regions = Collections.emptyList();

        private long beaconExpirationDurationSeconds = 5;

        private IBleDevice bleDevice = null;

        private ScanDuration scanDuration = ScanDuration.UNIFORM;

        private IBeaconParser beaconParser = BeaconParserFactory.ALTBEACON_PARSER;

        private ITimeProvider timeProvider = new ITimeProvider.SystemTimeProvider();

        private RssiFilter.Builder rssiFilterBuilder = null;

        private final Context context;

        private ScanExecutorProvider scanTasksExecutorProvider = new ScanExecutorProvider() {
            @Override
            public ScheduledExecutorService provide() {
                return newSingleThreadScheduledExecutor();
            }
        };

        public Builder(@NonNull Context context) {
            this.context = context;
        }

        /**
         * Sets the {@link IBeaconBatchListener} implementation which will be notified every time
         * the change in the nearby beacons occurs.
         */
        public Builder setBeaconBatchListener(IBeaconBatchListener listener) {
            this.beaconBatchListener = listener;
            return this;
        }

        /**
         * Sets the {@link IBeaconListener} implementation which will be notified every time we
         * detect a new beacon or a previously detected beacon gets lost
         */
        public Builder setBeaconListener(IBeaconListener listener) {
            this.beaconListener = listener;
            return this;
        }

        /**
         * Sets the list of {@link Region}s to look for. The empty list means we're
         * interested in all beacons matching the current beacon layout around us, no matter what
         * data do they have.
         *
         * Default value: empty list.
         */
        public Builder setRegions(@NonNull List<Region> regions) {
            this.regions = regions;
            return this;
        }

        /**
         * Sets how long does it take for a beacon to be considered expired after its last
         * detection.
         *
         * Default value: 5 seconds
         */
        public Builder setBeaconExpirationDuration(@IntRange(from = 1) long seconds) {
            this.beaconExpirationDurationSeconds = seconds;
            return this;
        }

        /**
         * Sets the {@link ScanDuration} object which determines two things:
         * 1. How long do we scan for beacons
         * 2. How long do we wait between to subsequent scans
         *
         * Default value: 6 seconds for both scan and rest durations.
         */
        public Builder setScanDuration(@NonNull ScanDuration scanDuration) {
            this.scanDuration = scanDuration;
            return this;
        }

        /**
         * Changes the {@link IBeaconParser} implementation used by the system to parse
         * the detected beacons.
         *
         *  By default the library parses the detected beacons in accordance with the AltBeacon
         *  specification.
         */
        public Builder setBeaconParser(@NonNull IBeaconParser beaconParser) {
            this.beaconParser = beaconParser;
            return this;
        }

        /**
         * Enables distance calculation for detected beacons. Use {@link BeaconScanner#getRanger()}
         * to get access to the distance calculator
         * @return this object
         */
        public Builder setRangingEnabled() {
            return setRangingEnabled(new RunningAverageRssiFilter.Builder());
        }

        /**
         * Enables distance calculation for detected beacons. Use {@link BeaconScanner#getRanger()}
         * to get access to the distance calculator
         * @param rssiFilterBuilder RSSI filter to be used for RSSI averaging, default filter is
         *                          {@link RunningAverageRssiFilter}
         * @return this object
         */
        public Builder setRangingEnabled(@NonNull RssiFilter.Builder rssiFilterBuilder) {
            this.rssiFilterBuilder = rssiFilterBuilder;
            return this;
        }

        @VisibleForTesting
        Builder setTimeProvider(@NonNull ITimeProvider timeProvider) {
            this.timeProvider = timeProvider;
            return this;
        }

        @VisibleForTesting
        Builder setBleDevice(@NonNull IBleDevice bleDevice) {
            this.bleDevice = bleDevice;
            return this;
        }

        @VisibleForTesting
        Builder setScanTasksExecutor(@NonNull ScanExecutorProvider scanTasksExecutorProvider) {
            this.scanTasksExecutorProvider = scanTasksExecutorProvider;
            return this;
        }

        public BeaconScanner build() {
            if (beaconExpirationDurationSeconds < 1) {
                throw new AssertionError("Beacon validity duration has to be positive; "
                        + "actual value is: " + beaconExpirationDurationSeconds + " seconds");
            }

            if (bleDevice == null) {
                final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                if (bluetoothAdapter == null) {
                    throw new IllegalStateException("Bluetooth is not accessible on that device");
                }

                final ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    scanSettingsBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
                }

                bleDevice = new SystemBleDevice(
                    context,
                    bluetoothAdapter,
                    scanSettingsBuilder.build(),
                    beaconParser.asScanFilters(regions)
                );
            }

            Ranger ranger = null;

            if (rssiFilterBuilder != null) {
                ranger = new Ranger(rssiFilterBuilder);
            }

            final BeaconScanner scanner = new BeaconScanner(
                bleDevice,
                scanTasksExecutorProvider,
                beaconParser,
                timeProvider,
                scanDuration,
                ranger
            );

            scanner.beaconExpirationDurationMillis = TimeUnit.SECONDS.toMillis(
                beaconExpirationDurationSeconds
            );
            scanner.beaconBatchListener = beaconBatchListener;
            scanner.beaconListener = beaconListener;

            return scanner;
        }
    }

    private final Runnable deliverBeaconBatchJob = new Runnable() {
        @Override
        public void run() {
            if (beaconBatchListener != null) {
                beaconBatchListener.onBeaconsDetected(nearbyBeacons.values());
            }
        }
    };

    private final Runnable scanResumeJob = new Runnable() {
        @Override
        public void run() {
            BeaconLogger.d("BLE scans resumed... ");

            bleDevice.startScans(scanCallback);

            scheduledExecutor.schedule(
                scanPauseJob,
                scanDuration.scanDurationMillis,
                TimeUnit.MILLISECONDS
            );
        }
    };

    private final Runnable scanPauseJob = new Runnable() {
        @Override
        public void run() {
            BeaconLogger.d("BLE scans paused...");

            bleDevice.stopScans(scanCallback);

            scheduledExecutor.schedule(
                scanResumeJob,
                scanDuration.restDurationMillis,
                TimeUnit.MILLISECONDS
            );
        }
    };

    private final Runnable scanStopJob = new Runnable() {
        @Override
        public void run() {
            BeaconLogger.d("BLE scans stopped");

            bleDevice.stopScans(scanCallback);

            scheduledExecutor.shutdownNow();

            try {
                scheduledExecutor = scheduledExecutorProvider.provide();
            } catch (Exception e) {
                BeaconLogger.e("Can not create a new schedulerExecutor due to " + e + "; scans "
                    + "most likely will not be resumed");
            }
        }
    };

    private final Runnable beaconsEvictionJob = new Runnable() {
        @Override
        public void run() {
            BeaconLogger.d("Beacons eviction started");

            evictOutdatedBeacons();

            BeaconLogger.d("Beacons eviction completed");
        }
    };

    private final class BeaconHandlerJob implements Runnable {

        private final ScanResult scanResult;

        private BeaconHandlerJob(ScanResult scanResult) {
            this.scanResult = scanResult;
        }

        @Override
        public void run() {
            final ScanRecord scanRecord = scanResult.getScanRecord();

            if (scanRecord == null) {
                return;
            }

            final byte[] rawBytes = scanRecord.getBytes();

            final Beacon inMemoryBeacon = nearbyBeacons.get(Arrays.hashCode(rawBytes));

            final long elapsedRealTimeTimeMillis = timeProvider.elapsedRealTimeTimeMillis();

            if (inMemoryBeacon != null) {
                handleInMemoryBeacon(inMemoryBeacon, elapsedRealTimeTimeMillis);
            } else {
                final Beacon parsedBeacon = beaconParser.parse(scanResult);

                if (parsedBeacon == null) {
                    return;
                }

                handleScannedBeacon(rawBytes, elapsedRealTimeTimeMillis, parsedBeacon);
            }

            uiHandler.post(deliverBeaconBatchJob);
        }

        private void handleScannedBeacon(byte[] rawBytes,
                                         long elapsedRealTimeTimeMillis,
                                         final Beacon parsedBeacon) {

            updateBeaconTransientValues(parsedBeacon, elapsedRealTimeTimeMillis);

            nearbyBeacons.put(Arrays.hashCode(rawBytes), parsedBeacon);

            if (beaconListener != null) {
                uiHandler.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            beaconListener.onBeaconEntered(parsedBeacon);
                        }
                    }
                );
            }
        }

        private void handleInMemoryBeacon(final Beacon inMemoryBeacon,
                                          long elapsedRealTimeTimeMillis) {

            updateBeaconTransientValues(inMemoryBeacon, elapsedRealTimeTimeMillis);

            if (beaconListener != null) {
                uiHandler.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            beaconListener.onBeaconUpdated(inMemoryBeacon);
                        }
                    }
                );
            }
        }

        private void updateBeaconTransientValues(Beacon inMemoryBeacon,
                                                 long elapsedRealTimeTimeMillis) {
            inMemoryBeacon.setLastSeenAtSystemClock(elapsedRealTimeTimeMillis);
            inMemoryBeacon.setRssi((byte) scanResult.getRssi());

            if (ranger != null) {
                ranger.addReading(inMemoryBeacon, (byte) scanResult.getRssi());
            }
        }
    }

    private final class BeaconScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            scheduledExecutor.submit(new BeaconHandlerJob(result));
        }

        @Override
        public void onScanFailed(int errorCode) {
            BeaconLogger.d("On scan failed: " + errorCode);
        }
    }
}

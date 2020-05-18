package aga.android.luch;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import aga.android.luch.parsers.BeaconParserFactory;
import aga.android.luch.parsers.IBeaconParser;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import static android.os.SystemClock.elapsedRealtime;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

public class BeaconScanner implements IScanner {

    private static final long BEACON_EVICTION_PERIODICITY_MILLIS = 1_000;

    @NonNull
    private IBleDevice bleDevice;

    @NonNull
    private final BeaconScanCallback scanCallback = new BeaconScanCallback();

    @Nullable
    private IBeaconListener beaconListener = null;

    @NonNull
    private final Handler uiHandler;

    @NonNull
    private final ScanExecutorProvider scheduledExecutorProvider;

    @NonNull
    private ScheduledExecutorService scheduledExecutor;

    @NonNull
    private final IBeaconParser beaconParser;

    @NonNull
    private final Map<Beacon, Long> nearbyBeacons = new ConcurrentHashMap<>();

    @NonNull
    private final ScanDuration scanDuration;

    private long beaconExpirationDurationMillis;

    private BeaconScanner(@NonNull IBleDevice bleDevice,
                          @NonNull ScanExecutorProvider scheduledExecutorProvider,
                          @NonNull IBeaconParser beaconParser,
                          @NonNull ScanDuration scanDuration) {
        this.bleDevice = bleDevice;
        this.beaconParser = beaconParser;
        this.scanDuration = scanDuration;
        this.uiHandler = new Handler();
        this.scheduledExecutorProvider = scheduledExecutorProvider;
        this.scheduledExecutor = scheduledExecutorProvider.provide();
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

    private void evictOutdatedBeacons() {
        final Iterator<Beacon> iterator = nearbyBeacons.keySet().iterator();

        for (; iterator.hasNext();) {
            final Beacon inMemoryBeacon = iterator.next();
            final Long lastAppearanceMillis = nearbyBeacons.get(inMemoryBeacon);

            if (lastAppearanceMillis != null
                    && elapsedRealtime() - lastAppearanceMillis > beaconExpirationDurationMillis) {

                iterator.remove();
            }
        }

        uiHandler.post(deliverBeaconsJob);
    }

    public static final class Builder {

        private IBeaconListener listener = null;

        private List<RegionDefinition> definitions = Collections.emptyList();

        private long beaconExpirationDurationSeconds = 5;

        private IBleDevice bleDevice = null;

        private ScanDuration scanDuration = ScanDuration.UNIFORM;

        private IBeaconParser beaconParser = BeaconParserFactory.ALTBEACON_PARSER;

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
         * Sets the {@link IBeaconListener} implementation which will be notified every time the
         * change in the nearby beacons occurs.
         */
        public Builder setBeaconListener(IBeaconListener listener) {
            this.listener = listener;
            return this;
        }

        /**
         * Sets the list of {@link RegionDefinition}s to look for. The empty list means we're
         * interested in all beacons matching the current beacon layout around us, no matter what
         * data do they have.
         *
         * Default value: empty list.
         */
        public Builder setRegionDefinitions(List<RegionDefinition> definitions) {
            this.definitions = definitions;
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

        @VisibleForTesting
        Builder setBleDevice(IBleDevice bleDevice) {
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
                    beaconParser.asScanFilters(definitions)
                );
            }

            final BeaconScanner scanner = new BeaconScanner(
                bleDevice,
                scanTasksExecutorProvider,
                beaconParser,
                scanDuration
            );

            scanner.beaconExpirationDurationMillis = TimeUnit.SECONDS.toMillis(
                beaconExpirationDurationSeconds
            );
            scanner.beaconListener = listener;

            return scanner;
        }
    }

    private final Runnable deliverBeaconsJob = new Runnable() {
        @Override
        public void run() {
            if (beaconListener != null) {
                beaconListener.onNearbyBeaconsDetected(nearbyBeacons.keySet());
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
            final Beacon beacon = beaconParser.parse(scanResult);

            if (beacon == null) {
                return;
            }

            nearbyBeacons.put(beacon, elapsedRealtime());

            uiHandler.post(deliverBeaconsJob);
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

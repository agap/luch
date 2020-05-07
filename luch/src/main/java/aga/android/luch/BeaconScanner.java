package aga.android.luch;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private final Map<Beacon, Long> nearbyBeacons = new ConcurrentHashMap<>();

    @NonNull
    private final ScanDuration scanDuration;

    private long beaconExpirationDurationMillis;

    private BeaconScanner(@NonNull IBleDevice bleDevice,
                          @NonNull ScanExecutorProvider scheduledExecutorProvider,
                          @NonNull ScanDuration scanDuration) {
        this.bleDevice = bleDevice;
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

        private ScanExecutorProvider scanTasksExecutorProvider = new ScanExecutorProvider() {
            @Override
            public ScheduledExecutorService provide() {
                return newSingleThreadScheduledExecutor();
            }
        };

        public Builder setBeaconListener(IBeaconListener listener) {
            this.listener = listener;
            return this;
        }

        /**
         * Sets the list of {@link RegionDefinition}s to look for. The empty list means we're
         * interested in all iBeacons around us, no matter what proximity uuid/major/minor do
         * they have.
         *
         * Default value: empty list.
         */
        public Builder setRegionDefinitions(List<RegionDefinition> definitions) {
            this.definitions = definitions;
            return this;
        }

        public Builder setBeaconExpirationDuration(@IntRange(from = 1) long seconds) {
            this.beaconExpirationDurationSeconds = seconds;
            return this;
        }

        public Builder setScanDuration(@NonNull ScanDuration scanDuration) {
            this.scanDuration = scanDuration;
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

            final BeaconScanner scanner;

            if (bleDevice != null) {
                scanner = new BeaconScanner(bleDevice, scanTasksExecutorProvider, scanDuration);
            } else {
                final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                if (bluetoothAdapter == null) {
                    throw new IllegalStateException("Bluetooth is not accessible on that device");
                }

                final ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    scanSettingsBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
                }

                final IBleDevice bleDevice = new SystemBleDevice(
                    bluetoothAdapter,
                    scanSettingsBuilder.build(),
                    RegionDefinitionMapper.asScanFilters(definitions)
                );

                scanner = new BeaconScanner(bleDevice, scanTasksExecutorProvider, scanDuration);
            }

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

    private final class BeaconScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            final Beacon beacon = RegionDefinitionMapper.asBeacon(result);

            if (beacon == null) {
                return;
            }

            nearbyBeacons.put(beacon, elapsedRealtime());

            uiHandler.post(deliverBeaconsJob);
        }

        @Override
        public void onScanFailed(int errorCode) {
            BeaconLogger.d("On scan failed: " + errorCode);
        }
    }
}

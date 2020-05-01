package aga.android.luch;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import static android.os.SystemClock.elapsedRealtime;

public class BeaconScanner implements IScanner {

    private static final int MESSAGE_RESUME_SCANS = 1;
    private static final int MESSAGE_PAUSE_SCANS = 2;
    private static final int MESSAGE_STOP_SCANS = 3;
    private static final int MESSAGE_EVICT_OUTDATED_BEACONS = 4;

    @NonNull
    private IBleDevice bleDevice;

    @NonNull
    private final BeaconScanCallback scanCallback = new BeaconScanCallback();

    @Nullable
    private IBeaconListener beaconListener = null;

    @NonNull
    private final Handler handler;

    @NonNull
    private final Map<Beacon, Long> nearbyBeacons = new HashMap<>();

    @NonNull
    private final ScanDuration scanDuration;

    private long beaconEvictionPeriodicityMillis;

    private BeaconScanner(@NonNull IBleDevice bleDevice,
                          @NonNull ScanDuration scanDuration) {
        this.bleDevice = bleDevice;
        this.scanDuration = scanDuration;
        this.handler = new Handler(new HandlerCallback());
    }

    @Override
    public void start() {
        handler.sendMessage(
            handler.obtainMessage(MESSAGE_RESUME_SCANS)
        );

        handler.sendMessageDelayed(
            handler.obtainMessage(MESSAGE_EVICT_OUTDATED_BEACONS),
            beaconEvictionPeriodicityMillis
        );
    }

    @Override
    public void stop() {
       handler.sendMessageAtFrontOfQueue(
           handler.obtainMessage(MESSAGE_STOP_SCANS)
       );
    }

    private void evictOutdatedBeacons() {
        final Iterator<Beacon> iterator = nearbyBeacons.keySet().iterator();

        for (; iterator.hasNext(); ) {
            final Beacon inMemoryBeacon = iterator.next();
            final Long lastAppearanceMillis = nearbyBeacons.get(inMemoryBeacon);

            if (lastAppearanceMillis != null
                    && elapsedRealtime() - lastAppearanceMillis > beaconEvictionPeriodicityMillis) {

                iterator.remove();
            }
        }

        if (beaconListener != null) {
            beaconListener.onNearbyBeaconsDetected(nearbyBeacons.keySet());
        }
    }

    public static final class Builder {

        private IBeaconListener listener = null;

        private List<RegionDefinition> definitions = Collections.emptyList();

        private long beaconEvictionPeriodicityMillis = TimeUnit.SECONDS.toMillis(5);

        private IBleDevice bleDevice = null;

        private ScanDuration scanDuration = ScanDuration.UNIFORM;

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

        public Builder setBeaconEvictionTime(long millis) {
            this.beaconEvictionPeriodicityMillis = millis;
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

        public BeaconScanner build() {
            final BeaconScanner scanner;

            if (bleDevice != null) {
                scanner = new BeaconScanner(bleDevice, scanDuration);
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

                scanner = new BeaconScanner(bleDevice, scanDuration);
            }

            scanner.beaconEvictionPeriodicityMillis = beaconEvictionPeriodicityMillis;
            scanner.beaconListener = listener;

            return scanner;
        }
    }

    private final class HandlerCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MESSAGE_RESUME_SCANS:
                    BeaconLogger.d("BLE scans resumed...");

                    bleDevice.startScans(scanCallback);

                    handler.sendMessageDelayed(
                        handler.obtainMessage(MESSAGE_PAUSE_SCANS),
                        scanDuration.scanDurationMillis
                    );

                    break;

                case MESSAGE_PAUSE_SCANS:
                    BeaconLogger.d("BLE scans paused...");

                    bleDevice.stopScans(scanCallback);

                    handler.sendMessageDelayed(
                        handler.obtainMessage(MESSAGE_RESUME_SCANS),
                        scanDuration.restDurationMillis
                    );

                    break;

                case MESSAGE_STOP_SCANS:
                    BeaconLogger.d("BLE scans stopped");

                    bleDevice.stopScans(scanCallback);

                    handler.removeCallbacksAndMessages(null);

                    break;

                case MESSAGE_EVICT_OUTDATED_BEACONS:
                    BeaconLogger.d("Beacons eviction started");

                    evictOutdatedBeacons();

                    BeaconLogger.d("Beacons eviction completed");

                    handler.sendMessageDelayed(
                        handler.obtainMessage(MESSAGE_EVICT_OUTDATED_BEACONS),
                        beaconEvictionPeriodicityMillis
                    );

                    break;
            }

            return true;
        }
    }

    private final class BeaconScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            final Beacon beacon = RegionDefinitionMapper.asBeacon(result);

            if (beacon == null) {
                return;
            }

            nearbyBeacons.put(beacon, elapsedRealtime());

            if (beaconListener != null) {
                beaconListener.onNearbyBeaconsDetected(nearbyBeacons.keySet());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            BeaconLogger.d("On scan failed: " + errorCode);
        }
    }
}

package aga.android.ibeacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
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

import static android.os.SystemClock.elapsedRealtime;

// todo make package private
public class BeaconScanner implements IScanner, Handler.Callback {

    private static final int MESSAGE_RESUME_SCANS = 1;
    private static final int MESSAGE_PAUSE_SCANS = 2;
    private static final int MESSAGE_STOP_SCANS = 3;
    private static final int MESSAGE_EVICT_OUTDATED_BEACONS = 4;
    
    private final ScanSettings scanSettings;

    @NonNull
    private final BluetoothAdapter bluetoothAdapter;

    @NonNull
    private final BeaconScanCallback scanCallback = new BeaconScanCallback();

    @Nullable
    private IBeaconListener beaconListener = null;

    @NonNull
    private final Handler handler;

    @NonNull
    private final Map<Beacon, Long> nearbyBeacons = new HashMap<>();

    private long beaconEvictionPeriodicityMillis;

    private long scanDurationMillis;
    private long restDurationMillis;

    @NonNull
    private List<ScanFilter> scanFilters = Collections.emptyList();

    private BeaconScanner() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            throw new IllegalStateException("Bluetooth is not accessible on that device");
        }

        final ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scanSettingsBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
        }

        scanSettings = scanSettingsBuilder.build();

        handler = new Handler(this);
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

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case MESSAGE_RESUME_SCANS:
                BeaconLogger.d("BLE scans resumed...");

                startScans();

                handler.sendMessageDelayed(
                    handler.obtainMessage(MESSAGE_PAUSE_SCANS),
                    scanDurationMillis
                );

                break;

            case MESSAGE_PAUSE_SCANS:
                BeaconLogger.d("BLE scans paused...");

                stopScans();

                handler.sendMessageDelayed(
                    handler.obtainMessage(MESSAGE_RESUME_SCANS),
                    restDurationMillis
                );

                break;

            case MESSAGE_STOP_SCANS:
                BeaconLogger.d("BLE scans stopped");

                stopScans();

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

    private void startScans() {
        bluetoothAdapter
            .getBluetoothLeScanner()
            .startScan(
                scanFilters,
                scanSettings,
                scanCallback
            );
    }

    private void stopScans() {
        bluetoothAdapter
            .getBluetoothLeScanner()
            .stopScan(scanCallback);
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

        private List<RegionDefinition> definitions;

        private long beaconEvictionPeriodicityMillis = TimeUnit.SECONDS.toMillis(5);

        private long scanDurationMillis = 100;
        private long restDurationMillis = 1_000;

        public Builder setBeaconListener(IBeaconListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder setRegionDefinitions(List<RegionDefinition> definitions) {
            this.definitions = definitions;
            return this;
        }

        public Builder setBeaconEvictionTime(long millis) {
            this.beaconEvictionPeriodicityMillis = millis;
            return this;
        }

        public Builder setScanDuration(long millis) {
            this.scanDurationMillis = millis;
            return this;
        }

        public Builder setRestDuration(long millis) {
            this.restDurationMillis = millis;
            return this;
        }

        public BeaconScanner build() {
            final BeaconScanner scanner = new BeaconScanner();

            scanner.beaconEvictionPeriodicityMillis = beaconEvictionPeriodicityMillis;
            scanner.scanDurationMillis = scanDurationMillis;
            scanner.restDurationMillis = restDurationMillis;
            scanner.beaconListener = listener;
            scanner.scanFilters = RegionDefinitionMapper.asScanFilters(definitions);

            return scanner;
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

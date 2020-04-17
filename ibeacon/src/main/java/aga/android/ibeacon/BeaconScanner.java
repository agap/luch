package aga.android.ibeacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
import android.util.Log;

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
public class BeaconScanner implements IScanner {

    private static final String TAG = "BeaconScanner";

    private static final ScanSettings SCAN_SETTINGS = new ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .build();

    @NonNull
    private final BluetoothAdapter bluetoothAdapter;

    @NonNull
    private final BeaconScanCallback scanCallback = new BeaconScanCallback();

    @Nullable
    private IBeaconListener beaconListener = null;

    @NonNull
    private final Handler handler = new Handler();

    @NonNull
    private final Map<Beacon, Long> nearbyBeacons = new HashMap<>();

    private long beaconEvictionTimeMillis;

    @NonNull
    private List<ScanFilter> scanFilters = Collections.emptyList();

    private BeaconScanner() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            throw new IllegalStateException("Bluetooth is not accessible on that device");
        }
    }

    @Override
    public void start() {
        bluetoothAdapter
            .getBluetoothLeScanner()
            .startScan(
                scanFilters,
                SCAN_SETTINGS,
                scanCallback
            );

        handler.postDelayed(beaconEvictionTask, beaconEvictionTimeMillis);
    }

    @Override
    public void stop() {
        bluetoothAdapter
            .getBluetoothLeScanner()
            .stopScan(scanCallback);

        handler.removeCallbacksAndMessages(null);
    }

    public static final class Builder {

        private IBeaconListener listener = null;

        private List<RegionDefinition> definitions;

        private long beaconEvictionTimeMillis = TimeUnit.SECONDS.toMillis(5);

        public Builder setBeaconListener(IBeaconListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder setRegionDefinitions(List<RegionDefinition> definitions) {
            this.definitions = definitions;
            return this;
        }

        public Builder setBeaconEvictionTime(long millis) {
            this.beaconEvictionTimeMillis = millis;
            return this;
        }

        public BeaconScanner build() {
            final BeaconScanner scanner = new BeaconScanner();

            scanner.beaconEvictionTimeMillis = beaconEvictionTimeMillis;
            scanner.beaconListener = listener;
            scanner.scanFilters = RegionDefinitionMapper.asScanFilters(definitions);

            return scanner;
        }
    }

    private final Runnable beaconEvictionTask = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Running the beacon eviction task");

            final Iterator<Beacon> iterator = nearbyBeacons.keySet().iterator();

            for (; iterator.hasNext(); ) {
                final Beacon inMemoryBeacon = iterator.next();
                final Long lastAppearanceMillis = nearbyBeacons.get(inMemoryBeacon);

                Log.d(TAG, "Elapsed real time is: " + elapsedRealtime() +
                        "; last appearance time is: " + lastAppearanceMillis +
                        "; eviction period is: " + beaconEvictionTimeMillis);

                if (lastAppearanceMillis != null
                        && elapsedRealtime() - lastAppearanceMillis > beaconEvictionTimeMillis) {

                    iterator.remove();
                }
            }

            if (beaconListener != null) {
                beaconListener.onNearbyBeaconsDetected(nearbyBeacons.keySet());
            }

            handler.postDelayed(this, beaconEvictionTimeMillis);
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

            if (beaconListener != null) {
                beaconListener.onNearbyBeaconsDetected(nearbyBeacons.keySet());
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d(TAG, "On batch scan results: " + results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "On scan failed: " + errorCode);
        }
    }
}

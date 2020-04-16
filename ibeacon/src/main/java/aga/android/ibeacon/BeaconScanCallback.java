package aga.android.ibeacon;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;

import static android.os.SystemClock.elapsedRealtime;

class BeaconScanCallback extends ScanCallback {

    private static final String TAG = "BeaconScanCallback";

    // todo make it possible to change this value
    private final long beaconEvictionTimeMillis = TimeUnit.SECONDS.toMillis(5);

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

    @Nullable
    private IBeaconListener beaconListener = null;

    private final Handler handler = new Handler();

    private final Map<Beacon, Long> nearbyBeacons = new HashMap<>();

    public BeaconScanCallback() {
        handler.postDelayed(beaconEvictionTask, beaconEvictionTimeMillis);
    }

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

    void setListener(IBeaconListener listener) {
        beaconListener = listener;
    }

    void stop() {
        handler.removeCallbacksAndMessages(null);
    }
}

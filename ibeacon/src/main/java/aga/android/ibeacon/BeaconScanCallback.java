package aga.android.ibeacon;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import java.util.List;

class BeaconScanCallback extends ScanCallback {

    private static final String TAG = "BeaconScanCallback";

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        Log.d(TAG, "Scan result: " + callbackType + "; result: " + result);
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

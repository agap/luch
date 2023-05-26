package aga.android.luch;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;

import java.util.List;

class SystemBleDevice implements IBleDevice {

    private final BluetoothAdapter bluetoothAdapter;

    private final ScanSettings scanSettings;

    private final List<ScanFilter> scanFilters;

    private final Context context;

    SystemBleDevice(Context context,
                    BluetoothAdapter bluetoothAdapter,
                    ScanSettings scanSettings,
                    List<ScanFilter> scanFilters) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.scanSettings = scanSettings;
        this.scanFilters = scanFilters;
        this.context = context.getApplicationContext();
    }

    @Override
    public void startScans(@NonNull ScanCallback scanCallback) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            BeaconLogger.e(
                "BLE is missing on that device; BLE scans won't be started"
            );

            return;
        }

        try {
            if (!bluetoothAdapter.isEnabled()) {
                BeaconLogger.e(
                        "BluetoothAdapter is not enabled, scans will not be started (check if "
                                + "Bluetooth is turned on)"
                );
                return;
            }

            final BluetoothLeScanner bleScanner = bluetoothAdapter.getBluetoothLeScanner();

            if (bleScanner == null) {
                BeaconLogger.e(
                        "BluetoothLeScanner is missing, scans will not be started "
                                + "(check if Bluetooth is turned on)"
                );

                return;
            }

            bleScanner
                .startScan(
                    scanFilters,
                    scanSettings,
                    scanCallback
                );
        } catch (SecurityException e) {
            BeaconLogger.e(
                "Can't start the BLE scans since it results in SecurityException (check if the "
                    + "is running in the Samsung's Knox container or something similar)"
            );
        }
    }

    @Override
    public void stopScans(@NonNull ScanCallback scanCallback) {
        try {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP
                    && !bluetoothAdapter.isEnabled()) {
                BeaconLogger.e(
                    "Can't stop the BLE scans since BluetoothAdapter is not enabled, most likely "
                        + "the scans weren't started either (check if Bluetooth is turned on)"
                );
                return;
            }

            final BluetoothLeScanner bleScanner = bluetoothAdapter.getBluetoothLeScanner();

            if (bleScanner == null) {
                BeaconLogger.e(
                    "Can't stop the BLE scans since there is no BluetoothLeScanner available, "
                        + "most likely the scans weren't started either (check if Bluetooth is "
                        + "turned on)"
                );

                return;
            }

            bleScanner.stopScan(scanCallback);
        } catch (SecurityException e) {
            BeaconLogger.e(
                "Can't stop the BLE scans since it results in SecurityException (check if the "
                        + "is running in the Samsung's Knox container or something similar)"
            );
        }
    }
}

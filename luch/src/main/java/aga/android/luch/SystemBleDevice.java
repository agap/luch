package aga.android.luch;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;

import java.util.List;

import androidx.annotation.NonNull;

class SystemBleDevice implements IBleDevice {

    private final BluetoothAdapter bluetoothAdapter;

    private final ScanSettings scanSettings;

    private final List<ScanFilter> scanFilters;

    SystemBleDevice(BluetoothAdapter bluetoothAdapter,
                    ScanSettings scanSettings,
                    List<ScanFilter> scanFilters) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.scanSettings = scanSettings;
        this.scanFilters = scanFilters;
    }

    @Override
    public void startScans(@NonNull ScanCallback scanCallback) {
        final BluetoothLeScanner bleScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (bleScanner == null) {
            BeaconLogger.e(
                "BluetoothLeScanner is missing, scans will not be started " +
                    "(check if Bluetooth is turned on)"
            );

            return;
        }

        bleScanner
            .startScan(
                scanFilters,
                scanSettings,
                scanCallback
            );
    }

    @Override
    public void stopScans(@NonNull ScanCallback scanCallback) {
        final BluetoothLeScanner bleScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (bleScanner == null) {
            BeaconLogger.e(
                "Can't stop the BLE scans since there is no BluetoothLeScanner available, " +
                    "most likely the scans weren't started either (check if Bluetooth is turned on)"
            );

            return;
        }

        bleScanner.stopScan(scanCallback);
    }
}

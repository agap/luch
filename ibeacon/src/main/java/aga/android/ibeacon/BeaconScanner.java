package aga.android.ibeacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;

import java.util.List;

import androidx.annotation.NonNull;

// todo make package private
public class BeaconScanner implements IScanner {

    @NonNull
    private final BluetoothAdapter bluetoothAdapter;

    private final ScanCallback scanCallback;

    public BeaconScanner() {
        scanCallback = new BeaconScanCallback();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            throw new IllegalStateException("Bluetooth is not accessible on that device");
        }
    }

    @Override
    public void setRegionDefinitions(List<RegionDefinition> definitions) {
        // todo
    }

    @Override
    public void start() {
        bluetoothAdapter
            .getBluetoothLeScanner()
            .startScan(scanCallback);
    }

    @Override
    public void stop() {
        bluetoothAdapter
            .getBluetoothLeScanner()
            .stopScan(scanCallback);
    }
}

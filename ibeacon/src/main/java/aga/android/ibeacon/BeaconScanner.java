package aga.android.ibeacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;

// todo make package private
public class BeaconScanner implements IScanner {

    private static final ScanSettings SCAN_SETTINGS = new ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .build();

    @NonNull
    private final BluetoothAdapter bluetoothAdapter;

    private final ScanCallback scanCallback;

    @NonNull
    private List<ScanFilter> scanFilters = Collections.emptyList();

    public BeaconScanner() {
        scanCallback = new BeaconScanCallback();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            throw new IllegalStateException("Bluetooth is not accessible on that device");
        }
    }

    @Override
    public void setRegionDefinitions(List<RegionDefinition> definitions) {
        scanFilters = RegionDefinitionMapper.asScanFilters(definitions);
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
    }

    @Override
    public void stop() {
        bluetoothAdapter
            .getBluetoothLeScanner()
            .stopScan(scanCallback);
    }
}

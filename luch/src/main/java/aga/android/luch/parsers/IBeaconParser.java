package aga.android.luch.parsers;

import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;

import java.util.List;

import aga.android.luch.Beacon;
import aga.android.luch.Region;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface IBeaconParser {

    /**
     * Parses the scan result provided to us by the OS as a Beacon in accordance with the specified
     * beacon layout.
     *
     * @param scanResult the scan result to be parsed
     * @return Beacon instance in case the parsing has succeeded, null otherwise
     */
    @Nullable
    Beacon parse(@NonNull ScanResult scanResult);

    /**
     * Converts the provided {@link Region}s to {@link ScanFilter}s which are going to
     * be used by Android's BLE API for scanning. The {@link ScanFilter}s are created in
     * accordance with the specified beacon layout.
     *
     * Special case - empty list of {@link Region}s; in that case asScanFilters
     * method returns a list having a single instance of {@link ScanFilter} that will be
     * configured to match all {@link Beacon}s having a specified beacon layout, no matter which
     * identifiers do they contain.
     *
     * @param regions regions to be converted
     * @return list of scan filters to be used in
     *          the {@link android.bluetooth.le.BluetoothLeScanner}
     */
    @NonNull
    List<ScanFilter> asScanFilters(@NonNull List<Region> regions);
}

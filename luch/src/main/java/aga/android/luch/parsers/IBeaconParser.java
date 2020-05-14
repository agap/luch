package aga.android.luch.parsers;

import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;

import java.util.List;

import aga.android.luch.Beacon;
import aga.android.luch.RegionDefinition;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface IBeaconParser {

    @Nullable
    Beacon parse(@NonNull ScanResult scanResult);

    @NonNull
    List<ScanFilter> asScanFilters(@NonNull List<RegionDefinition> regionDefinitions);
}

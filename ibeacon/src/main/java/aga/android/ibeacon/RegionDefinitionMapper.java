package aga.android.ibeacon;

import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static aga.android.ibeacon.Conversions.byteArrayToInteger;
import static aga.android.ibeacon.Conversions.byteArrayToUuidString;
import static aga.android.ibeacon.Conversions.integerToByteArray;
import static aga.android.ibeacon.Conversions.uuidStringToByteArray;
import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOfRange;

class RegionDefinitionMapper {

    private static final int MANUFACTURER_ID = 76;

    private static final int MANUFACTURER_DATA_LENGTH = 23;

    private static final byte[] MASK_UUID_ONLY = {
        0, 0,

        1, 1, 1, 1,
        1, 1,
        1, 1,
        1, 1, 1, 1, 1, 1, 1, 1,

        0, 0,

        0, 0,

        0
    };

    private static final byte[] MASK_FULL_PACKAGE = {
        0, 0,

        1, 1, 1, 1,
        1, 1,
        1, 1,
        1, 1, 1, 1, 1, 1, 1, 1,

        1, 1,

        1, 1,

        0
    };


    private RegionDefinitionMapper() {

    }

    static List<ScanFilter> asScanFilters(@NonNull List<RegionDefinition> regionDefinitions) {
        final List<ScanFilter> filters = new ArrayList<>();

        for (RegionDefinition regionDefinition : regionDefinitions) {
            filters.add(asScanFilter(regionDefinition));
        }

        return filters;
    }

    static ScanFilter asScanFilter(@NonNull RegionDefinition regionDefinition) {
        final byte[] data = new byte[MANUFACTURER_DATA_LENGTH];

        arraycopy(
            uuidStringToByteArray(regionDefinition.getUuid()),
            0,
            data,
            2,
            16
        );

        final boolean hasMajorMinor = regionDefinition.getMajor() != null
                && regionDefinition.getMinor() != null;

        if (hasMajorMinor) {
            arraycopy(
                integerToByteArray(regionDefinition.getMajor()),
                0,
                data,
                18,
                2
            );

            arraycopy(
                integerToByteArray(regionDefinition.getMinor()),
                0,
                data,
                20,
                2
            );
        }

        return new ScanFilter
            .Builder()
            .setManufacturerData(
                MANUFACTURER_ID,
                data,
                hasMajorMinor ? MASK_FULL_PACKAGE : MASK_UUID_ONLY
            )
            .build();
    }

    @Nullable
    static Beacon asBeacon(@NonNull ScanResult scanResult) {
        if (scanResult.getScanRecord() == null) {
            return null;
        }

        final byte[] manufacturerData = scanResult
            .getScanRecord()
            .getManufacturerSpecificData(MANUFACTURER_ID);

        if (manufacturerData == null || manufacturerData.length != MANUFACTURER_DATA_LENGTH) {
            return null;
        }

        final String proximityUuid = byteArrayToUuidString(copyOfRange(manufacturerData, 2, 18));
        final int major = byteArrayToInteger(copyOfRange(manufacturerData, 18, 20));
        final int minor = byteArrayToInteger(copyOfRange(manufacturerData, 20, 22));

        return new Beacon(
            proximityUuid,
            scanResult.getDevice().getAddress(),
            major,
            minor,
            scanResult.getRssi()
        );
    }
}

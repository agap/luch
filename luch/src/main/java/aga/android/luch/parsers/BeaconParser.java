package aga.android.luch.parsers;

import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import aga.android.luch.Beacon;
import aga.android.luch.BeaconLogger;
import aga.android.luch.Region;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static aga.android.luch.parsers.Conversions.asByteArray;
import static aga.android.luch.parsers.Conversions.byteArrayToHexString;
import static java.lang.String.format;

final class BeaconParser implements IBeaconParser {

    private final List<IFieldConverter<?>> fieldConverters = new ArrayList<>();

    private final Object beaconType;

    private final int beaconTypeIndex;

    private final int manufacturerId;

    private final int txPowerIndex;

    BeaconParser(@NonNull List<IFieldConverter<?>> fieldConverters,
                 int beaconTypeIndex,
                 int manufacturerId,
                 Object beaconType,
                 int txPowerIndex) {
        this.fieldConverters.addAll(fieldConverters);
        this.beaconTypeIndex = beaconTypeIndex;
        this.manufacturerId = manufacturerId;
        this.beaconType = beaconType;
        this.txPowerIndex = txPowerIndex;
    }

    @Override
    @Nullable
    public Beacon parse(@NonNull ScanResult scanResult) {
        if (scanResult.getScanRecord() == null) {
            return null;
        }

        final SparseArray<byte[]> manufacturerData = scanResult
            .getScanRecord()
            .getManufacturerSpecificData();

        for (int i = 0; i < manufacturerData.size(); i++) {
            final int key = manufacturerData.keyAt(i);
            final byte[] rawBytes = manufacturerData.get(key);
            final List<Byte> bytesList = Conversions.asList(rawBytes);

            try {
                final List<Object> identifiers = new ArrayList<>();

                for (int j = 0; j < fieldConverters.size(); j++) {
                    final IFieldConverter<?> converter = fieldConverters.get(j);

                    final Object parsedField = converter.consume(bytesList);
                    identifiers.add(parsedField);

                    if (j == beaconTypeIndex && !beaconType.equals(parsedField)) {
                        BeaconLogger.e(
                            format(
                                Locale.US,
                                "Expected beacon type is %s, while the actual beacon type is %s;"
                                        + " the data %s will be skipped",
                                beaconType,
                                parsedField,
                                Conversions.byteArrayToHexString(rawBytes)
                            )
                        );

                        return null;
                    }
                }

                final Byte txPower = txPowerIndex != -1
                    ? ((byte) identifiers.get(txPowerIndex))
                    : null;

                return new Beacon(
                    scanResult.getDevice().getAddress(),
                    identifiers,
                    txPower
                );

            } catch (Exception e) {
                BeaconLogger.e(
                    "An attempt to parse the following manufacturerData: "
                        + byteArrayToHexString(rawBytes) + " have resulted in the following "
                        + "exception: " + e.toString()
                );
            }
        }

        return null;
    }

    @NonNull
    @Override
    public List<ScanFilter> asScanFilters(@NonNull List<Region> regions) {

        final List<ScanFilter> scanFilters = new ArrayList<>();

        try {
            if (regions.isEmpty()) {
                scanFilters.add(
                    getScanFilter(new Region.Builder().build())
                );
            } else {
                for (Region region : regions) {
                    scanFilters.add(getScanFilter(region));
                }
            }
        } catch (Exception e) {
            BeaconLogger.e(
                "Could not create scan filters based on the beacon layout and provided regions; "
                    + "the scan filters to be used are: " + scanFilters.toString()
            );
        }

        return scanFilters;
    }

    @NonNull
    private ScanFilter getScanFilter(@NonNull Region region)
            throws RegionConversionException {

        final List<Byte> mask = new ArrayList<>();
        final List<Byte> filter = new ArrayList<>();

        for (int i = 0; i < fieldConverters.size(); i++) {
            final IFieldConverter<?> parser = fieldConverters.get(i);
            final Object field = region.getFieldAt(i);

            if (field == null) {
                parser.insertMask(mask, (byte) 0x00);
                parser.insertMask(filter, (byte) 0x00);
            } else if (parser.canParse(field.getClass())) {
                parser.insertMask(mask, (byte) 0x01);
                parser.insert(filter, field);
            } else {
                throw new RegionConversionException(
                    "Type mismatch, field parser in position " + i + " has type of "
                        + IFieldConverter.class + ", while the Region object has an incompatible "
                        + "field (" + field + ") in the same position. Check the beacon layout."
                );
            }
        }

        return new ScanFilter
            .Builder()
            .setManufacturerData(
                manufacturerId,
                asByteArray(filter),
                asByteArray(mask)
            )
            .build();
    }
}

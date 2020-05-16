package aga.android.luch.parsers;

import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import aga.android.luch.Beacon;
import aga.android.luch.BeaconLogger;
import aga.android.luch.Conversions;
import aga.android.luch.RegionDefinition;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static aga.android.luch.Conversions.asByteArray;
import static aga.android.luch.Conversions.byteArrayToHexString;
import static java.lang.String.format;
import static java.util.Collections.emptyList;

final class BeaconParser implements IBeaconParser {

    private final List<IFieldConverter> fieldConverters = new ArrayList<>();

    private final Object beaconType;

    private final int beaconTypePosition;

    private final int manufacturerId;

    BeaconParser(@NonNull List<? extends IFieldConverter> fieldConverters,
                         int beaconTypePosition,
                         int manufacturerId,
                         Object beaconType) {
        this.fieldConverters.addAll(fieldConverters);
        this.beaconTypePosition = beaconTypePosition;
        this.manufacturerId = manufacturerId;
        this.beaconType = beaconType;
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
                final List identifiers = new ArrayList();

                for (int j = 0; j < fieldConverters.size(); j++) {
                    final IFieldConverter converter = fieldConverters.get(j);

                    final Object parsedField = converter.consume(bytesList);
                    //noinspection unchecked
                    identifiers.add(parsedField);

                    if (j == beaconTypePosition && beaconType != parsedField) {
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

                return new Beacon(
                    scanResult.getDevice().getAddress(),
                    identifiers
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
    public List<ScanFilter> asScanFilters(@NonNull List<RegionDefinition> regionDefinitions) {

        final List<ScanFilter> scanFilters = new ArrayList<>();

        try {
            if (regionDefinitions.isEmpty()) {
                scanFilters.add(
                    getScanFilter(new RegionDefinition(emptyList()))
                );
            } else {
                for (RegionDefinition regionDefinition : regionDefinitions) {
                    scanFilters.add(getScanFilter(regionDefinition));
                }
            }
        } catch (Exception e) {
            BeaconLogger.e(
                "Could not create scan filters based on the beacon layout and provided region "
                    + "definitions; the scan filters to be used are: " + scanFilters.toString()
            );
        }

        return scanFilters;
    }

    @NonNull
    private ScanFilter getScanFilter(@NonNull RegionDefinition regionDefinition)
            throws RegionDefinitionConversionException {

        final List<Byte> mask = getMask(regionDefinition, MASK_PRODUCER);
        final List<Byte> filter = getMask(regionDefinition, FILTER_PRODUCER);

        return new ScanFilter
            .Builder()
            .setManufacturerData(
                manufacturerId,
                asByteArray(filter),
                asByteArray(mask)
            )
            .build();
    }

    /**
     * Creates a beacon advertisement mask to be used in {@link android.bluetooth.le.ScanFilter}
     * based on the {@link IFieldConverter} it contains
     * @param regionDefinition the definition of the region to scan for; the existence or absence
     *                         of certain fields in the definition will affect the mask
     * @return byte mask
     */
    @NonNull
    private List<Byte> getMask(@NonNull RegionDefinition regionDefinition,
                               @NonNull ByteProducer byteProducer)
            throws RegionDefinitionConversionException {

        final List<Byte> data = new ArrayList<>();

        for (int i = 0; i < fieldConverters.size(); i++) {
            final IFieldConverter parser = fieldConverters.get(i);
            final Object field = regionDefinition.getFieldAt(i);

            byteProducer.produce(data, field, i, parser);
        }

        return data;
    }

    private interface ByteProducer {

        void produce(List<Byte> packet,
                     Object field,
                     int position,
                     IFieldConverter parser) throws RegionDefinitionConversionException;
    }

    private static final ByteProducer FILTER_PRODUCER = new ByteProducer() {
        @Override
        public void produce(List<Byte> packet,
                            Object field,
                            int position,
                            IFieldConverter parser) throws RegionDefinitionConversionException {

            if (field == null) {
                parser.insertMask(packet, (byte) 0x00);
            } else if (parser.canParse(field.getClass())) {
                //noinspection unchecked
                parser.insert(packet, field);
            } else {
                throw new RegionDefinitionConversionException(
                    "Type mismatch, field parser in position " + position + " has type of "
                        + IFieldConverter.class + ", while the RegionDefinition object has "
                        + "an incompatible field (" + field
                        + ") in the same position. Check the beacon layout."
                );
            }
        }
    };

    private static final ByteProducer MASK_PRODUCER = new ByteProducer() {
        @Override
        public void produce(List<Byte> packet,
                            Object field,
                            int position,
                            IFieldConverter parser) throws RegionDefinitionConversionException {

            final byte maskByte;

            if (field == null) {
                maskByte = 0x00;
            } else if (parser.canParse(field.getClass())) {
                maskByte = 0x01;
            } else {
                throw new RegionDefinitionConversionException(
                    "Type mismatch, field parser in position " + position + " has type of "
                            + IFieldConverter.class + ", while the RegionDefinition object has "
                            + "an incompatible field (" + field
                            + ") in the same position. Check the beacon layout."
                );
            }

            parser.insertMask(packet, maskByte);
        }
    };
}

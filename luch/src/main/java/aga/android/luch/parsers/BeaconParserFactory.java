package aga.android.luch.parsers;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public final class BeaconParserFactory {

    private static final int ALTBEACON_MANUFACTURER_ID = 0x0118;

    private static final List<String> SUPPORTED_PREFIXES = asList("m", "i", "p", "d");

    private static final List<? extends IFieldConverter> SUPPORTED_CONVERTERS = asList(
        new SingleByteFieldConverter(),
        new IntegerFieldConverter(),
        new UuidFieldConverter()
    );

    private BeaconParserFactory() {
        // no instances please
        // add comment
    }

    public static final IBeaconParser ALTBEACON_PARSER = createFromLayout(
        "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25",
        ALTBEACON_MANUFACTURER_ID
    );

    @SuppressWarnings("WeakerAccess")
    public static IBeaconParser createFromLayout(@NonNull String beaconLayout) {
        return createFromLayout(beaconLayout, 0x004C);
    }

    @SuppressWarnings("WeakerAccess")
    public static IBeaconParser createFromLayout(@NonNull String beaconLayout,
                                                 int manufacturerId) {

        final List<IFieldConverter> converters = new ArrayList<>();

        final String[] tokens = beaconLayout.split(",");

        int beaconTypeFieldPosition = -1;

        Object beaconType = null;

        for (String token : tokens) {
            final String fieldPrefix = getFieldPrefix(token);
            final IFieldConverter converter = getSuitableConverter(token);

            if (converter == null) {
                throw new IllegalArgumentException(
                    format("Can't find the converter for token %s", token)
                );
            } else {
                converters.add(converter);
            }

            if (fieldPrefix.equals("m")) {
                if (!token.contains("=")) {
                    throw new IllegalArgumentException(
                        format("Can't find the beacon's type value in token %s", token)
                    );
                }

                final String rawBeaconType = token.split("=")[1];

                beaconType = converter.consume(
                    Conversions.asList(Conversions.hexStringToByteArray(rawBeaconType))
                );

                beaconTypeFieldPosition = converters.size() - 1;
            }
        }

        if (beaconType == null) {
            throw new IllegalArgumentException(
                format("Could not find the beacon type field in the layout %s", beaconLayout)
            );
        }

        return new BeaconParser(converters, beaconTypeFieldPosition, manufacturerId, beaconType);
    }

    private static String getFieldPrefix(@NonNull String token) {
        final String fieldPrefix = token.substring(0, 1);

        if (!SUPPORTED_PREFIXES.contains(fieldPrefix)) {
            throw new IllegalArgumentException(
                format("Can't parse the field token %s, the prefix is not supported", token)
            );
        }

        return fieldPrefix;
    }

    private static IFieldConverter getSuitableConverter(String token) {

        final String[] fieldRange;
        final int length;

        try {
            fieldRange = token.split(":")[1].split("=")[0].split("-");

            final int startIndex = parseInt(fieldRange[0]);
            final int endIndex = parseInt(fieldRange[1]);

            length = endIndex - startIndex + 1;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                format(
                    "Can't parse the field token %s, something is wrong with the field range",
                    token
                )
            );
        }

        IFieldConverter suitableConverter = null;

        for (IFieldConverter converter : SUPPORTED_CONVERTERS) {
            if (converter.canParse(length)) {
                suitableConverter = converter;
                break;
            }
        }

        return suitableConverter;
    }
}

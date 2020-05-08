package aga.android.luch.parsers;

import java.util.Collection;

import androidx.annotation.NonNull;

import static aga.android.luch.Conversions.byteArrayToHexString;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;

public class IntegerFieldParser implements IFieldParser<Integer> {

    private static final Collection<Byte> MASK = unmodifiableCollection(
        asList(
            (byte) 1, (byte) 1
        )
    );

    @Override
    public Integer parse(@NonNull byte[] packet, int start) throws BeaconParseException {
        try {
            return (packet[start] & 0xff) * 0x100 + (packet[start + 1] & 0xff);
        } catch (Exception e) {
            throw new BeaconParseException(
                "Could not parse the integer from the data packet " + byteArrayToHexString(packet)
                    + "(starting byte index is " + start + "; expected to see 2 bytes)",
                e
            );
        }
    }

    @Override
    public Collection<Byte> getMask() {
        return MASK;
    }
}

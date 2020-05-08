package aga.android.luch.parsers;

import java.util.Collection;

import androidx.annotation.NonNull;

import static aga.android.luch.Conversions.byteArrayToHexString;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableCollection;

public class SingleByteFieldParser implements IFieldParser<Byte> {

    private final Collection<Byte> MASK = unmodifiableCollection(
        singletonList(
            (byte) 1
        )
    );

    @Override
    public Byte parse(@NonNull byte[] packet, int start) throws BeaconParseException {
        try {
            return packet[start];
        } catch (Exception e) {
            throw new BeaconParseException(
                "Could not take the single byte from the data packet "
                    + byteArrayToHexString(packet) + " (starting byte index is " + start
                    + "; expected to see 1 byte)",
                e
            );
        }
    }

    @Override
    public Collection<Byte> getMask() {
        return MASK;
    }
}

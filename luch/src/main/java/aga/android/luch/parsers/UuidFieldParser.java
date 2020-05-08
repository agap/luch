package aga.android.luch.parsers;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import androidx.annotation.NonNull;

import static aga.android.luch.Conversions.byteArrayToHexString;
import static java.util.Collections.unmodifiableCollection;

public class UuidFieldParser implements IFieldParser<UUID> {

    private final Collection<Byte> MASK = unmodifiableCollection(
        Arrays.asList(
            (byte) 1, (byte) 1, (byte) 1, (byte) 1,
            (byte) 1, (byte) 1, (byte) 1, (byte) 1,
            (byte) 1, (byte) 1, (byte) 1, (byte) 1,
            (byte) 1, (byte) 1, (byte) 1, (byte) 1
        )
    );

    @Override
    public UUID parse(@NonNull byte[] packet, int start) throws BeaconParseException {
        try {
            final ByteBuffer byteBuffer = ByteBuffer.wrap(packet, start, 16);
            return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
        } catch (Exception e) {
            throw new BeaconParseException(
                "Could not parse the UUID from the data packet " + byteArrayToHexString(packet)
                    + " (starting byte index is " + start + "; expected to see 16 bytes)",
                e
            );
        }
    }

    @Override
    public Collection<Byte> getMask() {
        return MASK;
    }
}

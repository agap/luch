package aga.android.luch.parsers;

import java.nio.ByteBuffer;
import java.util.UUID;

import androidx.annotation.NonNull;

import static aga.android.luch.Conversions.byteArrayToHexString;

public class UuidFieldParser implements IFieldParser<UUID> {

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
    public int getFieldLength() {
        return 16;
    }
}

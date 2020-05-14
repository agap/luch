package aga.android.luch.parsers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;

public class UuidFieldParser implements IFieldParser<UUID> {

    private static final int UUID_BYTE_SIZE = 16;

    private final byte[] bytes = new byte[UUID_BYTE_SIZE];

    @Override
    public UUID consume(@NonNull List<Byte> packet) throws BeaconParseException {
        try {
            for (int i = 0; i < UUID_BYTE_SIZE; i++) {
                bytes[i] = packet.remove(0);
            }

            final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

            return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
        } catch (Exception e) {
            //todo handle higher
            throw new BeaconParseException(
                "Could not parse the UUID from the data packet ",
//                        + byteArrayToHexString(packet)
//                    + " (starting byte index is " + start + "; expected to see 16 bytes)",
                e
            );
        }
    }

    @Override
    public void insert(@NonNull List<Byte> packet, @NonNull UUID value) {
        ByteBuffer
            .wrap(bytes)
            .order(ByteOrder.BIG_ENDIAN)
            .putLong(value.getMostSignificantBits())
            .putLong(value.getLeastSignificantBits());

        for (int i = 0; i < packet.size(); i++) {
            packet.add(bytes[i]);
        }
    }

    @Override
    public void insertMask(@NonNull List<Byte> packet, byte maskBit) {
        for (int i = 0; i < UUID_BYTE_SIZE; i++) {
            packet.add(maskBit);
        }
    }

    @Override
    public boolean canParse(@NonNull Class clazz) {
        return UUID.class.isAssignableFrom(clazz);
    }
}

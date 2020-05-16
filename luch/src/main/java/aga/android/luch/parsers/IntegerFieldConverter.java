package aga.android.luch.parsers;

import java.util.List;

import androidx.annotation.NonNull;

class IntegerFieldConverter implements IFieldConverter<Integer> {

    @Override
    public Integer consume(@NonNull List<Byte> packet) {
        final int mostSigBits = packet.remove(0);
        final int leastSigBits = packet.remove(0);

        return (mostSigBits & 0xff) * 0x100 + (leastSigBits & 0xff);
    }

    @Override
    public void insert(@NonNull List<Byte> packet, @NonNull Integer value) {
        packet.add((byte) (value / 256));
        packet.add((byte) (value / 256));
    }

    @Override
    public void insertMask(@NonNull List<Byte> packet, byte maskBit) {
        packet.add(maskBit);
        packet.add(maskBit);
    }

    @Override
    public boolean canParse(@NonNull Class clazz) {
        return Integer.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean canParse(int numberOfBytes) {
        return numberOfBytes == 2;
    }
}

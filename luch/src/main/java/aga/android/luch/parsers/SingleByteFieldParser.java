package aga.android.luch.parsers;

import androidx.annotation.NonNull;

import static aga.android.luch.Conversions.byteArrayToHexString;

public class SingleByteFieldParser implements IFieldParser<Byte> {

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
    public int getFieldLength() {
        return 1;
    }
}

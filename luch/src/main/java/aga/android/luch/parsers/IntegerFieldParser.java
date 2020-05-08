package aga.android.luch.parsers;

import androidx.annotation.NonNull;

import static aga.android.luch.Conversions.byteArrayToHexString;

public class IntegerFieldParser implements IFieldParser<Integer> {

    @Override
    public Integer parse(@NonNull byte[] packet, int start) throws BeaconParseException {
        try {
            return (packet[start] & 0xff) * 0x100 + (packet[start + 1] & 0xff);
        } catch (Exception e) {
            throw new BeaconParseException(
                "Could not parse the integer from the data packet " + byteArrayToHexString(packet)
                    + " (starting byte index is " + start + "; expected to see 2 bytes)",
                e
            );
        }
    }

    @Override
    public int getFieldLength() {
        return 2;
    }
}

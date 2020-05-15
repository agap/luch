package aga.android.luch;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

public class ConversionsTest {

    private static final byte[] NIL_UUID_EXPECTED_BYTE_ARRAY = new byte[16];
    private static final byte[] LARGEST_UUID_EXPECTED_BYTE_ARRAY = new byte[16];

    static {
        Arrays.fill(LARGEST_UUID_EXPECTED_BYTE_ARRAY, (byte) 0xff);
    }

    @Test
    public void testNilUuidStringToByteArrayConversion() {

        // given
        final String uuid = "00000000-0000-0000-0000-000000000000";

        // when
        final byte[] byteArrayRepresentation = Conversions.uuidStringToByteArray(uuid);

        // then
        assertArrayEquals(NIL_UUID_EXPECTED_BYTE_ARRAY, byteArrayRepresentation);
    }

    @Test
    public void testLargestUuidStringToByteArrayConversion() {

        // given
        final String uuid = "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF";

        // when
        final byte[] byteArrayRepresentation = Conversions.uuidStringToByteArray(uuid);

        // then
        assertArrayEquals(LARGEST_UUID_EXPECTED_BYTE_ARRAY, byteArrayRepresentation);
    }
}
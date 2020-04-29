package aga.android.luch;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

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

    @Test
    public void testNilUuidByteArrayToStringConversion() {

        // when
        final String uuid = Conversions.byteArrayToUuidString(NIL_UUID_EXPECTED_BYTE_ARRAY);

        // then
        assertEquals("00000000-0000-0000-0000-000000000000", uuid);
    }

    @Test
    public void testLargestUuidByteArrayToStringConversion() {

        // when
        final String uuid = Conversions.byteArrayToUuidString(LARGEST_UUID_EXPECTED_BYTE_ARRAY);

        // then
        assertEquals("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF", uuid);
    }

    @Test
    public void testZeroByteArrayToIntegerConversion() {

        // given
        final byte[] array = new byte[2];

        // when
        final int result = Conversions.byteArrayToInteger(array);

        // then
        assertEquals(0, result);
    }

    @Test
    public void testByteArrayRepresenting65535ToIntegerConversion() {

        // given
        final byte[] array = {(byte) 0xFF, (byte) 0xFF};

        // when
        final int result = Conversions.byteArrayToInteger(array);

        // then
        assertEquals(65535, result);
    }

    @Test
    public void testZeroIntegerToByteArrayConversion() {

        // when
        final byte[] result = Conversions.integerToByteArray(0);

        // then
        assertArrayEquals(new byte[] { (byte) 0x00, (byte) 0x00 }, result);
    }

    @Test
    public void testBiggestMajorOrMinorToByteArrayConversion() {

        // when
        final byte[] result = Conversions.integerToByteArray(65535);

        // then
        assertArrayEquals(new byte[] { (byte) 0xFF, (byte) 0xFF }, result);
    }
}
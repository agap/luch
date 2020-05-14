package aga.android.luch.parsers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IntegerFieldParserTest {

    private final IntegerFieldParser parser = new IntegerFieldParser();

    @Test(expected = BeaconParseException.class)
    public void testNotHavingEnoughDataToParseThrowsBeaconParseException()
        throws BeaconParseException {

        // given
        final byte[] packet = new byte[3];

        // when
        parser.consume(packet, 2);
    }

    @Test
    public void testFieldLengthIsCorrect() {

        // when
        final int length = parser.getFieldLength();

        // then
        assertEquals(
            2,
            length
        );
    }

    @Test
    public void testZeroByteSequenceToIntegerConversion() throws BeaconParseException {

        // given
        final byte[] packet = {0x00, 0x15, (byte) 0xFF, (byte) 0x00, (byte) 0x00};

        // when
        final int result = parser.consume(packet, 3);

        // then
        assertEquals(0, result);
    }

    @Test
    public void testByteSequenceRepresenting65535ToIntegerConversion() throws BeaconParseException {

        // given
        final byte[] packet = {0x00, 0x15, (byte) 0xFF, (byte) 0xFF, (byte) 0xA0};

        // when
        final int result = parser.consume(packet, 2);

        // then
        assertEquals(65535, result);
    }
}

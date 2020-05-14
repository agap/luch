package aga.android.luch.parsers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SingleByteParserTest {

    private final SingleByteFieldParser parser = new SingleByteFieldParser();

    @Test(expected = BeaconParseException.class)
    public void testAttemptToExtractByteUnderWrongIndexThrowsBeaconParserException()
        throws BeaconParseException {

        // given
        final byte[] packet = new byte[4];

        // when
        parser.consume(packet, 4);
    }

    @Test
    public void testFieldLengthIsCorrect() {

        // when
        final int length = parser.getFieldLength();

        // then
        assertEquals(
            1,
            length
        );
    }
}

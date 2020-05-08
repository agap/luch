package aga.android.luch.parsers;

import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class SingleByteParserTest {

    private final SingleByteFieldParser parser = new SingleByteFieldParser();

    @Test(expected = BeaconParseException.class)
    public void testAttemptToExtractByteUnderWrongIndexThrowsBeaconParserException()
        throws BeaconParseException {

        // given
        final byte[] packet = new byte[4];

        // when
        parser.parse(packet, 4);
    }

    @Test
    public void testMaskIsCorrect() {

        // given
        final Collection<Byte> mask = parser.getMask();

        // then
        assertThat(
            mask,
            contains((byte) 1)
        );
    }
}

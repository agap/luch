package aga.android.luch.parsers;

import org.junit.Test;

import java.util.Collection;
import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class UuidFieldParserTest {

    private final UuidFieldParser parser = new UuidFieldParser();

    @Test(expected = BeaconParseException.class)
    public void testNotHavingEnoughDataToParseThrowsBeaconParseException()
        throws BeaconParseException {

        // given
        final byte[] packet = new byte[15];

        // when
        parser.parse(packet, 0);
    }

    @Test
    public void testMaskIsCorrect() {

        // when
        final Collection<Byte> mask = parser.getMask();

        // then
        assertThat(
            mask,
            contains(
                (byte) 1, (byte) 1, (byte) 1, (byte) 1,
                (byte) 1, (byte) 1, (byte) 1, (byte) 1,
                (byte) 1, (byte) 1, (byte) 1, (byte) 1,
                (byte) 1, (byte) 1, (byte) 1, (byte) 1
            )
        );
    }

    @Test
    public void testAllBytesZeroToUuidParsingIsCorrect() throws BeaconParseException {

        // given
        final byte[] packet = {
            0x02, (byte) 0xFE, 0x14, 0x77,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            (byte) 0xAE, (byte) 0xEA
        };

        // when
        final UUID uuid = parser.parse(packet, 4);

        // then
        assertEquals(
            UUID.fromString("00000000-0000-0000-0000-000000000000"),
            uuid
        );
    }

    @Test
    public void testAllBytesFFToUuidParsingIsCorrect() throws BeaconParseException {

        // given
        final byte[] packet = {
            0x02, (byte) 0xFE, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, 0x00
        };

        // when
        final UUID uuid = parser.parse(packet, 2);

        // then
        assertEquals(
            UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"),
            uuid
        );
    }
}

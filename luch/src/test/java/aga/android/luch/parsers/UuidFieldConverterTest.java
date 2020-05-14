package aga.android.luch.parsers;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static org.junit.Assert.assertEquals;

public class UuidFieldConverterTest {

    private final UuidFieldConverter parser = new UuidFieldConverter();

    @Test(expected = Exception.class)
    public void testNotHavingEnoughDataToParseThrowsBeaconParseException() {

        // given
        final List<Byte> packet = new ArrayList<>(
            asList(
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00
            )
        );

        // when
        parser.consume(packet);
    }

    @Test
    public void testAllBytesZeroToUuidParsingIsCorrect() {

        // given
        final List<Byte> packet = new ArrayList<>(
            asList(
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0xFF
            )
        );

        // when
        final UUID uuid = parser.consume(packet);

        // then
        assertEquals(
            fromString("00000000-0000-0000-0000-000000000000"),
            uuid
        );
        assertEquals(
            singletonList((byte) 0xFF),
            packet
        );
    }

    @Test
    public void testAllBytesFFToUuidParsingIsCorrect() {

        // given
        final List<Byte> packet = new ArrayList<>(
            asList(
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFE, (byte) 0x12, (byte) 0x00
            )
        );

        // when
        final UUID uuid = parser.consume(packet);

        // then
        assertEquals(
            fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"),
            uuid
        );
        assertEquals(
            asList((byte) 0xFE, (byte) 0x12, (byte) 0x00),
            packet
        );
    }
}

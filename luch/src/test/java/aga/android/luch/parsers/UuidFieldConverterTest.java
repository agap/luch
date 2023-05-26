package aga.android.luch.parsers;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class UuidFieldConverterTest {

    private final UuidFieldConverter converter = new UuidFieldConverter();
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
        converter.consume(packet);
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
        final UUID uuid = converter.consume(packet);

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
        final UUID uuid = converter.consume(packet);

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

    @Test
    public void testInsert() {

        // given
        final List<Byte> packet = new ArrayList<>(singletonList((byte) 0xFF));

        // when
        converter.insert(packet, fromString("01234567-0123-4567-89AB-456789ABCDEF"));

        // then
        assertEquals(
            asList(
                (byte) 0xFF,
                (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67,
                (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67,
                (byte) 0x89, (byte) 0xAB, (byte) 0x45, (byte) 0x67,
                (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF
            ),
            packet
        );
    }

    @Test
    public void testInsertMask() {

        // given
        final List<Byte> packet = new ArrayList<>(singletonList((byte) 0xFF));

        // when
        converter.insertMask(packet, (byte) 0x01);

        // then
        assertEquals(
            asList(
                (byte) 0xFF,
                (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
                (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
                (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
                (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01
            ),
            packet
        );
    }

    @Test
    public void testUuidObjectCanBeParsed() {

        assertTrue(converter.canParse(UUID.class));
    }

    @Test
    public void testByteSequenceNotHaving16BytesCanNotBeParsed() {

        assertFalse(converter.canParse(15));
    }
}

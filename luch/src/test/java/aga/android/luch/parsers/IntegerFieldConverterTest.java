package aga.android.luch.parsers;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class IntegerFieldConverterTest {

    private final IntegerFieldConverter converter = new IntegerFieldConverter();

    @Test(expected = Exception.class)
    public void testNotHavingEnoughDataToParseThrowsException() {

        // given
        final List<Byte> packet = new ArrayList<>(
            singletonList((byte) 0x01)
        );

        // when
        converter.consume(packet);
    }

    @Test
    public void testZeroByteSequenceToIntegerConversion() {

        // given
        final List<Byte> packet = new ArrayList<>(
            asList(
                (byte) 0x00, (byte) 0x00, (byte) 0x10
            )
        );

        // when
        final int result = converter.consume(packet);

        // then
        assertEquals(0, result);
    }

    @Test
    public void testByteSequenceRepresenting65535ToIntegerConversion() {

        // given
        final List<Byte> packet = new ArrayList<>(
            asList(
                (byte) 0xFF, (byte) 0xFF, (byte) 0xA0
            )
        );

        // when
        final int result = converter.consume(packet);

        // then
        assertEquals(65535, result);
    }

    @Test
    public void testInsert() {

        // given
        final List<Byte> packet = new ArrayList<>(asList((byte) 0xAE, (byte) 0x25));

        // when
        converter.insert(packet, 65535);

        // then
        assertEquals(
            asList((byte) 0xAE, (byte) 0x25, (byte) 0xFF, (byte) 0xFF),
            packet
        );
    }

    @Test
    public void testInsertMask() {

        // given
        final List<Byte> packet = new ArrayList<>(asList((byte) 0xEA, (byte) 0x12));

        // when
        converter.insertMask(packet, (byte) 0x01);

        // then
        assertEquals(
            asList((byte) 0xEA, (byte) 0x12, (byte) 0x01, (byte) 0x01),
            packet
        );
    }

    @Test
    public void testIntegerObjectCanBeParsed() {

        assertTrue(converter.canParse(Integer.class));
    }
}

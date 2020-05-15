package aga.android.luch.parsers;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class SingleByteConverterTest {

    private final SingleByteFieldConverter converter = new SingleByteFieldConverter();

    @Test(expected = Exception.class)
    public void testNotHavingEnoughDataToParseThrowsException() {

        // given
        final List<Byte> packet = Collections.emptyList();

        // when
        converter.consume(packet);
    }

    @Test
    public void testInsert() {

        // given
        final List<Byte> packet = new ArrayList<>(singletonList((byte) 0xFF));

        // when
        converter.insert(packet, (byte) 0x11);

        // then
        assertEquals(
            Arrays.asList((byte) 0xFF, (byte) 0x11),
            packet
        );
    }

    @Test
    public void testInsertMask() {

        // given
        final List<Byte> packet = new ArrayList<>(singletonList((byte) 0xFF));

        // when
        converter.insertMask(packet, (byte) 0x00);

        // then
        assertEquals(
            Arrays.asList((byte) 0xFF, (byte) 0x00),
            packet
        );
    }

    @Test
    public void testByteObjectCanBeParsed() {

        assertTrue(converter.canParse(Byte.class));
    }
}

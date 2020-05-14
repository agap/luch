package aga.android.luch.parsers;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class IntegerFieldConverterTest {

    private final IntegerFieldConverter parser = new IntegerFieldConverter();

    @Test(expected = Exception.class)
    public void testNotHavingEnoughDataToParseThrowsException() {

        // given
        final List<Byte> packet = new ArrayList<>(
            singletonList((byte) 0x01)
        ) ;

        // when
        parser.consume(packet);
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
        final int result = parser.consume(packet);

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
        final int result = parser.consume(packet);

        // then
        assertEquals(65535, result);
    }
}

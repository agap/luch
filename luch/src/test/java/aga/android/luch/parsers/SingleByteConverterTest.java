package aga.android.luch.parsers;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class SingleByteConverterTest {

    private final SingleByteFieldConverter parser = new SingleByteFieldConverter();

    @Test(expected = Exception.class)
    public void testNotHavingEnoughDataToParseThrowsException() {

        // given
        final List<Byte> packet = Collections.emptyList();

        // when
        parser.consume(packet);
    }
}

package aga.android.luch.distance;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RunningAverageRssiFilterTest {

    @Test
    public void testRunningAverageCalculation() {

        // given
        final List<Reading> readings = Arrays.asList(
            new Reading((byte) -95, 100_000_000),
            new Reading((byte) -98, 100_000_100),
            new Reading((byte) -100, 100_000_200)
        );

        final IRssiFilter filter = new RunningAverageRssiFilter();

        // when
        final byte average = filter.getFilteredValue(readings);

        // then
        assertEquals(-97, average);
    }
}

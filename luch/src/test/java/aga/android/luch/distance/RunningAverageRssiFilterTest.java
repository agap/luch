package aga.android.luch.distance;

import org.junit.Test;

import aga.android.luch.ITimeProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RunningAverageRssiFilterTest {

    @Test
    public void testRunningAverageWithNoHistoryCalculation() {

        // given
        final ITimeProvider.TestTimeProvider timeProvider = new ITimeProvider.TestTimeProvider();

        final RssiFilter filter = new RunningAverageRssiFilter.Builder()
            .addTimeProvider(timeProvider)
            .build();

        // when
        final Byte average = filter.getFilteredValue();

        // then
        assertNull(average);
    }

    @Test
    public void testRunningAverageCalculationWithData() {

        // given
        final ITimeProvider.TestTimeProvider timeProvider = new ITimeProvider.TestTimeProvider();

        final RssiFilter filter = new RunningAverageRssiFilter.Builder()
            .addRssiValidityPeriodMillis(1_000)
            .addTimeProvider(timeProvider)
            .build();

        // when
        filter.addReading((byte) -95);
        filter.addReading((byte) -98);
        filter.addReading((byte) -100);

        final Byte average = filter.getFilteredValue();

        // then
        assertEquals(Byte.valueOf((byte) -97), average);
    }

    @Test
    public void testRunningAverageCalculationOnOutdatedDataReturnsNull() {
        // given
        final ITimeProvider.TestTimeProvider timeProvider = new ITimeProvider.TestTimeProvider();

        final RssiFilter filter = new RunningAverageRssiFilter.Builder()
            .addRssiValidityPeriodMillis(1_000)
            .addTimeProvider(timeProvider)
            .build();

        // when
        filter.addReading((byte) -95);
        filter.addReading((byte) -98);
        filter.addReading((byte) -100);

        // then
        assertEquals(Byte.valueOf((byte) -97), filter.getFilteredValue());

        // when
        timeProvider.elapsedRealTimeMillis = 2_000;
        filter.addReading((byte) -120);

        // then
        assertEquals(Byte.valueOf((byte) -120), filter.getFilteredValue());
    }
}

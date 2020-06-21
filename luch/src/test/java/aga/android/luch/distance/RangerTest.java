package aga.android.luch.distance;

import org.junit.Test;

import aga.android.luch.Beacon;
import aga.android.luch.ITimeProvider.TestTimeProvider;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

public class RangerTest {

    private final TestTimeProvider timeProvider = new TestTimeProvider();

    private final RssiFilter.Builder testFilter = new RunningAverageRssiFilter
        .Builder()
        .addTimeProvider(timeProvider);

    @Test
    public void testRangeCalculationWithEmptyCacheAndSameTxPowerAndRssi() {

        // given
        final Ranger ranger = new Ranger(testFilter);
        final Beacon beacon = new Beacon("00:11:22:33:44:EE", emptyList(), (byte) -95);
        beacon.setRssi((byte) -95);

        // when
        final double distance = ranger.calculateDistance(beacon);

        // then
        assertEquals(1.0f, distance, 0.0);
    }

    @Test
    public void testRangeCalculationWithEmptyCacheAndTxPowerGreaterThanRssi() {

        // given
        final Ranger ranger = new Ranger(testFilter);
        final Beacon beacon = new Beacon("00:11:22:33:44:EE", emptyList(), (byte) -85);
        beacon.setRssi((byte) -95);

        // when
        final double distance = ranger.calculateDistance(beacon);

        // then
        assertEquals(3.16, distance, 0.01);
    }

    @Test
    public void testRangeCalculationWithEmptyCacheAndTxPowerSmallerThanRssi() {

        // given
        final Ranger ranger = new Ranger(testFilter);
        final Beacon beacon = new Beacon("00:11:22:33:44:EE", emptyList(), (byte) -95);
        beacon.setRssi((byte) -85);

        // when
        final double distance = ranger.calculateDistance(beacon);

        // then
        assertEquals(0.316, distance, 0.001);
    }

    @Test
    public void testRangeCalculationWithNonEmptyCache() {

        // given
        final Ranger ranger = new Ranger(testFilter);
        final Beacon beacon = new Beacon("00:11:22:33:44:EE", emptyList(), (byte) -95);
        beacon.setRssi((byte) -95);

        // when
        ranger.addReading(beacon, (byte) -100);
        ranger.addReading(beacon, (byte) -102);
        ranger.addReading(beacon, (byte) -99);
        ranger.addReading(beacon, (byte) -97);
        ranger.addReading(beacon, (byte) -95);
        final double distance = ranger.calculateDistance(beacon);

        // then
        assertEquals(1.412, distance, 0.001);
    }

    @Test
    public void testRangeCalculationOfBeaconWithNoTxPower() {
        // given
        final Ranger ranger = new Ranger(testFilter);
        final Beacon beacon = new Beacon("00:11:22:33:44:EE", emptyList(), null);
        beacon.setRssi((byte) -85);

        // when
        final double distance = ranger.calculateDistance(beacon);

        // then
        assertEquals(Double.MAX_VALUE, distance, 0.000001);
    }
}

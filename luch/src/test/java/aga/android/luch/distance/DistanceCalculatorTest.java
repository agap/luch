package aga.android.luch.distance;

import org.junit.Test;

import java.util.Collections;

import aga.android.luch.Beacon;

import static org.junit.Assert.assertEquals;

public class DistanceCalculatorTest {

    private final AbstractDistanceCalculator calculator = new DistanceCalculator(0);

    @Test
    public void testTxPowerSmallerThanRssi() {
        // given
        final Beacon beacon = new Beacon(
            "00:11:22:33:FF:EE",
            Collections.singletonList((byte) -125),
            null
        );
        beacon.setRssi((byte) -95);

        // when
        final double distance = calculator.getDistance(beacon);

        // then
        assertEquals(0.03162277660168379, distance, 0.0);
    }

    @Test
    public void testTxPowerGreaterThanRssi() {
        // given
        final Beacon beacon = new Beacon(
            "00:11:22:33:FF:EE",
            Collections.singletonList((byte) -90),
            null
        );
        beacon.setRssi((byte) -95);

        // when
        final double distance = calculator.getDistance(beacon);

        // then
        assertEquals(1.7782794100389228, distance, 0.0);
    }

    @Test
    public void testTxPowerEqualToRssi() {
        // given
        final Beacon beacon = new Beacon(
            "00:11:22:33:FF:EE",
            Collections.singletonList((byte) -95),
            null
        );
        beacon.setRssi((byte) -95);

        // when
        final double distance = calculator.getDistance(beacon);

        // then
        assertEquals(1.0, distance, 0.0);
    }
}

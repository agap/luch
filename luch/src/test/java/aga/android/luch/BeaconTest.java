package aga.android.luch;

import org.junit.Test;

import java.util.Collections;

import static aga.android.luch.distance.DistanceCalculatorFactory.getCalculator;
import static org.junit.Assert.assertEquals;

public class BeaconTest {

    @Test
    public void testBeaconWithoutDistanceCalculatorReturnsMaxLongAsDistance() {

        // given
        final Beacon beacon = new Beacon(
            "00:11:22:33:FF:EE",
            Collections.emptyList(),
            null
        );

        // when
        final double distance = beacon.getDistance();

        // then
        assertEquals(Double.MAX_VALUE, distance, 0.0);
    }

    @Test
    public void testBeaconWithDistanceCalculatorReturnsActualDistance() {

        // given
        final Beacon beacon = new Beacon(
            "00:11:22:33:FF:EE",
            Collections.singletonList((byte) -105),
            getCalculator(0)
        );

        beacon.setRssi((byte) -95);

        // when
        final double distance = beacon.getDistance();

        // then
        assertEquals(0.31622776601683794, distance, 0.0);
    }
}

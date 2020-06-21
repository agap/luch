package aga.android.luch.rssi;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArmaFilterTest {

    @Test
    public void testArmaFilterFirstMeasurement() {

        // given
        final RssiFilter filter = new ArmaFilter.Builder().build();

        // when
        filter.addReading((byte) -120);

        // then
        assertEquals(Byte.valueOf((byte) -120), filter.getFilteredValue());
    }

    @Test
    public void testArmaFilterSecondMeasurement() {

        // given
        final RssiFilter filter = new ArmaFilter.Builder().build();

        // when
        filter.addReading((byte) -120);
        filter.addReading((byte) -110);

        // then
        assertEquals(Byte.valueOf((byte) -119), filter.getFilteredValue());
    }

    @Test
    public void testArmaFilterSecondMeasurementWithNonDefaultSpeed() {

        // given
        final RssiFilter filter = new ArmaFilter.Builder()
            .setArmaSpeed(0.5f)
            .build();

        // when
        filter.addReading((byte) -120);
        filter.addReading((byte) -110);

        // then
        assertEquals(Byte.valueOf((byte) -115), filter.getFilteredValue());
    }
}

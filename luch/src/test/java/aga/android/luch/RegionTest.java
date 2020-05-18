package aga.android.luch;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class RegionTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeIntegerFieldIsRejected() {

        final Region region = new Region
            .Builder()
            .addIntegerField(-1)
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntegerGreaterThan65535FieldIsRejected() {

        final Region region = new Region
            .Builder()
            .addIntegerField(65536)
            .build();
    }

    @Test
    public void testIntegerFieldWithinAcceptedRangeIsNotRejected() {

        // when
        final Region region = new Region
            .Builder()
            .addIntegerField(28536)
            .build();

        // then
        assertEquals(28536, region.getFieldAt(0));
    }

    @Test
    public void testInsertionOrderIsPreserved() {

        // given
        final UUID uuid = UUID.fromString("01234567-0123-4567-89AB-456789ABCDEF");

        // when
        final Region region = new Region
            .Builder()
            .addIntegerField(28536)
            .addUuidField(uuid)
            .addIntegerField(100)
            .addIntegerField(65530)
            .addByteField((byte) 0xBE)
            .build();

        // then
        assertEquals(28536, region.getFieldAt(0));
        assertEquals(uuid, region.getFieldAt(1));
        assertEquals(100, region.getFieldAt(2));
        assertEquals(65530, region.getFieldAt(3));
        assertEquals((byte) 0xBE, region.getFieldAt(4));
    }
}

package aga.android.ibeacon;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RegionDefinitionTest {

    @Test(expected = AssertionError.class)
    public void testNegativeMajorIsNotAccepted() {

        new RegionDefinition(
            "ffffffff-1234-aaaa-1a2b-a1b2c3d4e5f6",
            -1,
            120
        );
    }

    @Test(expected = AssertionError.class)
    public void testNegativeMinorIsNotAccepted() {

        new RegionDefinition(
            "ffffffff-1234-aaaa-1a2b-a1b2c3d4e5f6",
            65535,
            -1
        );
    }

    @Test(expected = AssertionError.class)
    public void testMajorGreaterThan65535IsNotAccepted() {

        new RegionDefinition(
            "ffffffff-1234-aaaa-1a2b-a1b2c3d4e5f6",
            65536,
            120
        );
    }

    @Test(expected = AssertionError.class)
    public void testMinorGreaterThan65535IsNotAccepted() {

        new RegionDefinition(
            "ffffffff-1234-aaaa-1a2b-a1b2c3d4e5f6",
            120,
            65536
        );
    }

    @Test(expected = AssertionError.class)
    public void testNonNullMajorAndNullMinorAreNotAccepted() {

        new RegionDefinition(
            "ffffffff-1234-aaaa-1a2b-a1b2c3d4e5f6",
            100,
            null
        );
    }

    @Test(expected = AssertionError.class)
    public void testNonNullMinorAndNullMajorAreNotAccepted() {

        new RegionDefinition(
            "ffffffff-1234-aaaa-1a2b-a1b2c3d4e5f6",
            null,
            100
        );
    }

    @Test
    public void testRegionHavingOnlyUuidIsConstructedProperly() {
        // given
        final String uuid = "ffffffff-1234-aaaa-1a2b-a1b2c3d4e5f6";

        // when
        final RegionDefinition definition = new RegionDefinition(uuid);

        // then
        assertEquals(uuid, definition.getUuid());
        assertNull(definition.getMajor());
        assertNull(definition.getMinor());
    }

    @Test
    public void testRegionHavingAllParametersIsConstructedProperly() {
        // given
        final String uuid = "ffffffff-1234-aaaa-1a2b-a1b2c3d4e5f6";
        final Integer major = 0;
        final Integer minor = 65535;

        // when
        final RegionDefinition definition = new RegionDefinition(uuid, major, minor);

        // then
        assertEquals(uuid, definition.getUuid());
        assertEquals(major, definition.getMajor());
        assertEquals(minor, definition.getMinor());
    }
}
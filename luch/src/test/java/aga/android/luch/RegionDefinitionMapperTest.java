package aga.android.luch;

import android.bluetooth.le.ScanFilter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, manifest = Config.NONE)
public class RegionDefinitionMapperTest {

    private static final byte[] UUID_ONLY_MASK = {
        0, 0,

        1, 1, 1, 1,
        1, 1,
        1, 1,
        1, 1, 1, 1, 1, 1, 1, 1,

        0, 0,

        0, 0,

        0
    };

    private static final byte[] FULL_MASK = {
        0, 0,

        1, 1, 1, 1,
        1, 1,
        1, 1,
        1, 1, 1, 1, 1, 1, 1, 1,

        1, 1,

        1, 1,

        0
    };

    @Test
    public void testFullRegionDefinitionToScanFilterConversion() {

        // given
        final RegionDefinition definition = new RegionDefinition(
            "e56e1f2c-c756-476f-8323-8d1f9cd245ea", 42819, 55646
        );

        // when
        final ScanFilter scanFilter = RegionDefinitionMapper.asScanFilter(definition);

        // then
        assertEquals(76, scanFilter.getManufacturerId());
        assertArrayEquals(FULL_MASK, scanFilter.getManufacturerDataMask());
        assertArrayEquals(
            new byte[]{
                0x00, 0x00,
                (byte) 0xE5, 0x6E, 0x1F, 0x2C,
                (byte) 0xC7, 0x56,
                0x47, 0x6F,
                (byte) 0x83, 0x23, (byte) 0x8D, 0x1F, (byte) 0x9C, (byte) 0xD2, 0x45, (byte) 0xEA,
                (byte) 0xA7, 0x43,
                (byte) 0xD9, 0x5E,
                0x00
            },
            scanFilter.getManufacturerData()
        );
    }

    @Test
    public void testUuidOnlyRegionDefinitionToScanFilterConversion() {

        // given
        final RegionDefinition definition = new RegionDefinition(
            "e56e1f2c-c756-476f-8323-8d1f9cd245ea"
        );

        // when
        final ScanFilter scanFilter = RegionDefinitionMapper.asScanFilter(definition);

        // then
        assertEquals(76, scanFilter.getManufacturerId());
        assertArrayEquals(UUID_ONLY_MASK, scanFilter.getManufacturerDataMask());
        assertArrayEquals(
            new byte[]{
                0x00, 0x00,
                (byte) 0xE5, 0x6E, 0x1F, 0x2C,
                (byte) 0xC7, 0x56,
                0x47, 0x6F,
                (byte) 0x83, 0x23, (byte) 0x8D, 0x1F, (byte) 0x9C, (byte) 0xD2, 0x45, (byte) 0xEA,
                0x00, 0x00,
                0x00, 0x00,
                0x00
            },
            scanFilter.getManufacturerData()
        );
    }
}
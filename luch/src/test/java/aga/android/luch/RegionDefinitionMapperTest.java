package aga.android.luch;

import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import static aga.android.luch.TestHelpers.createScanResult;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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

    private static final byte[] MATCH_ALL_MASK = new byte[23];

    @Test
    public void testFullRegionDefinitionToScanFilterConversion() {

        // given
        final RegionDefinition definition = new RegionDefinition(
            "E56E1F2C-C756-476F-8323-8D1F9CD245EA", 42819, 55646
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
            "E56E1F2C-C756-476F-8323-8D1F9CD245EA"
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

    @Test
    public void testEmptyListOfRegionDefinitionsIsConvertedToSpecialMatchAllScanFilter() {

        // given
        final List<RegionDefinition> regionDefinitions = Collections.emptyList();

        // when
        final List<ScanFilter> scanFilters = RegionDefinitionMapper.asScanFilters(
            regionDefinitions
        );

        // then
        assertThat(scanFilters, hasSize(1));

        final ScanFilter scanFilter = scanFilters.get(0);

        assertEquals(76, scanFilter.getManufacturerId());
        assertArrayEquals(MATCH_ALL_MASK, scanFilter.getManufacturerDataMask());
    }

    @Test
    public void testScanResultToBeaconConversion()
            throws InvocationTargetException,
                    NoSuchMethodException,
                    InstantiationException,
                    IllegalAccessException {

        // given
        final String bluetoothAddress = "00:11:22:33:FF:EE";
        final String proximityUuid = "E56E1F2C-C756-476F-8323-8D1F9CD245EA";
        final int rssi = -96;
        final int major = 100;
        final int minor = 65520;
        final ScanResult scanResult = createScanResult(
            bluetoothAddress,
            proximityUuid,
            major,
            minor,
            rssi
        );

        // when
        final Beacon beacon = RegionDefinitionMapper.asBeacon(scanResult);

        // then
        assertEquals(bluetoothAddress, beacon.getHardwareAddress());
        assertEquals(rssi, beacon.getRssi());
        assertEquals(proximityUuid, beacon.getUuid());
        assertEquals(major, beacon.getMajor());
        assertEquals(minor, beacon.getMinor());
    }
}
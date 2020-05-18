package aga.android.luch.parsers;

import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import aga.android.luch.Beacon;
import aga.android.luch.Region;

import static aga.android.luch.parsers.BeaconParserTestHelpers.createAltBeaconScanResult;
import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, manifest = Config.NONE)
public class BeaconParserTest {

    private final IBeaconParser parser = BeaconParserFactory.ALTBEACON_PARSER;

    @Test
    public void testAltBeaconHavingAllFieldsIsParsedCorrectly()
        throws InvocationTargetException,
                NoSuchMethodException,
                InstantiationException,
                IllegalAccessException {

        // given
        final String bluetoothAddress = "00:11:22:33:FF:EE";
        final String proximityUuid = "E56E1F2C-C756-476F-8323-8D1F9CD245EA";

        final int major = 100;
        final int minor = 65520;

        final byte rssi = -95;
        final byte data = 0x01;

        final ScanResult scanResult = createAltBeaconScanResult(
            bluetoothAddress,
            new byte[] {(byte) 0xBE, (byte) 0xAC},
            proximityUuid,
            major,
            minor,
            rssi,
            data
        );

        // when
        final Beacon beacon = parser.parse(scanResult);

        // then
        assertEquals(bluetoothAddress, beacon.getHardwareAddress());
        assertEquals(fromString(proximityUuid), beacon.getIdentifierAsUuid(1));
        assertEquals(major, beacon.getIdentifierAsInt(2));
        assertEquals(minor, beacon.getIdentifierAsInt(3));
        assertEquals(rssi, beacon.getIdentifierAsByte(4));
        assertEquals(data, beacon.getIdentifierAsByte(5));
    }

    @Test
    public void testBeaconHavingWrongBeaconTypeIsIgnored()
        throws InvocationTargetException,
                NoSuchMethodException,
                InstantiationException,
                IllegalAccessException {
        // given
        final String bluetoothAddress = "00:11:22:33:FF:EE";
        final String proximityUuid = "E56E1F2C-C756-476F-8323-8D1F9CD245EA";

        final int major = 100;
        final int minor = 65520;

        final byte rssi = -95;
        final byte data = 0x01;

        final ScanResult scanResult = createAltBeaconScanResult(
            bluetoothAddress,
            new byte[] {(byte) 0x25, (byte) 0xDE},
            proximityUuid,
            major,
            minor,
            rssi,
            data
        );

        // when
        final Beacon beacon = parser.parse(scanResult);

        // then
        assertNull(beacon);
    }

    @Test
    public void testScanFilterMatchingAllBeacons() {

        // when
        final List<ScanFilter> scanFilters = parser.asScanFilters(
            Collections.<Region>emptyList()
        );

        // then
        assertEquals(
            singletonList(
                new ScanFilter
                    .Builder()
                    .setManufacturerData(
                        280,
                        new byte[24],
                        new byte[24]
                    )
                    .build()
            ),
            scanFilters
        );
    }

    @Test
    public void testScanFilterMatchingBeaconByUuidOnly() {

        // given
        final UUID proximityUuid = fromString("E56E1F2C-C756-476F-8323-8D1F9CD245EA");

        final Region region = new Region
            .Builder()
            .addNullField()
            .addUuidField(proximityUuid)
            .build();

        // when
        final List<ScanFilter> scanFilters = parser.asScanFilters(
            singletonList(region)
        );

        // then
        assertEquals(
            singletonList(
                new ScanFilter
                    .Builder()
                    .setManufacturerData(
                        280,
                        new byte[] {
                            0x00, 0x00, (byte) 0xE5, 0x6E, 0x1F, 0x2C, (byte) 0xC7, 0x56,
                            0x47, 0x6F, (byte) 0x83, 0x23, (byte) 0x8D, 0x1F, (byte) 0x9C,
                            (byte) 0xD2, 0x45, (byte) 0xEA, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
                        },
                        new byte[] {
                            0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0
                        }
                    )
                    .build()
            ),
            scanFilters
        );
    }

    @Test
    public void testScanFilterMatchingBeaconByUuidAndMajorAndMinor() {

        // given
        final UUID proximityUuid = fromString("E56E1F2C-C756-476F-8323-8D1F9CD245EA");

        final int major = 65535;
        final int minor = 0;

        final Region region = new Region
            .Builder()
            .addNullField()
            .addUuidField(proximityUuid)
            .addIntegerField(major)
            .addIntegerField(minor)
            .build();

        // when
        final List<ScanFilter> scanFilters = parser.asScanFilters(
            singletonList(region)
        );

        // then
        assertEquals(
            singletonList(
                new ScanFilter
                    .Builder()
                    .setManufacturerData(
                        280,
                        new byte[] {
                            0x00, 0x00, (byte) 0xE5, 0x6E, 0x1F, 0x2C, (byte) 0xC7, 0x56,
                            0x47, 0x6F, (byte) 0x83, 0x23, (byte) 0x8D, 0x1F, (byte) 0x9C,
                            (byte) 0xD2, 0x45, (byte) 0xEA, (byte) 0xFF, (byte) 0xFF, 0x00,
                            0x00, 0x00, 0x00
                        },
                        new byte[] {
                            0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0
                        }
                    )
                    .build()
            ),
            scanFilters
        );
    }
}

package aga.android.luch.parsers;

import android.bluetooth.le.ScanResult;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.InvocationTargetException;

import aga.android.luch.Beacon;

import static aga.android.luch.TestHelpers.createAltBeaconScanResult;
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
}

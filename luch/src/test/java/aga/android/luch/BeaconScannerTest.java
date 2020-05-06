package aga.android.luch;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static aga.android.luch.TestHelpers.createScanResult;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, manifest = Config.NONE)
public class BeaconScannerTest {

    @Test(expected = AssertionError.class)
    public void testNegativeBeaconValidityDurationIsNotAccepted() {
        new BeaconScanner
            .Builder()
            .setBeaconExpirationDuration(-1)
            .build();
    }

    @Test
    public void testScannerStartsAndStopsScansViaBleDevice() {
        // given
        final CountingFakeBleDevice bleDevice = new CountingFakeBleDevice();

        final BeaconScanner scanner = new BeaconScanner
            .Builder()
            .setBleDevice(bleDevice)
            .build();

        // when
        scanner.start();
        scanner.stop();
        scanner.start();
        scanner.stop();

        // then
        assertEquals(2, bleDevice.starts);
        assertEquals(2, bleDevice.stops);
    }

    @Test
    public void testScannerRetransmitsBeaconsToIBeaconListener()
            throws InvocationTargetException,
                    NoSuchMethodException,
                    InstantiationException,
                    IllegalAccessException {
        // given
        final RetransmittingFakeBleDevice bleDevice = new RetransmittingFakeBleDevice();

        final RecordingBeaconListener beaconListener = new RecordingBeaconListener();

        final BeaconScanner scanner = new BeaconScanner
            .Builder()
            .setBleDevice(bleDevice)
            .setBeaconListener(beaconListener)
            .build();

        final String bluetoothAddress = "00:11:22:33:FF:EE";
        final String proximityUuid = "E56E1F2C-C756-476F-8323-8D1F9CD245EA";
        final int rssi = -95;
        final int major = 15600;
        final int minor = 395;
        final ScanResult scanResult = createScanResult(
            bluetoothAddress,
            proximityUuid,
            major,
            minor,
            rssi
        );

        // when
        scanner.start();
        bleDevice.transmitScanResult(scanResult);
        scanner.stop();

        // then
        assertEquals(
            singleton(
                new Beacon(proximityUuid, bluetoothAddress, major, minor, rssi)
            ),
            beaconListener.nearbyBeacons
        );
    }

    /**
     * In real life the {@link SystemBleDevice} instance will pass the {@link ScanCallback} instance
     * into the {@link android.bluetooth.le.BluetoothLeScanner} which will later call it with the
     * {@link ScanResult} as an argument. Since we replace the {@link SystemBleDevice} with
     * {@link RetransmittingFakeBleDevice} in tests, we'll just expose the method that allows us to
     * send an instance of {@link ScanResult} into the {@link ScanCallback} provided by
     * {@link BeaconScanner} and validate that the ScanResult -> Beacon conversion works smooth
     * and {@link IBeaconListener} instance is called.
     */
    private static final class RetransmittingFakeBleDevice implements IBleDevice {

        @Nullable
        private ScanCallback scanCallback;

        void transmitScanResult(@NonNull ScanResult scanResult) {
            if (scanCallback != null) {
                scanCallback.onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, scanResult);
            }
        }

        @Override
        public void startScans(@NonNull ScanCallback scanCallback) {
            this.scanCallback = scanCallback;
        }

        @Override
        public void stopScans(@NonNull ScanCallback scanCallback) {
            this.scanCallback = scanCallback;
        }
    }

    private static final class RecordingBeaconListener implements IBeaconListener {

        final Set<Beacon> nearbyBeacons = new HashSet<>();

        @Override
        public void onNearbyBeaconsDetected(@NonNull Set<Beacon> beacons) {
            nearbyBeacons.clear();
            nearbyBeacons.addAll(beacons);
        }
    }

    private static final class CountingFakeBleDevice implements IBleDevice {

        private int starts = 0;
        private int stops = 0;

        @Override
        public void startScans(@NonNull ScanCallback scanCallback) {
            starts++;
        }

        @Override
        public void stopScans(@NonNull ScanCallback scanCallback) {
            stops++;
        }
    }
}
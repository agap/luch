package aga.android.luch;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;

import org.jmock.lib.concurrent.DeterministicScheduler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;

import static aga.android.luch.parsers.BeaconParserTestHelpers.createAltBeaconScanResult;
import static java.util.Arrays.asList;
import static java.util.UUID.fromString;
import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, manifest = Config.NONE)
public class BeaconScannerTest {

    // By using the DeterministicScheduler from jmock we can rely on the virtual time instead of
    // the real time, which makes our tests less error-prone and much faster to run.
    private final DeterministicScheduler executorService = new DeterministicScheduler();

    private final ScanExecutorProvider executorProvider = new ScanExecutorProvider() {
        @Override
        public ScheduledExecutorService provide() {
            return executorService;
        }
    };

    @Test(expected = AssertionError.class)
    public void testNegativeBeaconValidityDurationIsNotAccepted() {
        new BeaconScanner
            .Builder(ApplicationProvider.getApplicationContext())
            .setBeaconExpirationDuration(-1)
            .build();
    }

    @Test
    public void testBeaconScannerCanBeBuiltWithDefaultParameters() {
        new BeaconScanner
            .Builder(ApplicationProvider.getApplicationContext())
            .build();
    }

    @Test
    public void testScannerStartsAndStopsScansViaBleDevice() {
        // given
        final CountingFakeBleDevice bleDevice = new CountingFakeBleDevice();

        final BeaconScanner scanner = new BeaconScanner
            .Builder(ApplicationProvider.getApplicationContext())
            .setBleDevice(bleDevice)
            .setScanTasksExecutor(executorProvider)
            .build();

        // when
        scanner.start();
        executorService.tick(10, TimeUnit.MILLISECONDS);
        scanner.stop();
        executorService.tick(10, TimeUnit.MILLISECONDS);
        scanner.start();
        executorService.tick(10, TimeUnit.MILLISECONDS);
        scanner.stop();
        executorService.tick(10, TimeUnit.MILLISECONDS);

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
            .Builder(ApplicationProvider.getApplicationContext())
            .setBleDevice(bleDevice)
            .setBeaconListener(beaconListener)
            .setScanTasksExecutor(executorProvider)
            .build();

        final String bluetoothAddress = "00:11:22:33:FF:EE";
        final String proximityUuid = "E56E1F2C-C756-476F-8323-8D1F9CD245EA";
        final byte rssi = -95;
        final int major = 15600;
        final int minor = 395;
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
        scanner.start();
        executorService.tick(10, TimeUnit.MILLISECONDS);
        bleDevice.transmitScanResult(scanResult);
        executorService.tick(10, TimeUnit.MILLISECONDS);
        scanner.stop();

        // then
        assertEquals(
            Collections.singleton(
                new Beacon(
                    bluetoothAddress,
                    asList(48812, fromString(proximityUuid), major, minor, rssi, data)
                )
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
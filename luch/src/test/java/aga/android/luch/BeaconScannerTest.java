package aga.android.luch;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.UUID.fromString;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static aga.android.luch.ScanDuration.preciseDuration;
import static aga.android.luch.distance.DistanceCalculatorFactory.getCalculator;
import static aga.android.luch.parsers.BeaconParserTestHelpers.createAltBeaconScanResult;
import static aga.android.luch.parsers.BeaconParserTestHelpers.getBluetoothDevice;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;

import org.hamcrest.Matchers;
import org.jmock.lib.concurrent.DeterministicScheduler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import aga.android.luch.ITimeProvider.TestTimeProvider;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, manifest = Config.NONE)
public class BeaconScannerTest {

    private final String bluetoothAddress = "00:11:22:33:FF:EE";
    private final String proximityUuid = "E56E1F2C-C756-476F-8323-8D1F9CD245EA";
    private final byte rssi = -95;
    private final byte txPower = -105;
    private final int major = 15600;
    private final int minor = 395;
    private final byte data = 0x01;

    private final RetransmittingFakeBleDevice bleDevice = new RetransmittingFakeBleDevice();

    private final RecordingBeaconListener beaconListener = new RecordingBeaconListener();

    private final TestTimeProvider timeProvider = new TestTimeProvider();

    // By using the DeterministicScheduler from jmock we can rely on the virtual time instead of
    // the real time, which makes our tests less error-prone and much faster to run.
    private final DeterministicScheduler executorService = new DeterministicScheduler();

    private final ScanExecutorProvider executorProvider = new ScanExecutorProvider() {
        @Override
        public ScheduledExecutorService provide() {
            return executorService;
        }
    };

    @Before
    public void setup() {
        beaconListener.clear();
        timeProvider.elapsedRealTimeMillis = 0L;
    }

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
        advanceTimeBy(10);

        scanner.stop();
        advanceTimeBy(10);

        scanner.start();
        advanceTimeBy(10);

        scanner.stop();
        advanceTimeBy(10);

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
        final BeaconScanner scanner = new BeaconScanner
            .Builder(ApplicationProvider.getApplicationContext())
            .setBleDevice(bleDevice)
            .setBeaconBatchListener(beaconListener)
            .setScanTasksExecutor(executorProvider)
            .build();

        final ScanResult scanResult = getScanResult();

        // when
        scanner.start();
        advanceTimeBy(10);
        bleDevice.transmitScanResult(scanResult);
        advanceTimeBy(10);
        scanner.stop();

        runUiThreadTasksIncludingDelayedTasks();

        // then
        assertEquals(
            singleton(
                new Beacon(
                    bluetoothAddress,
                    asList(48812, fromString(proximityUuid), major, minor, txPower, data),
                    txPower
                )
            ),
            beaconListener.nearbyBeacons
        );
    }

    @Test
    public void testScannerEvictsOutdatedBeacons()
            throws InvocationTargetException,
                    NoSuchMethodException,
                    InstantiationException,
                    IllegalAccessException {
        // given
        final long beaconExpirationDurationSeconds = 10;
        final long scanRestDurationSeconds = 6;

        final ScanDuration scanDuration = preciseDuration(
            SECONDS.toMillis(scanRestDurationSeconds),
            SECONDS.toMillis(scanRestDurationSeconds)
        );

        final BeaconScanner scanner = new BeaconScanner
            .Builder(ApplicationProvider.getApplicationContext())
            .setBleDevice(bleDevice)
            .setBeaconBatchListener(beaconListener)
            .setBeaconExpirationDuration(beaconExpirationDurationSeconds)
            .setScanDuration(scanDuration)
            .setTimeProvider(timeProvider)
            .setScanTasksExecutor(executorProvider)
            .build();


        final ScanResult scanResult = getScanResult();

        // when
        scanner.start();

        advanceTimeBy(10);
        bleDevice.transmitScanResult(scanResult);
        advanceTimeBy(20);

        runUiThreadTasksIncludingDelayedTasks();

        // then
        // Stage 1 - beacon is delivered
        assertEquals(
            singleton(
                new Beacon(
                    bluetoothAddress,
                    asList(48812, fromString(proximityUuid), major, minor, txPower, data),
                    txPower
                )
            ),
            beaconListener.nearbyBeacons
        );

        // when
        advanceTimeBy(7_000);

        // then
        // Stage 2 - the beacon is still here, since the eviction checks are started after
        // the very first scan and the beacon's validity is 10 seconds.
        assertThat(
            beaconListener.nearbyBeacons,
            not(is(Matchers.<Beacon>empty()))
        );

        // when
        advanceTimeBy(4_000);

        runUiThreadTasksIncludingDelayedTasks();

        // then
        // Stage 3 - the beacon is evicted, since the elapsed time is 11 seconds, which is
        // greater than beacon's validity
        assertEquals(
            Collections.<Beacon>emptySet(),
            beaconListener.nearbyBeacons
        );
    }

    @Test
    public void testScannerRemovesAllBeaconsOnStop()
        throws InvocationTargetException,
            NoSuchMethodException,
            InstantiationException,
            IllegalAccessException {
        // given
        final long beaconExpirationDurationSeconds = 10;
        final long scanRestDurationSeconds = 6;

        final ScanDuration scanDuration = preciseDuration(
            SECONDS.toMillis(scanRestDurationSeconds),
            SECONDS.toMillis(scanRestDurationSeconds)
        );

        final BeaconScanner scanner = new BeaconScanner
            .Builder(ApplicationProvider.getApplicationContext())
            .setBleDevice(bleDevice)
            .setBeaconBatchListener(beaconListener)
            .setBeaconExpirationDuration(beaconExpirationDurationSeconds)
            .setScanDuration(scanDuration)
            .setTimeProvider(timeProvider)
            .setScanTasksExecutor(executorProvider)
            .build();


        final ScanResult scanResult = getScanResult();

        // when
        scanner.start();

        advanceTimeBy(10);
        bleDevice.transmitScanResult(scanResult);
        advanceTimeBy(20);

        // then
        // Stage 1 - beacon is delivered
        assertEquals(
            singleton(
                new Beacon(
                    bluetoothAddress,
                    asList(48812, fromString(proximityUuid), major, minor, txPower, data),
                    txPower
                )
            ),
            beaconListener.nearbyBeacons
        );

        // when
        scanner.stop();
        advanceTimeBy(10);

        // then
        // Stage 2 - beacons are removed
        assertEquals(
            emptySet(),
            beaconListener.nearbyBeacons
        );
    }

    @Test
    public void testScannerInvokesEnterExitCallbacks()
        throws NoSuchMethodException,
            InstantiationException,
            IllegalAccessException,
            InvocationTargetException {
        // given
        final long beaconExpirationDurationSeconds = 10;
        final long scanRestDurationSeconds = 6;

        final ScanDuration scanDuration = preciseDuration(
            SECONDS.toMillis(scanRestDurationSeconds),
            SECONDS.toMillis(scanRestDurationSeconds)
        );

        final BeaconScanner scanner = new BeaconScanner
            .Builder(ApplicationProvider.getApplicationContext())
            .setBleDevice(bleDevice)
            .setBeaconListener(beaconListener)
            .setBeaconExpirationDuration(beaconExpirationDurationSeconds)
            .setScanDuration(scanDuration)
            .setTimeProvider(timeProvider)
            .setScanTasksExecutor(executorProvider)
            .build();

        final ScanResult scanResult = getScanResult();

        // when
        scanner.start();
        advanceTimeBy(10);
        bleDevice.transmitScanResult(scanResult);
        advanceTimeBy(10);

        runUiThreadTasksIncludingDelayedTasks();

        // then
        // Stage 1 - beacon is delivered
        assertEquals(
            singleton(
                new Beacon(
                    bluetoothAddress,
                    asList(48812, fromString(proximityUuid), major, minor, txPower, data),
                    txPower
                )
            ),
            beaconListener.nearbyBeacons
        );

        // when
        advanceTimeBy(7_000);
        bleDevice.transmitScanResult(scanResult);
        advanceTimeBy(10);

        runUiThreadTasksIncludingDelayedTasks();

        // then
        // Stage 2 - the beacon was not redelivered again as it's the same beacon.
        assertThat(
            beaconListener.nearbyBeacons,
            hasSize(1)
        );

        // when
        advanceTimeBy(11_000);

        runUiThreadTasksIncludingDelayedTasks();

        // then
        // Stage 3 - the beacon is evicted, since it was seen at 7_020 millis,
        // elapsed time is 18_030 millis, 18_030 - 7_020 = 11_010 which is greater than
        // beacon's validity time.
        assertEquals(
            Collections.<Beacon>emptySet(),
            beaconListener.nearbyBeacons
        );
    }

    @Test
    public void testScannerReusesExistingBeaconIfItIsPresent()
        throws InvocationTargetException,
            NoSuchMethodException,
            InstantiationException,
            IllegalAccessException {
        // given
        final BeaconScanner scanner = new BeaconScanner
            .Builder(ApplicationProvider.getApplicationContext())
            .setBleDevice(bleDevice)
            .setBeaconBatchListener(beaconListener)
            .setTimeProvider(timeProvider)
            .setScanTasksExecutor(executorProvider)
            .build();

        final ScanResult scanResult = getScanResult();

        // when
        scanner.start();

        advanceTimeBy(1_000);

        bleDevice.transmitScanResult(scanResult);

        advanceTimeBy(1_000);

        runUiThreadTasksIncludingDelayedTasks();

        final Beacon initialBeacon = beaconListener.nearbyBeacons.iterator().next();
        final long initialTimestamp = initialBeacon.getLastSeenAtSystemClock();

        advanceTimeBy(1_000);

        bleDevice.transmitScanResult(scanResult);

        advanceTimeBy(1_000);

        final Beacon updatedBeacon = beaconListener.nearbyBeacons.iterator().next();
        final long updatedTimestamp = updatedBeacon.getLastSeenAtSystemClock();

        scanner.stop();

        // then
        assertEquals(initialBeacon, updatedBeacon);
        assertNotEquals(initialTimestamp, updatedTimestamp);
    }

    @Test
    public void testMalformedScanResultDoesNotCrashBeaconHandler()
        throws InvocationTargetException,
            NoSuchMethodException,
            InstantiationException,
            IllegalAccessException {

        // given
        final BeaconScanner scanner = new BeaconScanner
            .Builder(ApplicationProvider.getApplicationContext())
            .setBleDevice(bleDevice)
            .setBeaconBatchListener(beaconListener)
            .setScanTasksExecutor(executorProvider)
            .build();

        final ScanResult scanResult = new ScanResult(
            getBluetoothDevice("00:11:22:33:FF:EE"),
            null,
            -95,
            0
        );

        // when
        scanner.start();

        advanceTimeBy(10);

        bleDevice.transmitScanResult(scanResult);

        advanceTimeBy(10);

        // then
        assertEquals(
            Collections.emptySet(),
            beaconListener.nearbyBeacons
        );
    }

    @Test
    public void testRangingMode()
        throws NoSuchMethodException,
            InstantiationException,
            IllegalAccessException,
            InvocationTargetException {

        // given
        final BeaconScanner scanner = new BeaconScanner
            .Builder(ApplicationProvider.getApplicationContext())
            .setBleDevice(bleDevice)
            .setBeaconExpirationDuration(15)
            .setBeaconBatchListener(beaconListener)
            .setScanTasksExecutor(executorProvider)
            .setTimeProvider(timeProvider)
            .setRangingEnabled()
            .build();

        final Ranger ranger = scanner.getRanger();

        // when
        scanner.start();

        advanceTimeBy(100);
        bleDevice.transmitScanResult(getScanResult((byte) -95));

        advanceTimeBy(100);
        bleDevice.transmitScanResult(getScanResult((byte) -97));

        advanceTimeBy(100);
        bleDevice.transmitScanResult(getScanResult((byte) -99));

        advanceTimeBy(100);
        bleDevice.transmitScanResult(getScanResult((byte) -90));
        advanceTimeBy(100);

        // then
        // Stage 1 - Ranger has RSSI readings, hence it's running distance calculation
        // over the smoothed RSSI value
        final Beacon beacon = beaconListener.nearbyBeacons.iterator().next();
        assertEquals(0.3162, Objects.requireNonNull(ranger).calculateDistance(beacon), 0.0001);

        // when
        advanceTimeBy(15_000);

        // then
        // Stage 2 - 15 seconds have passed, RSSI values were cleaned up, distance calculation
        // is done over the latest known RSSI value
        assertEquals(0.1778, ranger.calculateDistance(beacon), 0.0001);

        scanner.stop();
    }

    private ScanResult getScanResult()
        throws NoSuchMethodException,
            InstantiationException,
            IllegalAccessException,
            InvocationTargetException {
        return getScanResult(rssi);
    }

    private ScanResult getScanResult(byte rssi)
        throws InvocationTargetException,
            NoSuchMethodException,
            InstantiationException,
            IllegalAccessException {

        return createAltBeaconScanResult(
            bluetoothAddress,
            new byte[] {(byte) 0xBE, (byte) 0xAC},
            proximityUuid,
            major,
            minor,
            rssi,
            txPower,
            data
        );
    }

    private void advanceTimeBy(long millis) {
        timeProvider.elapsedRealTimeMillis = timeProvider.elapsedRealTimeMillis + millis;
        executorService.tick(millis, MILLISECONDS);
    }

    /**
     * In real life the {@link SystemBleDevice} instance will pass the {@link ScanCallback} instance
     * into the {@link android.bluetooth.le.BluetoothLeScanner} which will later call it with the
     * {@link ScanResult} as an argument. Since we replace the {@link SystemBleDevice} with
     * {@link RetransmittingFakeBleDevice} in tests, we'll just expose the method that allows us to
     * send an instance of {@link ScanResult} into the {@link ScanCallback} provided by
     * {@link BeaconScanner} and validate that the ScanResult -> Beacon conversion works smooth
     * and {@link IBeaconBatchListener} instance is called.
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

    private static final class RecordingBeaconListener
        implements IBeaconBatchListener, IBeaconListener {

        final Set<Beacon> nearbyBeacons = new HashSet<>();

        void clear() {
            nearbyBeacons.clear();
        }

        @Override
        public void onBeaconsDetected(@NonNull Collection<Beacon> beacons) {
            nearbyBeacons.clear();
            nearbyBeacons.addAll(beacons);
        }

        @Override
        public void onBeaconEntered(@NonNull Beacon beacon) {
            nearbyBeacons.add(beacon);
        }

        @Override
        public void onBeaconUpdated(@NonNull Beacon beacon) {

        }

        @Override
        public void onBeaconExited(@NonNull Beacon beacon) {
            nearbyBeacons.remove(beacon);
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
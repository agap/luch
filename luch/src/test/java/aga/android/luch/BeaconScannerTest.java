package aga.android.luch;

import android.bluetooth.le.ScanCallback;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import androidx.annotation.NonNull;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, manifest = Config.NONE)
public class BeaconScannerTest {

    @Test
    public void testScannerStartsAndStopsScansViaBleDevice() {
        // given
        final FakeBleDevice bleDevice = new FakeBleDevice();

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

    private static final class FakeBleDevice implements IBleDevice {

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
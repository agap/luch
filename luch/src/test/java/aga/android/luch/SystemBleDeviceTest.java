package aga.android.luch;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.pm.PackageManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.List;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, manifest = Config.NONE)
public class SystemBleDeviceTest {

    private final BluetoothAdapter adapter = mock(BluetoothAdapter.class);

    private final BluetoothLeScanner scanner = mock(BluetoothLeScanner.class);

    private final ScanSettings scanSettings = new ScanSettings.Builder().build();

    private final List<ScanFilter> scanFilters = Collections.emptyList();

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
        }
    };

    private final PackageManager packageManager = getApplicationContext().getPackageManager();

    private final SystemBleDevice device = new SystemBleDevice(
        getApplicationContext(),
        adapter,
        scanSettings,
        scanFilters
    );

    @Test
    public void testScansWillBeStartedIfThereIsBleScannerInBluetoothAdapter() {

        // given
        when(adapter.getBluetoothLeScanner()).thenReturn(scanner);
        when(adapter.isEnabled()).thenReturn(true);
        shadowOf(packageManager).setSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE, true);

        // when
        device.startScans(scanCallback);

        // then
        verify(scanner).startScan(scanFilters, scanSettings, scanCallback);
        verifyNoMoreInteractions(scanner);
    }

    @Test
    public void testScansWillBeStoppedIfThereIsBleScannerInBluetoothAdapter() {

        // given
        when(adapter.getBluetoothLeScanner()).thenReturn(scanner);
        when(adapter.isEnabled()).thenReturn(true);

        // when
        device.stopScans(scanCallback);

        // then
        verify(scanner).stopScan(scanCallback);
        verifyNoMoreInteractions(scanner);
    }

    @Test
    public void testNoExceptionIsThrownWhileTryingToStartScansWhenThereIsNoBleScanner() {
        // given
        when(adapter.getBluetoothLeScanner()).thenReturn(null);
        when(adapter.isEnabled()).thenReturn(true);
        shadowOf(packageManager).setSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE, true);

        // when
        device.startScans(scanCallback);

        // then
        verify(adapter).getBluetoothLeScanner();
        verify(adapter).isEnabled();
        verifyNoMoreInteractions(adapter);
    }

    @Test
    public void testNoExceptionIsThrownWhileTryingToStopScansWhenThereIsNoBleScanner() {
        // given
        when(adapter.getBluetoothLeScanner()).thenReturn(null);
        when(adapter.isEnabled()).thenReturn(true);

        // when
        device.stopScans(scanCallback);

        // then
        verify(adapter).isEnabled();
        verify(adapter).getBluetoothLeScanner();
        verifyNoMoreInteractions(adapter);
    }

    @Test
    public void testNoScansAreStartedIfBluetoothAdapterIsDisabled() {
        // given
        when(adapter.isEnabled()).thenReturn(false);
        shadowOf(packageManager).setSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE, true);

        // when
        device.startScans(scanCallback);

        // then
        verify(adapter).isEnabled();
        verifyNoMoreInteractions(adapter);
    }

    @Test
    public void testNoAttemptToStopScansIsMadeIfBluetoothAdapterIsDisabled() {
        // given
        when(adapter.isEnabled()).thenReturn(false);

        // when
        device.stopScans(scanCallback);

        // then
        verify(adapter).isEnabled();
        verifyNoMoreInteractions(adapter);
    }

    @Test
    public void testNoAttemptToStartScansIfBleFeatureIsMissing() {
        // given
        shadowOf(packageManager).setSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE, false);

        // when
        device.startScans(scanCallback);

        // then
        verifyNoInteractions(scanner);
    }

    @Test
    public void testScannerStartMethodIsNotCrashingTheAppIfItIsRunningInKnoxContainer() {
        // given
        when(adapter.isEnabled()).thenThrow(SecurityException.class);
        shadowOf(packageManager).setSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE, true);

        // when
        device.startScans(scanCallback);

        // then
        verify(adapter).isEnabled();
        verifyNoMoreInteractions(adapter);
    }

    @Test
    public void testScannerStopMethodIsNotCrashingTheAppIfItIsRunningInKnoxContainer() {
        // given
        when(adapter.isEnabled()).thenThrow(SecurityException.class);
        shadowOf(packageManager).setSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE, true);

        // when
        device.stopScans(scanCallback);

        // then
        verify(adapter).isEnabled();
        verifyNoMoreInteractions(adapter);
    }
}
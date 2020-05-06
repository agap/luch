package aga.android.luch;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, manifest = Config.NONE)
public class SystemBleDeviceTest {

    private final BluetoothAdapter adapter = mock(BluetoothAdapter.class);

    private final BluetoothLeScanner scanner = mock(BluetoothLeScanner.class);

    private final ScanSettings scanSettings = new ScanSettings.Builder().build();

    private List<ScanFilter> scanFilters = Collections.emptyList();

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
        }
    };

    private final SystemBleDevice device = new SystemBleDevice(
        adapter,
        scanSettings,
        scanFilters
    );

    @Test
    public void testScansWillBeStartedIfThereIsBleScannerInBluetoothAdapter() {

        // given
        when(adapter.getBluetoothLeScanner()).thenReturn(scanner);
        when(adapter.isEnabled()).thenReturn(true);

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
}
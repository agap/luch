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

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23, manifest = Config.NONE)
public class SystemBleDeviceSdk23Test {
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

    private final SystemBleDevice device = new SystemBleDevice(
        getApplicationContext(),
        adapter,
        scanSettings,
        scanFilters
    );

    @Test
    public void testAttemptToStopScansIsMadeEvenIfAdapterIsDisabled() {
        // given
        when(adapter.getBluetoothLeScanner()).thenReturn(scanner);
        when(adapter.isEnabled()).thenReturn(false);

        // when
        device.stopScans(scanCallback);

        // then
        verify(adapter).getBluetoothLeScanner();
        verifyNoMoreInteractions(adapter);

        verify(scanner).stopScan(scanCallback);
        verifyNoMoreInteractions(scanner);
    }
}
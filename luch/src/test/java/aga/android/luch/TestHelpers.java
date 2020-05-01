package aga.android.luch;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.util.SparseArray;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

import static aga.android.luch.Conversions.integerToByteArray;
import static aga.android.luch.Conversions.uuidStringToByteArray;
import static java.lang.System.arraycopy;

// Unfortunately, both BluetoothDevice and ScanResult's constructors are package-private,
// so let's use some reflection magic to access them since we still need to test the
// ScanResult -> Beacon mapping logic.
class TestHelpers {

    static ScanResult createScanResult(@NonNull String bluetoothAddress,
                                       @NonNull String proximityUuid,
                                       int major,
                                       int minor,
                                       int rssi)
            throws NoSuchMethodException,
            IllegalAccessException,
            InstantiationException,
            InvocationTargetException {
        final BluetoothDevice bluetoothDevice = getBluetoothDevice(bluetoothAddress);

        final ScanRecord record = getScanRecord(proximityUuid, major, minor);

        return new ScanResult(
            bluetoothDevice,
            record,
            rssi,
            769642079079204L
        );
    }

    private static ScanRecord getScanRecord(@NonNull String proximityUuid,
                                            int major,
                                            int minor)
            throws IllegalAccessException,
                InstantiationException,
                InvocationTargetException,
                NoSuchMethodException {
        //noinspection JavaReflectionMemberAccess
        final Constructor<ScanRecord> constructor = ScanRecord.class.getDeclaredConstructor(
            List.class,
            SparseArray.class,
            Map.class,
            int.class,
            int.class,
            String.class,
            byte[].class
        );

        constructor.setAccessible(true);

        final SparseArray<byte[]> manufacturerData = new SparseArray<>();
        final byte[] manufacturerByteArray = new byte[23];

        manufacturerByteArray[0] = 0x02; // data type specification, 0x02 means it's iBeacon
        manufacturerByteArray[1] = 0x15; // the length of remaining data, 21 bytes
        manufacturerByteArray[22] = (byte) 0xB3; // iBeacon’s measured RSSI at a 1-meter distance

        arraycopy(
            uuidStringToByteArray(proximityUuid),
            0,
            manufacturerByteArray,
            2,
            16
        );

        arraycopy(
            integerToByteArray(major),
            0,
            manufacturerByteArray,
            18,
            2
        );

        arraycopy(
            integerToByteArray(minor),
            0,
            manufacturerByteArray,
            20,
            2
        );

        manufacturerData.append(
            76,
            manufacturerByteArray
        );

        return constructor.newInstance(
            Collections.emptyList(), // service solicitation uuids
            manufacturerData , // manufacturer specific data
            Collections.emptyMap(), // service data
            6, // advertise flags
            -2147483648, // tx power level
            null, // local name
            new byte[0] // raw bytes
        );
    }

    private static BluetoothDevice getBluetoothDevice(@NonNull String bluetoothAddress)
            throws IllegalAccessException,
            InvocationTargetException,
            InstantiationException,
            NoSuchMethodException {
        //noinspection JavaReflectionMemberAccess
        final Constructor<BluetoothDevice> constructor = BluetoothDevice.class.getConstructor(
            String.class
        );

        constructor.setAccessible(true);

        return constructor.newInstance(bluetoothAddress);
    }
}

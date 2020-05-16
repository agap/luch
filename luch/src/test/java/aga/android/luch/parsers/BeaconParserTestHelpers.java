package aga.android.luch.parsers;

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

import static aga.android.luch.parsers.Conversions.uuidStringToByteArray;
import static java.lang.System.arraycopy;

// Unfortunately, both BluetoothDevice and ScanResult's constructors are package-private,
// so let's use some reflection magic to access them since we still need to test the
// ScanResult -> Beacon mapping logic.
public class BeaconParserTestHelpers {

    public static ScanResult createAltBeaconScanResult(@NonNull String bluetoothAddress,
                                                       @NonNull byte[] beaconType,
                                                       @NonNull String proximityUuid,
                                                       int major,
                                                       int minor,
                                                       byte rssi,
                                                       byte data)
            throws NoSuchMethodException,
            IllegalAccessException,
            InstantiationException,
            InvocationTargetException {
        final BluetoothDevice bluetoothDevice = getBluetoothDevice(bluetoothAddress);

        final ScanRecord record = getScanRecord(beaconType, proximityUuid, major, minor, rssi, data);

        return new ScanResult(
            bluetoothDevice,
            record,
            rssi,
            769642079079204L
        );
    }

    private static ScanRecord getScanRecord(@NonNull byte[] beaconType,
                                            @NonNull String proximityUuid,
                                            int major,
                                            int minor,
                                            byte rssi,
                                            byte data)
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
        final byte[] manufacturerByteArray = new byte[24];

        manufacturerByteArray[0] = beaconType[0];
        manufacturerByteArray[1] = beaconType[1];
        manufacturerByteArray[22] = rssi; // AltBeacon’s measured RSSI at a 1-meter distance
        manufacturerByteArray[23] = data; // Optional data field

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
            280,
            manufacturerByteArray
        );

        return constructor.newInstance(
            Collections.emptyList(), // service solicitation uuids
            manufacturerData, // manufacturer specific data
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

    private static byte[] integerToByteArray(int value) {
        return new byte[] {
            (byte) (value / 256),
            (byte) (value % 256)
        };
    }
}

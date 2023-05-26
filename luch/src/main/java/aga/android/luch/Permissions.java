package aga.android.luch;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

class Permissions {

    private Permissions() {

    }

    static boolean checkBluetoothScanPermission(@NonNull Context context) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return context.checkSelfPermission(ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
                || context.checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED;
        }

        return context.checkSelfPermission(BLUETOOTH_SCAN) == PERMISSION_GRANTED;
    }
}

package aga.android.luch;

import android.bluetooth.le.ScanCallback;

import androidx.annotation.NonNull;

interface IBleDevice {

    void startScans(@NonNull ScanCallback scanCallback);

    void stopScans(@NonNull ScanCallback scanCallback);
}

package aga.android.luch;

import android.Manifest.permission;

import androidx.annotation.RequiresPermission;

public interface IScanner {

    @RequiresPermission(
        anyOf = {permission.ACCESS_FINE_LOCATION, permission.ACCESS_COARSE_LOCATION}
    )
    void start();

    @RequiresPermission(
        anyOf = {permission.ACCESS_FINE_LOCATION, permission.ACCESS_COARSE_LOCATION}
    )
    void stop();

    Ranger getRanger();
}

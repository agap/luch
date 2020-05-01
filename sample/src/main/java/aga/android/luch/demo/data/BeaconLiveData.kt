package aga.android.luch.demo.data

import aga.android.luch.Beacon
import aga.android.luch.BeaconScanner
import aga.android.luch.IBeaconListener
import aga.android.luch.IScanner
import aga.android.luch.ScanDuration
import android.annotation.SuppressLint
import android.os.Build
import androidx.lifecycle.LiveData
import java.util.concurrent.TimeUnit

class BeaconLiveData : LiveData<Set<Beacon>>() {

    private val listener: IBeaconListener = IBeaconListener { beacons ->
        value = beacons
    }

    private val scanner: IScanner = BeaconScanner.Builder()
        .setBeaconEvictionTime(TimeUnit.SECONDS.toMillis(10))
        .setScanDuration(
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                ScanDuration.preciseDuration(150, 1500)
            } else {
                ScanDuration.UNIFORM
            }
        )
        .setBeaconListener(listener)
        .setRegionDefinitions(RegionsDefinitionSource.getDefinitions())
        .build()

    // Check is suppressed since permission checks should happen on the Fragment/Activity level,
    // not in the LiveData/ViewModel. See MainActivity.
    @SuppressLint("MissingPermission")
    override fun onActive() {
        scanner.start()
    }

    @SuppressLint("MissingPermission")
    override fun onInactive() {
        scanner.stop()
    }
}
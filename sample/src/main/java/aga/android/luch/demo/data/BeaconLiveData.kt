package aga.android.luch.demo.data

import aga.android.luch.Beacon
import aga.android.luch.BeaconScanner
import aga.android.luch.IBeaconListener
import aga.android.luch.IScanner
import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import java.util.concurrent.TimeUnit

class BeaconLiveData : LiveData<Set<Beacon>>() {

    private val listener: IBeaconListener = IBeaconListener { beacons ->
        value = beacons
    }

    private val scanner: IScanner = BeaconScanner.Builder()
        .setBeaconEvictionTime(TimeUnit.SECONDS.toMillis(10))
        .setScanDuration(TimeUnit.SECONDS.toMillis(1))
        .setRestDuration(TimeUnit.SECONDS.toMillis(8))
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
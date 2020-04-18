package aga.android.ibeacon.demo.data

import aga.android.ibeacon.Beacon
import aga.android.ibeacon.BeaconScanner
import aga.android.ibeacon.IBeaconListener
import aga.android.ibeacon.IScanner
import androidx.lifecycle.LiveData
import java.util.concurrent.TimeUnit

class BeaconLiveData : LiveData<Set<Beacon>>() {

    private val listener: IBeaconListener = IBeaconListener { beacons ->
        value = beacons
    }

    private val scanner: IScanner = BeaconScanner.Builder()
        .setBeaconEvictionTime(TimeUnit.SECONDS.toMillis(5))
        .setScanDuration(500)
        .setRestDuration(TimeUnit.SECONDS.toMillis(1))
        .setBeaconListener(listener)
        .setRegionDefinitions(RegionsDefinitionSource.getDefinitions())
        .build()

    override fun onActive() {
        scanner.start()
    }

    override fun onInactive() {
        scanner.stop()
    }
}
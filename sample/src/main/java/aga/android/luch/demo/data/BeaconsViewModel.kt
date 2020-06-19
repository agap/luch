package aga.android.luch.demo.data

import aga.android.luch.Beacon
import aga.android.luch.BeaconScanner
import aga.android.luch.IBeaconBatchListener
import aga.android.luch.IScanner
import aga.android.luch.ScanDuration
import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BeaconsViewModel(application: Application) : AndroidViewModel(application) {

    private val batchListener: IBeaconBatchListener = IBeaconBatchListener { beacons ->
        beaconsLiveData.value = beacons
    }

    val scanner: IScanner = BeaconScanner.Builder(application)
        .setBeaconExpirationDuration(10)
        .setScanDuration(
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                ScanDuration.preciseDuration(150, 1500)
            } else {
                ScanDuration.UNIFORM
            }
        )
        .setRangingEnabled(true)
        .setBeaconBatchListener(batchListener)
        .build()


    private val beaconsLiveData: MutableLiveData<Collection<Beacon>> = BeaconLiveData(scanner)

    val beacons: LiveData<Collection<Beacon>>
        get() = beaconsLiveData
}
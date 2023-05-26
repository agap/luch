package aga.android.luch.demo.data

import aga.android.luch.Beacon
import aga.android.luch.IScanner
import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData

class BeaconLiveData(private val scanner: IScanner) : MutableLiveData<Collection<Beacon>>() {

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
package aga.android.luch.demo.data

import aga.android.luch.Beacon
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class BeaconsViewModel : ViewModel() {

    val beacons: LiveData<Set<Beacon>> = BeaconLiveData()
}
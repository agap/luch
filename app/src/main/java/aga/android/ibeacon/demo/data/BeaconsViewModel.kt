package aga.android.ibeacon.demo.data

import aga.android.ibeacon.Beacon
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class BeaconsViewModel : ViewModel() {

    val beacons: LiveData<Set<Beacon>> = BeaconLiveData()
}
package aga.android.luch.demo.data

import aga.android.luch.Beacon
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class BeaconsViewModel(application: Application) : AndroidViewModel(application) {

    val beacons: LiveData<Set<Beacon>> = BeaconLiveData(application)
}
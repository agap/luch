package aga.android.luch.demo

import aga.android.luch.BeaconLogger
import aga.android.luch.demo.data.BeaconModel
import aga.android.luch.demo.data.BeaconsViewModel
import aga.android.luch.demo.databinding.ActivityMainBinding
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.observe

class MainActivity : AppCompatActivity() {

    private companion object {
        const val PERMISSION_REQUEST_CODE = 100
    }

    private lateinit var binding: ActivityMainBinding

    private val adapter: BeaconsAdapter by lazy { BeaconsAdapter(this) }

    private val model: BeaconsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BeaconLogger.setInstance(BeaconLogger.SYSTEM_INSTANCE)

        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.beaconsList.adapter = adapter

        if (hasLocationPermissions()) {
            startObservation()
        } else {
            requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        }
    }

    private fun startObservation() {
        model.beacons.observe(this) { beacons ->
            adapter.submitList(beacons.toList().map {
                BeaconModel(
                    uuid = it.getIdentifierAsUuid(1).toString(),
                    major = it.getIdentifierAsInt(2),
                    minor = it.getIdentifierAsInt(3),
                    rssi = it.rssi.toInt(),
                    hardwareAddress = it.hardwareAddress,
                    distance = it.distance
                )
            })
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    startObservation()
                } else {
                    Toast
                        .makeText(
                            this,
                            R.string.location_permission_is_required,
                            Toast.LENGTH_SHORT
                        )
                        .show()

                    finish()
                }
            }
        }
    }
}
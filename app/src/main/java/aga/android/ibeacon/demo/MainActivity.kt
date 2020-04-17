package aga.android.ibeacon.demo

import aga.android.ibeacon.Beacon
import aga.android.ibeacon.BeaconScanner
import aga.android.ibeacon.IBeaconListener
import aga.android.ibeacon.IScanner
import aga.android.ibeacon.demo.databinding.ActivityMainBinding
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), IBeaconListener {

    private val scanner: IScanner = BeaconScanner()

    private lateinit var binding: ActivityMainBinding

    private val adapter: BeaconsAdapter by lazy { BeaconsAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.beaconsList.adapter = adapter

        scanner.setRegionDefinitions(RegionsDefinitionSource.getDefinitions(this));
        scanner.setBeaconListener(this)
    }

    override fun onResume() {
        super.onResume()
        scanner.start()
    }

    override fun onPause() {
        super.onPause()
        scanner.stop()
    }

    override fun onNearbyBeaconsDetected(beacons: Set<Beacon>) {
        adapter.submitList(beacons.toList())
    }
}
package aga.android.luch.demo

import aga.android.luch.BeaconLogger
import aga.android.luch.demo.data.BeaconsViewModel
import aga.android.luch.demo.databinding.ActivityMainBinding
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val adapter: BeaconsAdapter by lazy { BeaconsAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BeaconLogger.setInstance(BeaconLogger.SYSTEM_INSTANCE)

        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.beaconsList.adapter = adapter

        val model: BeaconsViewModel by viewModels()

        model.beacons.observe(this) { beacons ->
            adapter.submitList(beacons.toList())
        }
    }
}
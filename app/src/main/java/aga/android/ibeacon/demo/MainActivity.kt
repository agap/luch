package aga.android.ibeacon.demo

import aga.android.ibeacon.demo.data.BeaconsViewModel
import aga.android.ibeacon.demo.databinding.ActivityMainBinding
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

        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.beaconsList.adapter = adapter

        val model: BeaconsViewModel by viewModels()

        model.beacons.observe(this) { beacons ->
            adapter.submitList(beacons.toList())
        }
    }
}
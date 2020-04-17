package aga.android.ibeacon.demo

import aga.android.ibeacon.Beacon
import aga.android.ibeacon.demo.databinding.BeaconItemViewBinding
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class BeaconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val binding = BeaconItemViewBinding.bind(itemView)

    fun setBeacon(beacon: Beacon) {

        with(binding) {
            beaconItemUuid.text = beacon.uuid
            val resources = itemView.context.resources
            beaconItemMajor.text = resources.getString(R.string.beacon_major, beacon.major)
            beaconItemMinor.text = resources.getString(R.string.beacon_minor, beacon.minor)
        }
    }
}

class BeaconDiffCallback: DiffUtil.ItemCallback<Beacon>() {

    override fun areItemsTheSame(oldItem: Beacon, newItem: Beacon): Boolean {
        return oldItem.uuid == newItem.uuid
    }

    override fun areContentsTheSame(oldItem: Beacon, newItem: Beacon): Boolean {
        return oldItem == newItem
    }
}

class BeaconsAdapter(context: Context) : ListAdapter<Beacon, BeaconViewHolder>(BeaconDiffCallback()) {

    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeaconViewHolder {
        return BeaconViewHolder(
            inflater.inflate(R.layout.beacon_item_view, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BeaconViewHolder, position: Int) {
        holder.setBeacon(getItem(position))
    }
}
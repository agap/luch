package aga.android.luch.demo

import aga.android.luch.Beacon
import aga.android.luch.demo.databinding.BeaconItemViewBinding
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
            val resources = itemView.context.resources

            beaconItemUuid.text  = beacon.getIdentifierAsUuid(1).toString()
            beaconItemMajor.text = beacon.getIdentifierAsInt(2).toString()
            beaconItemMinor.text = beacon.getIdentifierAsInt(3).toString()
            beaconItemRssi.text  = resources.getString(
                R.string.beacon_rssi_value, beacon.getIdentifierAsByte(4)
            )

            beaconItemAddress.text = beacon.hardwareAddress
        }
    }
}

class BeaconDiffCallback: DiffUtil.ItemCallback<Beacon>() {

    override fun areItemsTheSame(oldItem: Beacon, newItem: Beacon): Boolean {
        return oldItem.getIdentifierAsUuid(1) == newItem.getIdentifierAsUuid(1)
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
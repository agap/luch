package aga.android.luch.demo

import aga.android.luch.demo.data.BeaconModel
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

    fun setBeacon(beacon: BeaconModel) {

        with(binding) {
            val resources = itemView.context.resources

            beaconItemUuid.text  = beacon.uuid
            beaconItemMajor.text = beacon.major.toString()
            beaconItemMinor.text = beacon.minor.toString()
            beaconItemRssi.text  = resources.getString(
                R.string.beacon_rssi_value, beacon.rssi
            )

            beaconItemAddress.text = beacon.hardwareAddress

            beaconDistance.text = resources.getString(
                R.string.beacon_distance_value, beacon.distance
            )
        }
    }
}

class BeaconDiffCallback: DiffUtil.ItemCallback<BeaconModel>() {

    override fun areItemsTheSame(oldItem: BeaconModel, newItem: BeaconModel): Boolean {
        return oldItem.uuid == newItem.uuid
    }

    override fun areContentsTheSame(oldItem: BeaconModel, newItem: BeaconModel): Boolean {
        return oldItem == newItem
    }
}

class BeaconsAdapter(context: Context) : ListAdapter<BeaconModel, BeaconViewHolder>(BeaconDiffCallback()) {

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
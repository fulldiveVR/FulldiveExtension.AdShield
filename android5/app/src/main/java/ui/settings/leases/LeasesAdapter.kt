package ui.settings.leases

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.OnClickListener
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import model.Lease
import org.adshield.R

class LeasesAdapter(private val interaction: Interaction) :
    ListAdapter<Lease, LeasesAdapter.LeaseViewHolder>(LeaseDC()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LeaseViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lease, parent, false), interaction
    )

    override fun onBindViewHolder(holder: LeaseViewHolder, position: Int) =
        holder.bind(getItem(position))

    fun swapData(data: List<Lease>) {
        submitList(data.toMutableList())
    }

    inner class LeaseViewHolder(
        itemView: View,
        private val interaction: Interaction
    ) : RecyclerView.ViewHolder(itemView), OnClickListener {

        private val name: TextView = itemView.findViewById(R.id.lease_name)
        private val deleteButton: View = itemView.findViewById(R.id.lease_delete)
        private val thisDevice: View = itemView.findViewById(R.id.lease_thisdevice)

        init {
            deleteButton.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            if (adapterPosition == RecyclerView.NO_POSITION) return
            val clicked = getItem(adapterPosition)
            interaction.onDelete(clicked)
            itemView.alpha = 0.5f
        }

        fun bind(item: Lease) = with(itemView) {
            name.text = item.niceName()
            if (interaction.isThisDevice(item)) {
                thisDevice.visibility = View.VISIBLE
                deleteButton.visibility = View.GONE
            } else {
                thisDevice.visibility = View.GONE
                deleteButton.visibility = View.VISIBLE
            }
        }
    }

    interface Interaction {
        fun onDelete(lease: Lease)
        fun isThisDevice(lease: Lease): Boolean
    }

    private class LeaseDC : DiffUtil.ItemCallback<Lease>() {
        override fun areItemsTheSame(
            oldItem: Lease,
            newItem: Lease
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Lease,
            newItem: Lease
        ): Boolean {
            return oldItem == newItem
        }
    }
}
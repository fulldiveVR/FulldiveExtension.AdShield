/*
 * This file is part of Blokada.
 *
 * Blokada is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Blokada is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Blokada.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright © 2020 Blocka AB. All rights reserved.
 *
 * @author Karol Gusak (karol@blocka.net)
 */

package ui.stats

import android.content.Context
import android.text.format.DateUtils
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import model.HistoryEntry
import model.HistoryEntryType
import org.blokada.R
import ui.StatsViewModel
import java.util.*

class StatsAdapter(
    private val viewModel: StatsViewModel,
    private val interaction: Interaction? = null
) :
    ListAdapter<HistoryEntry, StatsAdapter.ActivityViewHolder>(HistoryEntryDC()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ActivityViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false), interaction
    )

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) =
        holder.bind(getItem(position))

    fun swapData(data: List<HistoryEntry>) {
        submitList(data.toMutableList())
    }

    inner class ActivityViewHolder(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView), OnClickListener {

        private val icon: ImageView = itemView.findViewById(R.id.activity_icon)
        private val iconCounter: TextView = itemView.findViewById(R.id.activity_iconcounter)
        private val name: TextView = itemView.findViewById(R.id.activity_name)
        private val time: TextView = itemView.findViewById(R.id.activity_date)
        private val counter: TextView = itemView.findViewById(R.id.activity_counter)
        private val modified: View = itemView.findViewById(R.id.activity_modified)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (adapterPosition == RecyclerView.NO_POSITION) return
            val clicked = getItem(adapterPosition)
            interaction?.onClick(clicked)
        }

        fun bind(item: HistoryEntry) = with(itemView) {
            when(item.type) {
                HistoryEntryType.passed_allowed -> {
                    icon.setImageResource(R.drawable.ic_shield_off_outline)
                    icon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.green))
                    iconCounter.visibility = View.GONE
                    counter.text = getBlockedAllowedString(context, counter = item.requests, blocked = false)
                }
                HistoryEntryType.blocked_denied -> {
                    icon.setImageResource(R.drawable.ic_shield_off_outline)
                    icon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.red))
                    iconCounter.visibility = View.GONE
                    counter.text = getBlockedAllowedString(context, counter = item.requests, blocked = true)
                }
                HistoryEntryType.passed -> {
                    icon.setImageResource(R.drawable.ic_shield_outline)
                    icon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.green))
                    iconCounter.visibility = View.VISIBLE
                    counter.text = getBlockedAllowedString(context, counter = item.requests, blocked = false)
                }
                else -> {
                    icon.setImageResource(R.drawable.ic_shield_outline)
                    icon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.red))
                    iconCounter.visibility = View.VISIBLE
                    counter.text = getBlockedAllowedString(context, counter = item.requests, blocked = true)
                }
            }

            // Modified state
            val isOnCustomLists = viewModel.isAllowed(item.name) || viewModel.isDenied(item.name)
            val listApplied = item.type in listOf(HistoryEntryType.blocked_denied, HistoryEntryType.passed_allowed)
            if (isOnCustomLists xor listApplied) {
                modified.visibility = View.VISIBLE
                itemView.alpha = 0.5f
            } else {
                modified.visibility = View.GONE
                itemView.alpha = 1.0f
            }

            iconCounter.text = item.requests.toString() // TODO: max val
            name.text = item.name
            time.text = DateUtils.getRelativeTimeSpanString(item.time.time, Date().time, 0)
        }

        private fun getBlockedAllowedString(context: Context, counter: Int, blocked: Boolean): String {
            val base = context.getString(
                if (blocked) R.string.activity_state_blocked
                else R.string.activity_state_allowed
            )
            val times = if (counter == 1) context.getString(R.string.activity_happened_one_time)
                else context.getString(R.string.activity_happened_many_times, counter.toString())
            return "$base $times"
        }
    }

    interface Interaction {
        fun onClick(item: HistoryEntry)
    }

    private class HistoryEntryDC : DiffUtil.ItemCallback<HistoryEntry>() {
        override fun areItemsTheSame(
            oldItem: HistoryEntry,
            newItem: HistoryEntry
        ): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(
            oldItem: HistoryEntry,
            newItem: HistoryEntry
        ): Boolean {
            return oldItem == newItem
        }
    }
}
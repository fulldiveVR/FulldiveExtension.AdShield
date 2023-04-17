/*
 * This file is part of Blokada.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright © 2022 Blocka AB. All rights reserved.
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
import org.adshield.R
import ui.StatsViewModel
import java.lang.Integer.min
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

//        private val icon: ImageView = itemView.findViewById(R.id.activity_icon)
        private val iconCounter: TextView = itemView.findViewById(R.id.activity_iconcounter)
        private val name: TextView = itemView.findViewById(R.id.activity_name)
        private val action: TextView = itemView.findViewById(R.id.activity_action)
        private val time: TextView = itemView.findViewById(R.id.activity_date)
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
                    iconCounter.visibility = View.VISIBLE
                    action.setText(R.string.activity_forwarded)
                    action.setTextColor(ContextCompat.getColor(context,R.color.textColorForwarded))
                }
                HistoryEntryType.blocked_denied -> {
                    iconCounter.visibility = View.VISIBLE
                    action.setText(R.string.activity_regular)
                    action.setTextColor(ContextCompat.getColor(context,R.color.textColorDenied))
                }
                HistoryEntryType.passed -> {
                    iconCounter.visibility = View.VISIBLE
                    action.setText(R.string.activity_regular)
                    action.setTextColor(ContextCompat.getColor(context,R.color.textColorDenied))
                }
                else -> {
                    iconCounter.visibility = View.VISIBLE
                    action.setText(R.string.activity_forwarded)
                    action.setTextColor(ContextCompat.getColor(context,R.color.textColorForwarded))
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

            iconCounter.text = min(99, item.requests).toString()
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
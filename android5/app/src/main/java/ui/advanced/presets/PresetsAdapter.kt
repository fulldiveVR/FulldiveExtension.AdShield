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

package ui.advanced.presets

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.adshield.R

class PresetsAdapter(private val interaction: Interaction? = null) :
    ListAdapter<PacksPreset, PresetsAdapter.PackPresetViewHolder>(PackDC()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PackPresetViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pack_preset, parent, false), interaction
    )

    override fun onBindViewHolder(holder: PackPresetViewHolder, position: Int) =
        holder.bind(getItem(position))

    fun swapData(data: List<PacksPreset>) {
        submitList(data)
    }

    inner class PackPresetViewHolder(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView), OnClickListener {

        private val title: TextView = itemView.findViewById(R.id.pack_title)
        private val slugline: TextView = itemView.findViewById(R.id.pack_slugline)


        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (adapterPosition == RecyclerView.NO_POSITION) return
            val clicked = getItem(adapterPosition)
            interaction?.onClick(clicked)
        }

        fun bind(item: PacksPreset) = with(itemView) {
            title.text = itemView.context.getText(item.titleRes)
            slugline.text = itemView.context.getText(item.descriptionRes)
        }
    }

    interface Interaction {
        fun onClick(pack: PacksPreset)
    }

    private class PackDC : DiffUtil.ItemCallback<PacksPreset>() {
        override fun areItemsTheSame(
            oldItem: PacksPreset,
            newItem: PacksPreset
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: PacksPreset,
            newItem: PacksPreset
        ): Boolean {
            return oldItem == newItem
        }
    }
}
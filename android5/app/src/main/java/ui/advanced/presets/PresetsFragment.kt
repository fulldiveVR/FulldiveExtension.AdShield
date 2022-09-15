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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.adshield.R
import ui.advanced.packs.PacksViewModel
import ui.app

class PresetsFragment : Fragment() {

    private lateinit var vm: PacksViewModel
    private lateinit var adapter: PresetsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            vm = ViewModelProvider(it.app()).get(PacksViewModel::class.java)
        }

        val root = inflater.inflate(R.layout.fragment_packs_presets, container, false)
        val recycler: RecyclerView = root.findViewById(R.id.pack_recyclerview)

        vm.packs.observe(viewLifecycleOwner) { packs ->
            adapter = PresetsAdapter(interaction = object : PresetsAdapter.Interaction {

                override fun onClick(preset: PacksPreset) {
                    vm.onPresetSelected(packs, preset)
                    val nav = findNavController()
                    nav.popBackStack()
                    Toast.makeText(
                        context,
                        getString(R.string.str_presets_settings_toast, getText(preset.titleRes)),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
            recycler.adapter = adapter
            recycler.layoutManager = LinearLayoutManager(context)

            vm.packsPresets.observe(viewLifecycleOwner, Observer {
                adapter.swapData(it)
            })
        }

        return root
    }
}
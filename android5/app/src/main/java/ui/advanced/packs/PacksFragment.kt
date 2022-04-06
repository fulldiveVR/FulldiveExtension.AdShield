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

package ui.advanced.packs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import model.Pack
import org.adshield.R
import ui.app
import ui.utils.Tab
import ui.utils.TabLayout

class PacksFragment : Fragment() {

    private lateinit var vm: PacksViewModel
    private lateinit var tabLayout: TabLayout

    private val indicators by lazy {
        listOf(
            R.drawable.tab_indicator_0,
            R.drawable.tab_indicator_1,
            R.drawable.tab_indicator_2
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            vm = ViewModelProvider(it.app()).get(PacksViewModel::class.java)
        }

        val root = inflater.inflate(R.layout.fragment_packs, container, false)
        val recycler: RecyclerView = root.findViewById(R.id.pack_recyclerview)
        tabLayout = root.findViewById(R.id.tabLayout)

        val adapter = PacksAdapter(interaction = object : PacksAdapter.Interaction {

            override fun onClick(pack: Pack) {
                val nav = findNavController()
                nav.navigate(PacksFragmentDirections.actionNavigationPacksToPackDetailFragment(pack.id))
            }

            override fun onSwitch(pack: Pack, on: Boolean) {
                if (on) {
                    vm.install(pack)
                } else {
                    vm.uninstall(pack)
                }
            }

        })

        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(context)

        vm.packs.observe(viewLifecycleOwner, Observer {
            adapter.swapData(it)
        })

        // Needed for dynamic translation
        tabLayout.getTabAt(0)?.setTabText(getString(R.string.pack_category_highlights))
        tabLayout.getTabAt(1)?.setTabText(getString(R.string.pack_category_active))
        tabLayout.getTabAt(2)?.setTabText(getString(R.string.pack_category_all))

        when (vm.getFilter()) {
            PacksViewModel.Filter.ACTIVE -> tabLayout.selectTab(tabLayout.getTabAt(1))
            PacksViewModel.Filter.ALL -> tabLayout.selectTab(tabLayout.getTabAt(2))
            else -> tabLayout.selectTab(tabLayout.getTabAt(0))
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabUnselected(tab: Tab) {
                tab.updateSize()
                tab.updateFont()
            }

            override fun onTabSelected(tab: Tab) {
                tabLayout.setSelectedTabIndicator(indicators[tab.position % indicators.size])
                tab.updateSize()
                tab.updateFont()
                val filtering = when (tab.position) {
                    0 -> PacksViewModel.Filter.HIGHLIGHTS
                    1 -> PacksViewModel.Filter.ACTIVE
                    else -> PacksViewModel.Filter.ALL
                }
                vm.filter(filtering)
            }

            override fun onTabReselected(tab: Tab) = Unit
        })

        return root
    }
}
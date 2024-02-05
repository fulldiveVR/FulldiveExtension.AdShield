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

import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fulldive.wallet.extensions.unsafeLazy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.HistoryEntry
import org.adshield.R
import service.AlertDialogService
import ui.StatsViewModel
import ui.app
import ui.utils.Tab
import ui.utils.TabLayout


class StatsFragment : Fragment() {

    private lateinit var vm: StatsViewModel

    private lateinit var searchGroup: ViewGroup
    private lateinit var searchView: SearchView

    private val indicators by unsafeLazy {
        listOf(
            R.drawable.tab_indicator_0,
            R.drawable.tab_indicator_1
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        activity?.let {
            vm = ViewModelProvider(it.app()).get(StatsViewModel::class.java)
        }

        val root = inflater.inflate(R.layout.fragment_stats, container, false)

        searchGroup = root.findViewById(R.id.activity_searchgroup)
        searchGroup.visibility = View.GONE

        val adapter = StatsAdapter(vm, interaction = object : StatsAdapter.Interaction {
            override fun onClick(item: HistoryEntry) {
                val nav = findNavController()
                nav.navigate(
                    StatsFragmentDirections.actionNavigationStatsToActivityDetailFragment(
                        item.name
                    )
                )
            }
        })

        val layoutManager = LinearLayoutManager(context)
        val recyclerView: RecyclerView = root.findViewById(R.id.activity_recyclerview)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager

        val tabLayout: TabLayout = root.findViewById(R.id.activity_tabs)

        // Needed for dynamic translation
        tabLayout.getTabAt(0)?.setTabText(getString(R.string.activity_category_recent))
        tabLayout.getTabAt(1)?.setTabText(getString(R.string.activity_category_top))

        when (vm.getSorting()) {
            StatsViewModel.Sorting.TOP -> tabLayout.selectTab(tabLayout.getTabAt(1))
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

                val sorting = when (tab.position) {
                    0 -> StatsViewModel.Sorting.RECENT
                    else -> StatsViewModel.Sorting.TOP
                }
                vm.sort(sorting)
                recyclerView.scrollToTop()
            }

            override fun onTabReselected(tab: Tab) = Unit
        })

        val updateTabsAndFilter = {
            when (vm.getFilter()) {
                StatsViewModel.Filter.ALLOWED -> {
                    tabLayout.getTabAt(1)?.setTabText(R.string.activity_category_top_allowed)
//                    filter.setColorFilter(requireContext().getColorFromAttr(android.R.attr.colorPrimary))
                }
                StatsViewModel.Filter.BLOCKED -> {
                    tabLayout.getTabAt(1)?.setTabText(R.string.activity_category_top_blocked)
//                    filter.setColorFilter(requireContext().getColorFromAttr(android.R.attr.colorPrimary))
                }
                else -> {
                    tabLayout.getTabAt(1)?.setTabText(R.string.activity_category_top)
//                    filter.setColorFilter(null)
                }
            }
        }

        searchView = root.findViewById(R.id.activity_search)
        searchView.setOnClickListener {
            searchView.isIconified = false
            searchView.requestFocus()
        }
        searchView.setOnCloseListener {
            searchGroup.visibility = View.GONE
            true
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(term: String): Boolean {
                return false
            }

            override fun onQueryTextChange(term: String): Boolean {
                if (term.isNotBlank()) {
                    vm.search(term.trim())
                } else {
                    vm.search(null)
                }
                return true
            }

        })

        val empty: View = root.findViewById(R.id.activity_empty)

        vm.history.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) empty.visibility = View.GONE
            adapter.swapData(it)
            updateTabsAndFilter()
        }

        lifecycleScope.launch {
            // Let the user see as the stats refresh
            delay(1000)
            vm.refresh()
        }

        return root
    }

    private fun RecyclerView.scrollToTop() {
        lifecycleScope.launch {
            delay(1000) // Just Android things
            smoothScrollToPosition(0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.stats_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.stats_search -> {
                if (vm.stats.value?.entries?.isEmpty() == true) {
                    // Ignore action when empty
                } else if (searchGroup.visibility == View.GONE) {
                    searchGroup.visibility = View.VISIBLE
                    searchView.isIconified = false
                    searchView.requestFocus()
                } else {
                    searchGroup.visibility = View.GONE
                }
                true
            }
            R.id.stats_filter -> {
                val fragment = StatsFilterFragment.newInstance()
                fragment.show(parentFragmentManager, null)
                true
            }
            R.id.stats_clear -> {
                AlertDialogService.showAlert(getString(R.string.universal_status_confirm),
                    title = getString(R.string.universal_action_clear),
                    positiveAction = getString(R.string.universal_action_yes) to {
                        vm.clear()
                    })
                true
            }
            else -> false
        }
    }
}

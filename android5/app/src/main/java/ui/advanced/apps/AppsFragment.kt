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

package ui.advanced.apps

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fulldive.wallet.extensions.unsafeLazy
import model.App
import org.adshield.R
import service.AlertDialogService
import ui.utils.Tab
import ui.utils.TabLayout
import utils.getColorFromAttr

class AppsFragment : Fragment() {

    private lateinit var vm: AppsViewModel

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
        activity?.run {
            vm = ViewModelProvider(this)[AppsViewModel::class.java]
        }

        val root = inflater.inflate(R.layout.fragment_apps, container, false)

        searchGroup = root.findViewById(R.id.app_searchgroup)
        searchGroup.visibility = View.GONE

        val adapter = AppsAdapter(interaction = object : AppsAdapter.Interaction {
            override fun onClick(item: App) {
                vm.switchBypass(item.id)
            }
        })

        val layoutManager = LinearLayoutManager(context)
        val recyclerView: RecyclerView = root.findViewById(R.id.app_recyclerview)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager

        val tabLayout: TabLayout = root.findViewById(R.id.app_tabs)

        // Needed for dynamic translation
        tabLayout.getTabAt(0)?.setTabText(R.string.apps_label_installed)
        tabLayout.getTabAt(1)?.setTabText(R.string.apps_label_system)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabUnselected(tab: Tab) {
                tab.updateSize()
                tab.updateFont()
            }

            override fun onTabSelected(tab: Tab) {
                tabLayout.setSelectedTabIndicator(indicators[tab.position % indicators.size])
                tab.updateSize()
                tab.updateFont()

                val group = when (tab.position) {
                    0 -> AppsViewModel.Group.INSTALLED
                    else -> AppsViewModel.Group.SYSTEM
                }
                vm.showGroup(group)
            }

            override fun onTabReselected(tab: Tab) = Unit
        })

        val updateTabsAndFilter = { updateFilterIcon() }

        searchView = root.findViewById(R.id.app_search)
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
                if (term.isNotBlank()) vm.search(term.trim())
                else vm.search(null)
                return true
            }

        })

        vm.apps.observe(viewLifecycleOwner) {
            adapter.swapData(it)
            updateTabsAndFilter()
        }

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.stats_menu, menu)
        filterMenuItemDrawable = menu.findItem(R.id.stats_filter).icon
        updateFilterIcon()
        super.onCreateOptionsMenu(menu, inflater)
    }

    var filterMenuItemDrawable: Drawable? = null

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.stats_search -> {
                if (searchGroup.visibility == View.GONE) {
                    searchGroup.visibility = View.VISIBLE
                    searchView.isIconified = false
                    searchView.requestFocus()
                } else {
                    searchGroup.visibility = View.GONE
                }
                true
            }
            R.id.stats_filter -> {
                val fragment = AppsFilterFragment.newInstance()
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

    private fun updateFilterIcon() {
        when (vm.getFilter()) {
            AppsViewModel.Filter.BYPASSED -> {
                filterMenuItemDrawable?.colorFilter = PorterDuffColorFilter(
                    requireContext().getColorFromAttr(android.R.attr.colorPrimary),
                    PorterDuff.Mode.SRC_IN
                )
            }
            AppsViewModel.Filter.NOT_BYPASSED -> {
                filterMenuItemDrawable?.colorFilter = PorterDuffColorFilter(
                    requireContext().getColorFromAttr(android.R.attr.colorPrimary),
                    PorterDuff.Mode.SRC_IN
                )
            }
            else -> {
                filterMenuItemDrawable?.colorFilter = null
            }
        }
    }
}

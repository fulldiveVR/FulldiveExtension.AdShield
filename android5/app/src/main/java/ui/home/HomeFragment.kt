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

package ui.home

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.*
import org.adshield.R
import service.AlertDialogService
import service.EnvironmentService
import service.UpdateService
import ui.AccountViewModel
import ui.AdsCounterViewModel
import ui.TunnelViewModel
import ui.app
import ui.settings.SettingsFragmentDirections
import ui.utils.getColorFromAttr
import utils.Links
import utils.withBoldSections

class HomeFragment : Fragment() {

    private val alert = AlertDialogService
    private lateinit var vm: TunnelViewModel
    private lateinit var accountVM: AccountViewModel
    private lateinit var adsCounterVm: AdsCounterViewModel

    private lateinit var powerButton: PowerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            vm = ViewModelProvider(it.app()).get(TunnelViewModel::class.java)
            accountVM = ViewModelProvider(it.app()).get(AccountViewModel::class.java)
            adsCounterVm = ViewModelProvider(it.app()).get(AdsCounterViewModel::class.java)
        }

        val root = inflater.inflate(R.layout.fragment_home, container, false)

        powerButton = root.findViewById(R.id.home_powerview)

        val longStatus: TextView = root.findViewById(R.id.home_longstatus)
        val updateLongStatus = { s: TunnelStatus, counter: Long? ->
            longStatus.text = when {
                s.inProgress -> getString(R.string.home_status_detail_progress)
                s.active && s.gatewayId != null && counter == null -> {
                    (
                            getString(R.string.home_status_detail_active) + "\n" +
                                    getString(R.string.home_status_detail_plus)
                            ).withBoldSections(requireContext().getColorFromAttr(R.attr.colorRingPlus1))
                }
                s.active && EnvironmentService.isSlim() -> {
                    getString(R.string.home_status_detail_active_slim)
                }
                s.active && counter == null -> {
                    getString(R.string.home_status_detail_active)
                        .withBoldSections(requireContext().getColorFromAttr(R.attr.colorRingLibre1))
                }
                s.active && s.gatewayId != null -> {
                    (
                            getString(
                                R.string.home_status_detail_active_with_counter,
                                counter.toString()
                            ) + "\n" +
                                    getString(R.string.home_status_detail_plus)
                            ).withBoldSections(requireContext().getColorFromAttr(R.attr.colorRingPlus1))
                }
                s.active -> {
                    getString(R.string.home_status_detail_active_with_counter, counter.toString())
                        .withBoldSections(requireContext().getColorFromAttr(R.attr.colorRingLibre1))
                }
                else -> getString(R.string.home_action_tap_to_activate)
            }

            longStatus.setOnClickListener {
                when {
                    s.inProgress -> Unit
                    s.error != null -> Unit
                    s.active -> {
                        val fragment = ProtectionLevelFragment.newInstance()
                        fragment.show(parentFragmentManager, null)
                    }
                    else -> vm.turnOn()
                }
            }
        }

        vm.tunnelStatus.observe(viewLifecycleOwner, Observer { s ->
            powerButton.cover = !s.inProgress && !s.active
            powerButton.loading = s.inProgress
            powerButton.blueMode = s.active
            powerButton.orangeMode = !s.inProgress && s.gatewayId != null
            powerButton.isEnabled = !s.inProgress

            powerButton.setOnClickListener {
                when {
                    s.inProgress -> Unit
                    s.error != null -> Unit
                    s.active -> {
                        vm.turnOff()
                        adsCounterVm.roll()
                    }
                    else -> vm.turnOn()
                }
            }

            val status: TextView = root.findViewById(R.id.home_status)
            status.text = when {
                s.inProgress -> "..."
                s.active -> getString(R.string.home_status_active).toUpperCase()
                else -> getString(R.string.home_status_deactivated).toUpperCase()
            }

            updateLongStatus(s, adsCounterVm.counter.value?.let {
                if (it == 0L) null else it
            })

            when {
                s.error == null -> Unit
                s.error is NoPermissions -> showVpnPermsSheet()
                else -> showFailureDialog(s.error)
            }

            // Only after first init, to not animate on fragment creation
            powerButton.animate = true
        })

        adsCounterVm.counter.observe(viewLifecycleOwner, Observer { counter ->
            vm.tunnelStatus.value?.let { s ->
                updateLongStatus(s, counter)
            }
        })

        accountVM.account.observe(viewLifecycleOwner, Observer { account ->

            if (!account.isActive()) {
                setHasOptionsMenu(true)
            }
        })

        lifecycleScope.launchWhenCreated {
            delay(1000)
            UpdateService.handleUpdateFlow(
                onOpenDonate = {
                    val nav = findNavController()
                    nav.navigate(R.id.navigation_home)
                    nav.navigate(
                        HomeFragmentDirections.actionNavigationHomeToWebFragment(
                            Links.donate, getString(R.string.universal_action_donate)
                        )
                    )
                },
                onOpenMore = {
                    // Display the thank you page
                    val nav = findNavController()
                    nav.navigate(R.id.navigation_home)
                    nav.navigate(
                        HomeFragmentDirections.actionNavigationHomeToWebFragment(
                            Links.updated, getString(R.string.update_label_updated)
                        )
                    )
                }
            )
        }
        return root
    }

    override fun onResume() {
        super.onResume()
        powerButton.start()
    }

    override fun onPause() {
        powerButton.stop()
        super.onPause()
    }

    private fun showVpnPermsSheet() {
        val fragment = AskVpnProfileFragment.newInstance()
        fragment.show(parentFragmentManager, null)
    }

    private fun showLocationSheet() {
        val fragment = LocationFragment.newInstance()
        fragment.show(parentFragmentManager, null)
    }

    private fun showPlusSheet() {
        val fragment = PaymentFragment.newInstance()
        fragment.show(parentFragmentManager, null)
    }

    private fun showFailureDialog(ex: BlokadaException) {
        val additional: Pair<String, () -> Unit>? =
            if (shouldShowKbLink(ex)) getString(R.string.universal_action_learn_more) to {
                val nav = findNavController()
                nav.navigate(
                    HomeFragmentDirections.actionNavigationHomeToWebFragment(
                        Links.tunnelFailure, getString(R.string.universal_action_learn_more)
                    )
                )
                Unit
            }
            else null

        alert.showAlert(
            message = mapErrorToUserFriendly(ex),
            onDismiss = {
                vm.setInformedUserAboutError()
            },
            additionalAction = additional
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home_donate -> {
                val nav = findNavController()
                nav.navigate(R.id.navigation_settings)
                nav.navigate(
                    SettingsFragmentDirections.actionNavigationSettingsToWebFragment(
                        Links.donate, getString(R.string.universal_action_donate)
                    )
                )
                true
            }
            else -> false
        }
    }

}
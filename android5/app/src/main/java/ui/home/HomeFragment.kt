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

import analytics.TrackerConstants
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import appextension.StatisticHelper
import appextension.getColorCompat
import appextension.getDrawableCompat
import com.fulldive.wallet.presentation.base.subscription.SubscriptionService
import com.fulldive.wallet.presentation.base.subscription.SubscriptionSuccessDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import model.*
import org.adshield.MobileNavigationDirections
import org.adshield.R
import service.AlertDialogService
import service.ContextService
import service.EnvironmentService
import service.UpdateService
import ui.*
import ui.settings.SettingsFragmentDirections
import utils.Links
import utils.getColorFromAttr
import utils.withBoldSections
import java.util.*

class HomeFragment : Fragment() {

    private val alert = AlertDialogService
    private lateinit var vm: TunnelViewModel
    private lateinit var accountVM: AccountViewModel
    private lateinit var adsCounterVm: AdsCounterViewModel
    private lateinit var appSettingsVm: AppSettingsViewModel
    private lateinit var settingsVM: SettingsViewModel

    private lateinit var activateView: ActivateView
    private lateinit var statusTextView: TextView
    private lateinit var longStatusTextView: TextView
    private lateinit var limitedOfferLayout: FrameLayout
    private lateinit var closePopupButton: ImageView
    private lateinit var legalStateDescriptionTextView: TextView
    private lateinit var stopWorkingLayout: CardView

    private val colorConnected by lazy { requireContext().getColorCompat(R.color.colorAccent) }
    private val colorDisconnected by lazy { requireContext().getColorCompat(R.color.colorIconPrimary) }
    private val backgroundConnected by lazy { requireContext().getDrawableCompat(R.drawable.background_button_connected) }
    private val backgroundDisconnected by lazy { requireContext().getDrawableCompat(R.drawable.background_button_disconnected) }

    private fun setStatusConnected() {
        statusTextView.setTextColor(colorConnected)
        statusTextView.background = backgroundConnected
        statusTextView.text = getString(R.string.status_connected)
    }

    private fun setStatusDisconnected() {
        statusTextView.setTextColor(colorDisconnected)
        statusTextView.background = backgroundDisconnected
        statusTextView.text = getString(R.string.status_disconnected)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        iniViewModel()
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        initViews(root)

        lifecycleScope.launch {
            SubscriptionService.isConnectedState.zip(
                SubscriptionService.isProStatusPurchasedState
            ) { isConnected, isPurchased ->
                isConnected && isPurchased
            }
                .collect { isPurchased ->
                    if (isPurchased) {
                        if (appSettingsVm.isSubscribeSuccessShow.value != true) {
                            val fragment = SubscriptionSuccessDialogFragment.newInstance()
                            fragment.show(parentFragmentManager, null)
                            appSettingsVm.setSubscribeSuccessShow(true)
                        }
                    }
                }
        }

        legalStateDescriptionTextView
            .isVisible = RemoteConfigService.getIsLegalStateDescriptionEnabled()

        SubscriptionService.updateIsProLimited()
        lifecycleScope.launch {
            SubscriptionService.isProStatusPurchasedState
                .combine(SubscriptionService.isPopupShowState)
                { isPurchased, isShow ->
                    !isPurchased && isShow
                }.collect { isVisible ->
                    limitedOfferLayout.isVisible = isVisible
                    if (limitedOfferLayout.isVisible) {
                        StatisticHelper.logAction(TrackerConstants.EVENT_PRO_TUTORIAL_PRO_POPUP_SHOWN)
                    }
                }
        }

        limitedOfferLayout.setOnClickListener {
            findNavController()
                .apply {
                    StatisticHelper.logAction(TrackerConstants.EVENT_PRO_TUTORIAL_OPENED_FROM_PRO_POPUP)
                    navigate(
                        MobileNavigationDirections.activityToSubscriptionTutorial()
                    )
                }
        }

        closePopupButton.setOnClickListener {
            StatisticHelper.logAction(TrackerConstants.EVENT_PRO_TUTORIAL_CLOSE_PRO_POPUP)
            SubscriptionService.setClosePopup(true)
        }

        val updateLongStatus = { status: TunnelStatus, counter: Long? ->
            when {
                status.inProgress -> longStatusTextView.text =
                    getString(R.string.home_status_detail_progress)
                status.active && status.gatewayId != null && counter == null -> {
                    setStatusConnected()
                    longStatusTextView.text =
                        if (RemoteConfigService.getIsAdShieldAdsCounterLimited()) {
                            getString(R.string.home_status_detail_active_slim)
                        } else {
                            ("${getString(R.string.home_status_detail_active)}\n${getString(R.string.home_status_detail_plus)}")
                                .withBoldSections(
                                    requireContext()
                                        .getColorFromAttr(R.attr.colorAccent)
                                )
                        }
                }
                status.active && EnvironmentService.isSlim() -> {
                    setStatusConnected()
                    longStatusTextView.text = getString(R.string.home_status_detail_active_slim)
                }
                status.active && counter == null -> {
                    setStatusConnected()
                    longStatusTextView.text = getString(R.string.home_status_detail_active)
                        .withBoldSections(requireContext().getColorFromAttr(R.attr.colorAccent))
                }
                status.active && status.gatewayId != null -> {
                    setStatusConnected()
                    val statusString = if (RemoteConfigService.getIsAdShieldAdsCounterLimited()) {
                        getString(R.string.home_status_detail_active_slim)
                    } else {
                        "${
                            getString(
                                R.string.home_status_detail_active_with_counter,
                                counter.toString()
                            )
                        }\n ${getString(R.string.home_status_detail_plus)}"
                    }
                    longStatusTextView.text =
                        statusString.withBoldSections(requireContext().getColorFromAttr(R.attr.colorAccent))
                }
                status.active -> {
                    setStatusConnected()
                    longStatusTextView.text =
                        if (RemoteConfigService.getIsAdShieldAdsCounterLimited()) {
                            getString(R.string.home_status_detail_active_slim)
                        } else {
                            getString(
                                R.string.home_status_detail_active_with_counter,
                                counter.toString()
                            )
                                .withBoldSections(requireContext().getColorFromAttr(R.attr.colorAccent))
                        }
                }
                else -> {
                    setStatusDisconnected()
                    statusTextView.text = getString(R.string.status_disconnected)
                    longStatusTextView.text = getString(R.string.home_action_tap_to_activate)
                }
            }

            longStatusTextView.setOnClickListener {
                when {
                    status.inProgress -> Unit
                    status.error != null -> Unit
                    status.active -> {
                        val fragment = ProtectionLevelFragment.newInstance()
                        fragment.show(parentFragmentManager, null)
                    }
                    else -> vm.turnOn()
                }
            }
        }

        stopWorkingLayout.setOnClickListener {
            openUrlInBrowser("${RemoteConfigService.getAdblockTutorialUrl()}#${Build.MANUFACTURER.toLowerCase()}")
        }

        vm.isAdblockWork.observe(viewLifecycleOwner) { isAdshieldWork ->
            stopWorkingLayout.isVisible = activateView.activeMode && !isAdshieldWork
        }

        vm.tunnelStatus.observe(viewLifecycleOwner) { status ->
            stopWorkingLayout.isVisible = stopWorkingLayout.isVisible && status.active

            activateView.inactiveMode = !status.inProgress && !status.active
            activateView.activeMode = status.active
            activateView.isEnabled = !status.inProgress

            activateView.setOnClickListener {
                activateView.onClicked()
                when {
                    status.inProgress -> Unit
                    status.error != null -> Unit
                    status.active -> {
                        vm.turnOff()
                        adsCounterVm.roll()
                    }
                    else -> vm.turnOn()
                }
            }

            when {
                status.inProgress -> statusTextView.text =
                    getString(R.string.home_status_detail_progress)
                status.active -> setStatusConnected()
                else -> setStatusDisconnected()
            }

            updateLongStatus(status, adsCounterVm.counter.value?.let {
                if (it == 0L) null else it
            })

            when (status.error) {
                null -> Unit
                is NoPermissions -> showVpnPermsSheet()
                else -> showFailureDialog(status.error)
            }
        }

        adsCounterVm.counter.observe(viewLifecycleOwner) { counter ->
            vm.tunnelStatus.value?.let { s ->
                updateLongStatus(s, counter)
            }
        }

        accountVM.account.observe(viewLifecycleOwner) { account ->
            if (!account.isActive()) {
                setHasOptionsMenu(true)
            }
        }

        lifecycleScope.launchWhenCreated {
            delay(1000)
            UpdateService.handleUpdateFlow(
                onOpenDonate = {
                    findNavController()
                        .apply {
                            navigate(R.id.navigation_home)
                            navigate(
                                HomeFragmentDirections.actionNavigationHomeToWebFragment(
                                    Links.donate, getString(R.string.universal_action_donate)
                                )
                            )
                        }
                },
                onOpenMore = {
                    // Display the thank you page
                    findNavController()
                        .apply {
                            navigate(R.id.navigation_home)
                            navigate(
                                HomeFragmentDirections.actionNavigationHomeToWebFragment(
                                    Links.updated, getString(R.string.update_label_updated)
                                )
                            )
                        }
                }
            )
        }
        return root
    }

    override fun onResume() {
        super.onResume()
        vm.checkIfAdblockWork()
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
                findNavController()
                    .apply {
                        navigate(
                            HomeFragmentDirections.actionNavigationHomeToWebFragment(
                                Links.tunnelFailure, getString(R.string.universal_action_learn_more)
                            )
                        )
                    }
            }
            else null

        alert.showAlert(
            message = mapErrorToUserFriendly(ex),
            onDismiss = vm::setInformedUserAboutError,
            additionalAction = additional
        )
    }

    private fun iniViewModel() {
        activity?.let {
            vm = ViewModelProvider(it.app()).get(TunnelViewModel::class.java)
            accountVM = ViewModelProvider(it.app()).get(AccountViewModel::class.java)
            adsCounterVm = ViewModelProvider(it.app()).get(AdsCounterViewModel::class.java)
            appSettingsVm = ViewModelProvider(it.app()).get(AppSettingsViewModel::class.java)
            settingsVM = ViewModelProvider(it.app()).get(SettingsViewModel::class.java)
        }
    }

    private fun initViews(root: View) {
        activateView = root.findViewById(R.id.activateButton)
        statusTextView = root.findViewById(R.id.statusTextView)
        limitedOfferLayout = root.findViewById(R.id.limitedOfferLayout)
        closePopupButton = root.findViewById(R.id.closePopupButton)
        longStatusTextView = root.findViewById(R.id.statusDescriptionTextView)
        legalStateDescriptionTextView = root.findViewById(R.id.legalStateDescriptionTextView)
        stopWorkingLayout = root.findViewById(R.id.stopWorkingLayout)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home_donate -> {
                findNavController()
                    .apply {
                        navigate(R.id.navigation_settings)
                        navigate(
                            SettingsFragmentDirections.actionNavigationSettingsToWebFragment(
                                Links.donate, getString(R.string.universal_action_donate)
                            )
                        )
                    }

                true
            }
            else -> false
        }
    }

    override fun onDestroy() {
        SubscriptionService.onDestroy()
        super.onDestroy()
    }

    private fun openUrlInBrowser(url: String) {
        Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            ContextService.requireContext().startActivity(this)
        }
    }
}
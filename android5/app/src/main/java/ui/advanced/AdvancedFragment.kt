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

package ui.advanced

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import model.TunnelStatus
import org.adshield.R
import service.EnvironmentService
import service.RemoteConfigService
import ui.TunnelViewModel
import ui.app
import utils.Links

class AdvancedFragment : Fragment() {

    private lateinit var tunnelVM: TunnelViewModel

    private data class Section(
        val name: String,
        val slugline: String,
        val iconResId: Int,
        val destination: NavDirections
    )

    private val isSlimMode = EnvironmentService.isSlim()

    private val sections by lazy {
        listOfNotNull(
            Section(
                name = getString(R.string.networks_section_header),
                slugline = getString(R.string.networks_section_label),
                iconResId = R.drawable.ic_networks,
                destination = AdvancedFragmentDirections.actionAdvancedFragmentToSettingsNetworksFragment()
            ),
            if (isSlimMode) {
                null
            } else {
                Section(
                    name = getString(R.string.str_dns_settings),
                    slugline = getString(R.string.dns_forwarding_lists_description),
                    iconResId = R.drawable.ic_forwarding_lists,
                    destination = AdvancedFragmentDirections.actionNavigationSettingsToWebFragment(
                        Links.dnsSettings, getString(R.string.str_dns_settings)
                    )
                )
            },
            Section(
                name = getString(R.string.str_presets_settings),
                slugline = getString(R.string.str_presets_settings_description),
                iconResId = R.drawable.ic_my_blocklists,
                destination = AdvancedFragmentDirections.actionAdvancedFragmentToPresetsFragment()
            ),
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            tunnelVM = ViewModelProvider(it.app()).get(TunnelViewModel::class.java)
        }

        val root = inflater.inflate(R.layout.fragment_advanced, container, false)
        val sectionsContainer = root.findViewById<ViewGroup>(R.id.advanced_container)
        sectionsContainer.removeAllViews()

        for (section in sections) {
            val (name, slugline, iconResId, destination) = section

            val sectionView =
                inflater.inflate(R.layout.item_advanced_section, sectionsContainer, false)
            sectionView.setOnClickListener {
                val nav = findNavController()
                nav.navigate(destination)
            }
            sectionsContainer.addView(sectionView)

            val nameView = sectionView.findViewById<TextView>(R.id.advanced_name)
            nameView.text = name

            val sluglineView = sectionView.findViewById<TextView>(R.id.advanced_slugline)
            sluglineView.text = slugline

            val iconView = sectionView.findViewById<ImageView>(R.id.advanced_icon)
            iconView.setImageResource(iconResId)
        }

        val developImageView = root.findViewById<ImageView>(R.id.developImageView)
        val developTextView = root.findViewById<TextView>(R.id.advanced_migrateslim)
        val isDevelopVisible = RemoteConfigService.getIsWebAppsSettingsLimited()
                || RemoteConfigService.getIsWebCustomSettingsEnabled()
        developImageView.isVisible = isDevelopVisible
        developTextView.isVisible = isDevelopVisible
        return root
    }
}

internal fun statusToLevel(status: TunnelStatus): Int {
    return when {
        status.inProgress -> -1
        status.gatewayId != null -> 2
        status.active && status.isUsingDnsOverHttps -> 2
        status.active -> 0
        else -> 0
    }
}

private fun Context.levelToText(level: Int): String {
    return when (level) {
        -1 -> "..."
        1 -> getString(R.string.account_encrypt_label_level_medium)
        2 -> getString(R.string.account_encrypt_label_level_high)
        else -> getString(R.string.account_encrypt_label_level_low)
    }
}
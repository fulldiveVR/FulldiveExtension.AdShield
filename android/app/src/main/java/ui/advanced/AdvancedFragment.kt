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

package ui.advanced

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import model.TunnelStatus
import org.blokada.R
import service.EnvironmentService
import service.tr
import ui.SettingsViewModel
import ui.TunnelViewModel
import ui.advanced.encryption.EncryptionLevelFragment
import ui.app
import ui.home.PaymentFeaturesFragment
import ui.utils.getColorFromAttr
import utils.Links

class AdvancedFragment : Fragment() {

    private lateinit var tunnelVM: TunnelViewModel

    private data class Section(
        val name: String,
        val slugline: String,
        val iconResId: Int,
        val destination: NavDirections
    )

    private val sections by lazy {
        listOf(
            if (EnvironmentService.isSlim()) null
            else {
                Section(
                    name = getString(R.string.advanced_section_header_packs),
                    slugline = getString(R.string.advanced_section_slugline_packs),
                    iconResId = R.drawable.ic_baseline_remove_red_eye_24,
                    destination = AdvancedFragmentDirections.actionAdvancedFragmentToNavigationPacks()
                )
            },
            Section(
                name = getString(R.string.apps_section_header),
                slugline = getString(R.string.advanced_section_slugline_apps),
                iconResId = R.drawable.ic_baseline_apps_24,
                destination = AdvancedFragmentDirections.actionAdvancedFragmentToAppsFragment()
            ),
            Section(
                name = getString(R.string.account_action_encryption),
                slugline = getString(R.string.advanced_section_slugline_encryption),
                iconResId = R.drawable.ic_baseline_lock_24,
                destination = AdvancedFragmentDirections.actionAdvancedFragmentToSettingsEncryptionFragment()
            )
        ).filterNotNull()
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

            val sectionView = inflater.inflate(R.layout.item_advanced_section, sectionsContainer, false)
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

        val encryptionView = root.findViewById<View>(R.id.advanced_level)
        val encryptionIcon = root.findViewById<ImageView>(R.id.advanced_level_icon)
        val encryptionLevel = root.findViewById<TextView>(R.id.advanced_level_status)

        encryptionView.setOnClickListener {
            val fragment = EncryptionLevelFragment.newInstance()
            fragment.show(parentFragmentManager, null)
        }

        tunnelVM.tunnelStatus.observe(viewLifecycleOwner, Observer { status ->
            val level = statusToLevel(status)
            val ctx = requireContext()
            val color = when (level) {
                -1 -> ctx.getColorFromAttr(android.R.attr.textColorSecondary)
                1 -> ctx.getColor(R.color.orange)
                2 -> ctx.getColor(R.color.green)
                else -> ctx.getColor(R.color.red)
            }

            encryptionIcon.setColorFilter(color)
            encryptionIcon.setImageResource(when (level) {
                2 -> R.drawable.ic_baseline_lock_24
                1 -> R.drawable.ic_baseline_lock_open_24
                else -> R.drawable.ic_baseline_no_encryption_24
            })

            encryptionLevel.setTextColor(color)
            encryptionLevel.text = ctx.levelToText(level)
        })

        val migrateSlim = root.findViewById<View>(R.id.advanced_migrateslim)
        migrateSlim.visibility = if (EnvironmentService.isSlim()) View.VISIBLE else View.GONE
        migrateSlim.setOnClickListener {
            val nav = findNavController()
            nav.navigate(
                AdvancedFragmentDirections.actionAdvancedFragmentToWebFragment(
                    Links.updated, getString(R.string.universal_action_learn_more)
                )
            )
        }

        return root
    }

}

internal fun statusToLevel(status: TunnelStatus): Int {
    return when {
        status.inProgress -> -1
        status.gatewayId != null -> 2
        status.active && status.isUsingDnsOverHttps -> 1
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
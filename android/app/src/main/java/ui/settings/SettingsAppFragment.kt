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

package ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import org.blokada.R
import repository.LANGUAGE_NICE_NAMES
import service.EnvironmentService
import service.UpdateService
import ui.BlockaRepoViewModel
import ui.SettingsViewModel
import ui.app
import ui.THEME_RETRO_KEY
import ui.THEME_RETRO_NAME
import utils.Links

class SettingsAppFragment : PreferenceFragmentCompat() {

    private lateinit var vm: SettingsViewModel
    private lateinit var blockaRepoVM: BlockaRepoViewModel

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_app, rootKey)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let {
            vm = ViewModelProvider(it.app()).get(SettingsViewModel::class.java)
            blockaRepoVM = ViewModelProvider(it.app()).get(BlockaRepoViewModel::class.java)
        }

        val language: ListPreference = findPreference("app_language")!!
        val languages = mutableMapOf(
            "root" to getString(R.string.app_settings_status_default)
        ).also {
            it.putAll(LANGUAGE_NICE_NAMES.toSortedMap())
        }
        language.entryValues = languages.keys.toTypedArray()
        language.entries = languages.map { it.value }.toTypedArray()
        language.setOnPreferenceChangeListener { _, newValue ->
            when (newValue) {
                "root" -> vm.setLocale(null)
                else -> vm.setLocale(newValue as String)
            }
            true
        }

        val theme: ListPreference = findPreference("app_theme")!!
        theme.entryValues = listOf(
            getString(R.string.app_settings_status_default),
            getString(R.string.app_settings_theme_dark),
            getString(R.string.app_settings_theme_light),
            if (vm.syncableConfig.value?.rated == true) THEME_RETRO_NAME else null
        ).filterNotNull().toTypedArray()
        theme.entries = theme.entryValues
        theme.setOnPreferenceChangeListener { _, newValue ->
            when (newValue) {
                getString(R.string.app_settings_theme_dark) -> vm.setUseDarkTheme(true)
                getString(R.string.app_settings_theme_light) -> vm.setUseDarkTheme(false)
                THEME_RETRO_NAME -> vm.setUseTheme(THEME_RETRO_KEY)
                else -> vm.setUseDarkTheme(null)
            }
            showRestartRequired()
            true
        }

        val browser: ListPreference = findPreference("app_browser")!!
        browser.entryValues = listOf(
            getString(R.string.app_settings_browser_internal),
            getString(R.string.app_settings_browser_external)
        ).toTypedArray()
        browser.entries = browser.entryValues
        browser.setOnPreferenceChangeListener { _, newValue ->
            when (newValue) {
                getString(R.string.app_settings_browser_internal) -> vm.setUseChromeTabs(false)
                else -> vm.setUseChromeTabs(true)
            }
            true
        }

        val yesNoChoice = listOf(
            getString(R.string.universal_action_yes),
            getString(R.string.universal_action_no)
        ).toTypedArray()

        val ipv6: ListPreference = findPreference("app_ipv6")!!
        ipv6.entryValues = yesNoChoice
        ipv6.entries = ipv6.entryValues
        ipv6.setOnPreferenceChangeListener { _, newValue ->
            when (newValue) {
                getString(R.string.universal_action_yes) -> vm.setIpv6(true)
                else -> vm.setIpv6(false)
            }
            showRestartRequired()
            true
        }

        val backup: ListPreference = findPreference("app_backup")!!
        backup.entryValues = yesNoChoice
        backup.entries = backup.entryValues
        backup.setOnPreferenceChangeListener { _, newValue ->
            when (newValue) {
                getString(R.string.universal_action_yes) -> vm.setUseBackup(true)
                else -> vm.setUseBackup(false)
            }
            showRestartRequired()
            true
        }

        val dnsOverHttps: ListPreference = findPreference("app_doh")!!
        dnsOverHttps.entryValues = yesNoChoice
        dnsOverHttps.entries = dnsOverHttps.entryValues
        dnsOverHttps.setOnPreferenceChangeListener { _, newValue ->
            when (newValue) {
                getString(R.string.universal_action_yes) -> vm.setUseDnsOverHttps(true)
                else -> vm.setUseDnsOverHttps(false)
            }
            true
        }

        val useForeground: ListPreference = findPreference("app_useforeground")!!
        useForeground.entryValues = yesNoChoice
        useForeground.entries = useForeground.entryValues
        useForeground.setOnPreferenceChangeListener { _, newValue ->
            when (newValue) {
                getString(R.string.universal_action_yes) -> vm.setUseForegroundService(true)
                else -> vm.setUseForegroundService(false)
            }
            showRestartRequired()
            true
        }

        val details: Preference = findPreference("app_details")!!
        details.summary = EnvironmentService.getUserAgent()

        var clicks = 0
        details.setOnPreferenceClickListener {
            if (clicks++ == 21) {
                vm.setRatedApp()
                Toast.makeText(requireContext(), "( ͡° ͜ʖ ͡°)", Toast.LENGTH_SHORT).show()
            }
            true
        }

        vm.localConfig.observe(viewLifecycleOwner, Observer {
            val value = when (it.useDarkTheme) {
                true -> getString(R.string.app_settings_theme_dark)
                false -> getString(R.string.app_settings_theme_light)
                else -> when (it.themeName) {
                    THEME_RETRO_KEY -> THEME_RETRO_NAME
                    else -> getString(R.string.app_settings_status_default)
                }
            }
            theme.setDefaultValue(value)
            theme.value = value

            val locale = it.locale
            val selected = locale ?: "root"
            language.setDefaultValue(selected)
            language.value = selected

            val b = if (it.useChromeTabs) getString(R.string.app_settings_browser_external)
            else getString(R.string.app_settings_browser_internal)
            browser.setDefaultValue(b)
            browser.value = b

            val useIpv6 = if (it.ipv6) getString(R.string.universal_action_yes)
            else getString(R.string.universal_action_no)
            ipv6.setDefaultValue(useIpv6)
            ipv6.value = useIpv6

            val useBackup = if (it.backup) getString(R.string.universal_action_yes)
            else getString(R.string.universal_action_no)
            backup.setDefaultValue(useBackup)
            backup.value = useBackup

            val useDoh = if (it.useDnsOverHttps) getString(R.string.universal_action_yes)
            else getString(R.string.universal_action_no)
            dnsOverHttps.setDefaultValue(useDoh)
            dnsOverHttps.value = useDoh

            val useFg = if (it.useForegroundService) getString(R.string.universal_action_yes)
            else getString(R.string.universal_action_no)
            useForeground.setDefaultValue(useFg)
            useForeground.value = useFg
        })

        val config: Preference = findPreference("app_config")!!
        config.setOnPreferenceClickListener {
            UpdateService.resetSeenUpdate()
            blockaRepoVM.refreshRepo()
            true
        }

        blockaRepoVM.repoConfig.observe(viewLifecycleOwner, Observer {
            config.summary = it.name
        })

        val boot: Preference = findPreference("app_startonboot")!!
        boot.setOnPreferenceClickListener {
            val nav = findNavController()
            nav.navigate(
                SettingsAppFragmentDirections.actionSettingsAppFragmentToWebFragment(
                    Links.startOnBoot, getString(R.string.app_settings_start_on_boot)
                )
            )
            true
        }

        val info: Preference = findPreference("app_info")!!
        info.setOnPreferenceClickListener {
            val ctx = requireContext()
            ctx.startActivity(getIntentForAppInfo(ctx))
            true
        }

        val vpn: Preference = findPreference("app_vpn")!!
        vpn.setOnPreferenceClickListener {
            val ctx = requireContext()
            ctx.startActivity(getIntentForVpnProfile(ctx))
            true
        }

        val notification: Preference = findPreference("app_notifications")!!
        notification.setOnPreferenceClickListener {
            val ctx = requireContext()
            ctx.startActivity(getIntentForNotificationChannelsSettings(ctx))
            true
        }
    }

    private fun showRestartRequired() {
        Toast.makeText(requireContext(), getString(R.string.universal_status_restart_required), Toast.LENGTH_LONG).show()
    }

    private fun getIntentForAppInfo(ctx: Context) = Intent().apply {
        action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.parse("package:${ctx.packageName}")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    private fun getIntentForVpnProfile(ctx: Context) = Intent().apply {
        action = "android.net.vpn.SETTINGS"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    fun getIntentForNotificationChannelsSettings(ctx: Context) = Intent().apply {
        action = "android.settings.APP_NOTIFICATION_SETTINGS"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        putExtra("app_package", ctx.packageName)
        putExtra("app_uid", ctx.applicationInfo.uid)
        putExtra("android.provider.extra.APP_PACKAGE", ctx.packageName)
    }
}
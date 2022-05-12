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

package ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.internal.ManufacturerUtils
import model.AppTheme
import model.LocalConfig
import model.ThemeHelper
import org.adshield.BuildConfig
import org.adshield.R
import repository.LANGUAGE_NICE_NAMES
import service.EnvironmentService
import ui.AppSettingsViewModel
import ui.SettingsViewModel
import ui.app
import utils.Links
import java.util.*


class SettingsAppFragment : PreferenceFragmentCompat() {

    private lateinit var vm: SettingsViewModel
    private lateinit var appSettingsVm: AppSettingsViewModel
//    private lateinit var blockaRepoVM: BlockaRepoViewModel

    private val yesNoChoice by lazy {
        listOf(
            getString(R.string.universal_action_yes),
            getString(R.string.universal_action_no)
        ).toTypedArray()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_app, rootKey)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViewModel()

        val languagePreference = initLanguagePreference()
        val themePreference = initThemePreference()
        val browserPreference = initAppBrowserPreference()
        val backupPreference = initBackUpPreference()
        val useForegroundPreference = initUseForegroundPreference()
        initAppDetailsPreference()

        vm.localConfig.observe(viewLifecycleOwner) { localConfig ->
            languagePreference?.let { setLanguagePreference(it, localConfig) }
            browserPreference?.let { setAppBrowserPreference(it, localConfig) }
            backupPreference?.let { setBackUpPreference(it, localConfig) }
            useForegroundPreference?.let { setUseForegroundPreference(it, localConfig) }
        }

        appSettingsVm.currentTheme.observe(viewLifecycleOwner) { theme ->
            themePreference?.let { setAppThemePreference(it, theme) }
        }

        initPreferences()
    }

    private fun initViewModel() {
        activity?.let {
            vm = ViewModelProvider(it.app()).get(SettingsViewModel::class.java)
            appSettingsVm = ViewModelProvider(it.app()).get(AppSettingsViewModel::class.java)
//            blockaRepoVM = ViewModelProvider(it.app()).get(BlockaRepoViewModel::class.java)
        }
    }

    private fun initThemePreference(): ListPreference? {
        return (findPreference("app_theme") as? ListPreference)?.also { theme ->
            theme.entryValues = ThemeHelper.generateAppThemes()
                .map { context?.getString(it.titleRes).orEmpty() }
                .toTypedArray()
            theme.entries = theme.entryValues
            theme.setOnPreferenceChangeListener { _, newValue ->
                appSettingsVm.setCurrentAppTheme(
                    AppTheme.getThemeByTitle(
                        requireContext(),
                        newValue.toString()
                    ).type
                )
                true
            }
        }
    }

    private fun setAppThemePreference(themePreference: ListPreference, theme: AppTheme) {
        val value = getString(theme.titleRes)
        themePreference.setDefaultValue(value)
        themePreference.value = value
    }

    private fun initLanguagePreference(): ListPreference? {
        return (findPreference("app_language") as? ListPreference)?.also { language ->
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
        }
    }

    private fun setLanguagePreference(
        languagePreference: ListPreference,
        localConfig: LocalConfig
    ) {
        val locale = localConfig.locale
        val selected = locale ?: "root"
        languagePreference.setDefaultValue(selected)
        languagePreference.value = selected
    }

    private fun initAppDetailsPreference(): Preference? {
        return (findPreference("app_details") as? Preference)?.also { details ->
            details.summary = EnvironmentService.getUserAgent()
            var clicks = 0
            details.setOnPreferenceClickListener {
                if (clicks++ == 21) {
                    vm.setRatedApp()
                    Toast.makeText(requireContext(), "( ͡° ͜ʖ ͡°)", Toast.LENGTH_SHORT).show()
                }
                true
            }
        }
    }

    private fun initAppBrowserPreference(): ListPreference? {
        return (findPreference("app_browser") as? ListPreference)?.also { browser ->
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
        }
    }

    private fun setAppBrowserPreference(
        browserPreference: ListPreference,
        localConfig: LocalConfig
    ) {
        val b = if (localConfig.useChromeTabs) getString(R.string.app_settings_browser_external)
        else getString(R.string.app_settings_browser_internal)
        browserPreference.setDefaultValue(b)
        browserPreference.value = b
    }

    private fun initUseForegroundPreference(): ListPreference? {
        return (findPreference("app_useforeground") as? ListPreference)?.also { useForeground ->
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
        }
    }

    private fun setUseForegroundPreference(
        useForegroundPreference: ListPreference,
        localConfig: LocalConfig
    ) {
        val useFg = if (localConfig.useForegroundService) {
            getString(R.string.universal_action_yes)
        } else {
            getString(R.string.universal_action_no)
        }
        useForegroundPreference.setDefaultValue(useFg)
        useForegroundPreference.value = useFg
    }

    private fun initBackUpPreference(): ListPreference? {
        return (findPreference("app_backup") as? ListPreference)?.also { backup ->
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
        }
    }

    private fun setBackUpPreference(
        backupPreference: ListPreference,
        localConfig: LocalConfig
    ) {
        val useBackup = if (localConfig.backup) getString(R.string.universal_action_yes)
        else getString(R.string.universal_action_no)
        backupPreference.setDefaultValue(useBackup)
        backupPreference.value = useBackup
    }

    private fun initPreferences() {
        //        val config: Preference = findPreference("app_config")!!
//        config.setOnPreferenceClickListener {
//            UpdateService.resetSeenUpdate()
//            blockaRepoVM.refreshRepo()
//            true
//        }

//        blockaRepoVM.repoConfig.observe(viewLifecycleOwner, Observer {
//            config.summary = it.name
//        })

        val boot: Preference? = findPreference("app_startonboot")
        boot?.setOnPreferenceClickListener {
            val nav = findNavController()
            nav.navigate(
                SettingsAppFragmentDirections.actionSettingsAppFragmentToWebFragment(
                    Links.startOnBoot, getString(R.string.app_settings_start_on_boot)
                )
            )
            true
        }

        val info: Preference? = findPreference("app_info")
        info?.setOnPreferenceClickListener {
            val ctx = requireContext()
            ctx.startActivity(getIntentForAppInfo(ctx))
            true
        }

        val vpn: Preference? = findPreference("app_vpn")
        vpn?.setOnPreferenceClickListener {
            val ctx = requireContext()
            ctx.startActivity(getIntentForVpnProfile(ctx))
            true
        }

        val notification: Preference? = findPreference("app_notifications")
        notification?.setOnPreferenceClickListener {
            val ctx = requireContext()
            ctx.startActivity(getIntentForNotificationChannelsSettings(ctx))
            true
        }

        val isMeizu = Build.MANUFACTURER.lowercase(Locale.ENGLISH) == "meizu"

        val battery: Preference? = findPreference("app_battery")
        battery?.isVisible = !isMeizu
        battery?.setOnPreferenceClickListener {
            checkDoze()
            true
        }

        val dataUsage: Preference? = findPreference("app_data")
        dataUsage?.isVisible = !isMeizu
        dataUsage?.setOnPreferenceClickListener {
            checkDataSaving()
            true
        }

        val workAtBackground: Preference? = findPreference("app_background")
        workAtBackground?.isVisible = isMeizu
        workAtBackground?.setOnPreferenceClickListener {
            activity?.let { activity -> openFlymeSecurityApp(activity) }
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showRestartRequired() {
        Toast.makeText(
            requireContext(),
            getString(R.string.universal_status_restart_required),
            Toast.LENGTH_LONG
        ).show()
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

    private fun checkDoze() {
        val settings = Intent(
            Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        )
        if (context?.packageManager?.resolveActivity(
                settings,
                0
            ) != null
        ) try {
            startActivity(settings)
        } catch (ex: Throwable) {
        }
    }

    private fun checkDataSaving() {
        val settings = Intent(
            Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS,
            Uri.parse("package:" + context?.packageName)
        )
        if (context?.packageManager?.resolveActivity(
                settings,
                0
            ) != null
        ) try {
            startActivity(settings)
        } catch (ex: Throwable) {
        }
    }

    private fun openFlymeSecurityApp(context: Activity) {
        val intent = Intent("com.meizu.safe.security.SHOW_APPSEC")
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.putExtra("packageName", BuildConfig.APPLICATION_ID)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun getIntentForAppInfo(ctx: Context) = Intent().apply {
    action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    data = Uri.parse("package:${ctx.packageName}")
    flags = Intent.FLAG_ACTIVITY_NEW_TASK
}


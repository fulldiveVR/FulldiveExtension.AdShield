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

package ui

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import appextension.*
import com.akexorcist.localizationactivity.ui.LocalizationActivity
import com.fulldive.wallet.di.IEnrichableActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.joom.lightsaber.Injector
import kotlinx.coroutines.launch
import org.adshield.R
import service.ContextService
import service.ContextService.requireContext
import service.NetworkMonitorPermissionService
import service.TranslationService
import service.VpnPermissionService
import ui.home.FirstTimeFragment
import ui.home.HomeFragmentDirections
import ui.settings.SettingsFragmentDirections
import ui.settings.SettingsNavigation
import ui.web.WebService
import utils.Logger


class MainActivity : LocalizationActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
    IEnrichableActivity {

    private lateinit var statsVM: StatsViewModel
    private lateinit var tunnelVM: TunnelViewModel
    private lateinit var accountVM: AccountViewModel
    private lateinit var settingsVM: SettingsViewModel
    private lateinit var appSettingsVm: AppSettingsViewModel
    override lateinit var appInjector: Injector

    private lateinit var toolbar: Toolbar

    //    private lateinit var blockaRepoVM: BlockaRepoViewModel
    private lateinit var activationVM: ActivationViewModel

    fun Drawable.colored(color: Int, mode: PorterDuff.Mode = PorterDuff.Mode.SRC_ATOP) =
        this.mutate().apply {
            colorFilter = PorterDuffColorFilter(color, mode)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextService.setActivityContext(this)
        TranslationService.setup()
        initViewModel()

        appSettingsVm.initAppTheme()

        setContentView(R.layout.activity_main)

        val navigationView: BottomNavigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_rewards,
                R.id.advancedFragment,
                R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)

        // Hide the bottom navigation bar, unless we are top level
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val showNavBar = when (destination.id) {
                R.id.navigation_home -> true
                R.id.navigation_rewards -> true
                R.id.advancedFragment -> true
                R.id.navigation_settings -> true
                else -> isScreenBigEnough()
            }
            navigationView.visibility = if (showNavBar) View.VISIBLE else View.GONE
        }

        // Needed for dynamic translation of the bottom bar
        val selectionListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
            val (nav, title) = when (item.itemId) {
                R.id.navigation_rewards -> R.id.navigation_rewards to getString(R.string.main_tab_rewards)
                R.id.advancedFragment -> R.id.advancedFragment to getString(R.string.main_tab_advanced)
                R.id.navigation_settings -> R.id.navigation_settings to getString(R.string.main_tab_settings)
                else -> R.id.navigation_home to getString(R.string.main_tab_home)
            }
            navController.navigate(nav)
            item.title = title
            true
        }
        navigationView.setOnNavigationItemSelectedListener(selectionListener)

        // Needed for dynamic translation of the top bar
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            Logger.v("Navigation", destination.toString())

            val translationId = when (destination.id) {
                R.id.navigation_rewards -> R.string.rewards_toolbar_title
                R.id.activityDetailFragment -> R.string.main_tab_activity
                R.id.navigation_packs -> getString(R.string.advanced_section_header_packs)
                R.id.packDetailFragment -> R.string.advanced_section_header_packs
                R.id.advancedFragment -> R.string.main_tab_advanced
                R.id.userDeniedFragment -> R.string.userdenied_section_header
                R.id.settingsNetworksFragment -> R.string.networks_section_header
                R.id.networksDetailFragment -> R.string.all_networks_title
                R.id.appsFragment -> R.string.apps_section_header
                R.id.navigation_settings -> R.string.main_tab_settings
                R.id.navigation_settings_account -> R.string.account_action_my_account
                R.id.settingsLogoutFragment -> R.string.account_header_logout
                R.id.settingsAppFragment -> R.string.app_settings_section_header
                R.id.leasesFragment -> R.string.account_action_devices
                else -> null
            }
            toolbar.title = translationId?.let {
                if (it is Int) getString(it)
                else it.toString()
            } ?: run { toolbar.title }

            if (destination.id == R.id.navigation_home) {
                toolbar.setLogo(R.drawable.ic_adshield)
            } else {
                toolbar.logo = null
            }
        }

        val workType: String? = intent?.action
        if (workType != null && workType == AppExtensionWorkType.OPEN.id) {
            VpnPermissionService.askPermission()
        }

        PopupManager.onAppStarted(this)
    }

    private fun initViewModel() {
        accountVM = ViewModelProvider(app()).get(AccountViewModel::class.java)
        tunnelVM = ViewModelProvider(app()).get(TunnelViewModel::class.java)
        settingsVM = ViewModelProvider(app()).get(SettingsViewModel::class.java)
        statsVM = ViewModelProvider(app()).get(StatsViewModel::class.java)
        activationVM = ViewModelProvider(this).get(ActivationViewModel::class.java)
        appSettingsVm = ViewModelProvider(this).get(AppSettingsViewModel::class.java)

        tunnelVM.tunnelStatus.observe(this, Observer { status ->
            if (status.active) {
                val firstTime = !(settingsVM.syncableConfig.value?.notFirstRun ?: true)
                if (firstTime) {
                    settingsVM.setFirstTimeSeen()
                    val fragment = FirstTimeFragment.newInstance()
                    fragment.show(supportFragmentManager, null)
                }
            }
            val uri = getContentUri(LaunchHelper.getCurrentState(status))
            contentResolver.insert(uri, null)
        })
    }

    override fun onResume() {
        super.onResume()
        tunnelVM.refreshStatus()
        accountVM.checkAccount()
        lifecycleScope.launch {
            statsVM.refresh()
        }
    }

    override fun onPause() {
        super.onPause()
        tunnelVM.goToBackground()
    }

    override fun onSupportNavigateUp(): Boolean {
        if (WebService.goBack()) return true
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp()
    }

    override fun onBackPressed() {
        if (WebService.goBack()) return
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        VpnPermissionService.resultReturned(resultCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        NetworkMonitorPermissionService.resultReturned(grantResults)
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        SettingsNavigation.handle(navController, pref.key, accountVM.account.value?.id)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.help_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.help_activity -> {
                findNavController(R.id.nav_host_fragment)
                    .apply {
                        navigate(R.id.navigation_home)
                        navigate(HomeFragmentDirections.actionNavigationActivityToActivityStatsFragment())
                    }
                toolbar.title = getString(R.string.activity_section_header)
            }
            R.id.help_help -> {
                PopupManager.showContactSupportDialog(this) {
                    EmailHelper.sendEmailToSupport(this)
                }
            }
//            R.id.help_logs -> LogService.showLog()
//            R.id.help_sharelog -> LogService.shareLog()
            R.id.help_settings -> {
                findNavController(R.id.nav_host_fragment)
                    .apply {
                        navigate(R.id.navigation_settings)
                        navigate(
                            SettingsFragmentDirections.actionNavigationSettingsToSettingsAppFragment()
                        )
                    }
            }
            else -> return false
        }
        return true
    }

    private fun isScreenBigEnough(): Boolean {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels / displayMetrics.density > 650
    }

    companion object {
        const val ACTION = "action"
    }
}
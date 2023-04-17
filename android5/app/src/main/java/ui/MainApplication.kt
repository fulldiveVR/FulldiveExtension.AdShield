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

import android.app.Activity
import android.app.Service
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import blocka.LegacyAccountImport
import com.akexorcist.localizationactivity.ui.LocalizationApplication
import com.fulldive.wallet.di.EnrichableLifecycleCallbacks
import com.fulldive.wallet.di.IInjectorHolder
import com.fulldive.wallet.di.components.ApplicationComponent
import com.fulldive.wallet.extensions.withDefaults
import com.fulldive.wallet.interactors.ExperienceExchangeInterator
import com.fulldive.wallet.models.Chain
import com.joom.lightsaber.Injector
import com.joom.lightsaber.Lightsaber
import com.joom.lightsaber.getInstance
import engine.ABPService
import engine.EngineService
import engine.FilteringService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import model.BlockaConfig
import model.BlockaRepoConfig
import model.BlockaRepoPayload
import service.*
import ui.advanced.packs.PacksViewModel
import utils.Logger
import utils.cause
import java.util.*

class MainApplication : LocalizationApplication(), ViewModelStoreOwner, IInjectorHolder {

    companion object {
        /**
         * Not sure if doing it right, but some ViewModel in our app should be scoped to the
         * application, since they are relevant for when the app is started by the SystemTunnel
         * (as apposed to MainActivity). This probably would be solved better if some LiveData
         * objects within some of the ViewModels would not be owned by them.
         */
        val viewModelStore = ViewModelStore()
    }

    override fun getViewModelStore() = MainApplication.viewModelStore

    override fun getInjector(): Injector {
        return appInjector
    }

    private lateinit var appInjector: Injector
    private lateinit var accountVM: AccountViewModel
    private lateinit var tunnelVM: TunnelViewModel
    private lateinit var settingsVM: SettingsViewModel
    private lateinit var statsVM: StatsViewModel
    private lateinit var adsCounterVM: AdsCounterViewModel
    private lateinit var networksVM: NetworksViewModel
    private lateinit var packsVM: PacksViewModel

    private val experienceExchangeInterator by lazy { appInjector.getInstance<ExperienceExchangeInterator>() }

    override fun onCreate() {
        super.onCreate()
        appInjector = Lightsaber.Builder().build().createInjector(
            ApplicationComponent(applicationContext)
        )
        ContextService.setContext(this)
        LegacyAccountImport.setup()
        LogService.setup()
        DozeService.setup(this)
        setupEvents()
//        MonitorService.setup(settingsVM.getUseForegroundService())
        registerActivityLifecycleCallbacks(EnrichableLifecycleCallbacks(this))

        ABPService.initABP(ContextService.requireContext())
        ABPService.setAdblockState(true)
        ABPService.retainAdblockProvider()
    }

    private fun setupEvents() {
        networksVM = ViewModelProvider(this).get(NetworksViewModel::class.java)
        EngineService.setup(
            network = networksVM.getActiveNetworkConfig(),
            user = PersistenceService.load(BlockaConfig::class) // TODO: not nice
        )

        accountVM = ViewModelProvider(this).get(AccountViewModel::class.java)
        tunnelVM = ViewModelProvider(this).get(TunnelViewModel::class.java)
        settingsVM = ViewModelProvider(this).get(SettingsViewModel::class.java)
        statsVM = ViewModelProvider(this).get(StatsViewModel::class.java)
        adsCounterVM = ViewModelProvider(this).get(AdsCounterViewModel::class.java)
        packsVM = ViewModelProvider(this).get(PacksViewModel::class.java)

        accountVM.account.observeForever { account ->
            tunnelVM.checkConfigAfterAccountChanged(account)
        }

        settingsVM.localConfig.observeForever {
            EnvironmentService.escaped = it.escaped
            TranslationService.setLocale(it.locale)
            tunnelVM.refreshStatus()
        }

        statsVM.stats.observeForever { stats ->
            // Not sure how it can be null, but there was a crash report
            stats?.let { stats ->
                MonitorService.setStats(stats)
                val counter = stats.denied.toLong()
                adsCounterVM.setRuntimeCounter(counter)
            }
        }

        experienceExchangeInterator
            .getExchangeRateForToken(Chain.fdCoinDenom)
            .withDefaults()
            .subscribe()

        adsCounterVM.counter.observeForever {
            MonitorService.setCounter(it)
        }
        tunnelVM.tunnelStatus.observeForever {
            MonitorService.setTunnelStatus(it)
        }

        networksVM.activeConfig.observeForever {
            GlobalScope.launch {
                try {
                    EngineService.updateConfig(network = it)
                } catch (ex: Exception) {
                }

                // Without the foreground service, we will get killed while switching the VPN.
                // The simplest solution is to force the flag (which will apply from the next
                // app start). Not the nicest though.
                if (networksVM.hasCustomConfigs() && !settingsVM.getUseForegroundService())
                    settingsVM.setUseForegroundService(true)
            }
        }
        ConnectivityService.setup()

        GlobalScope.launch {
            BlocklistService.setup()
            packsVM.setup()
            FilteringService.reload(packsVM.getActiveUrls())
        }
    }

    private fun maybePerformAction(repo: BlockaRepoConfig) {
        // TODO: Maybe this method should be extracted to some separate file
        val log = Logger("Action")
        val persistence = PersistenceService
        repo.payload?.let { payload ->
            val previousPayload = persistence.load(BlockaRepoPayload::class)
            if (previousPayload == payload) {
                // Act on each payload once, this one has been acted on before.
                return
            }

            log.v("Got repo payload: ${payload.cmd}")
            try {
                startService(getIntentForCommand(payload.cmd))
            } catch (ex: Exception) {
                log.e("Could not act on payload".cause(ex))
            }

            log.v("Marking repo payload as acted on")
            persistence.save(payload)
        }
    }

    override fun getDefaultLanguage(): Locale {
        return TranslationService.getLocale()
    }

}

fun Activity.app(): MainApplication {
    return application as MainApplication
}

fun Service.app(): MainApplication {
    return application as MainApplication
}
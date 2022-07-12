/*
 * Copyright (c) 2022 FullDive
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package engine

import android.content.Context
import android.net.Uri
import model.CustomBlocklistConfig
import org.adblockplus.libadblockplus.FilterEngine
import org.adblockplus.libadblockplus.android.AdblockEngine
import org.adblockplus.libadblockplus.android.AdblockEngineProvider
import org.adblockplus.libadblockplus.android.AndroidHttpClientResourceWrapper
import org.adblockplus.libadblockplus.android.Subscription
import org.adblockplus.libadblockplus.android.Utils.createDomainAllowlistingFilter
import org.adblockplus.libadblockplus.android.settings.AdblockHelper
import org.adblockplus.libadblockplus.android.settings.AdblockSettingsStorage
import org.adblockplus.libadblockplus.android.settings.Utils
import org.adshield.R
import service.ContextService

object ABPService {

    private val adblockHelperInstance by lazy(LazyThreadSafetyMode.NONE) {
        AdblockHelper.get()
    }

    private val adblockEngineProvider: AdblockEngineProvider by lazy(LazyThreadSafetyMode.NONE) {
        adblockHelperInstance.provider
    }

    private val storage by lazy(LazyThreadSafetyMode.NONE) { adblockHelperInstance.storage }
    private val settings by lazy(LazyThreadSafetyMode.NONE) {
        storage.load() ?: AdblockSettingsStorage.getDefaultSettings(ContextService.requireContext())
    }

    fun getEngine(): AdblockEngine {
        return adblockHelperInstance.provider.engine
    }

    fun initABP(context: Context) {
        if (!adblockHelperInstance.isInit) {
            // init Adblock
            val basePath: String = context
                .getDir(AdblockEngine.BASE_PATH_DIRECTORY, Context.MODE_PRIVATE)
                .absolutePath
            val easylistResource = R.raw.easylist

            // provide preloaded subscriptions
            val preloadedResources = mutableMapOf(
                AndroidHttpClientResourceWrapper.EASYLIST to easylistResource,
                AndroidHttpClientResourceWrapper.ACCEPTABLE_ADS to R.raw.exceptionrules
            )
            adblockHelperInstance
                .init(context, basePath, AdblockHelper.PREFERENCE_NAME)
                .preloadSubscriptions(AdblockHelper.PRELOAD_PREFERENCE_NAME, preloadedResources)

            adblockHelperInstance.siteKeysConfiguration.forceChecks = false
        }
    }

    fun setAdblockState(isEnabled: Boolean) {
        adblockEngineProvider
            .lockEngine()
            ?.let {
                it.isEnabled = isEnabled
                adblockEngineProvider.unlockEngine()
            }
    }

    fun getAdblockState(): Boolean {
        return adblockEngineProvider
            .lockEngine()?.isEnabled
            ?.apply {
                adblockEngineProvider.unlockEngine()
            }
            .orFalse()
    }

    fun setAdblockSubscriptions(selectedSubscriptionUrls: Set<String>) {
        if (adblockHelperInstance.isInit) {
            adblockEngineProvider
                .lockEngine()
                ?.apply {
                    setSubscriptions(selectedSubscriptionUrls)
                    isAcceptableAdsEnabled = settings.isAcceptableAdsEnabled
                    settings.availableSubscriptions = recommendedSubscriptions.toList()
                    adblockEngineProvider.unlockEngine()
                }
            val customSubscriptions = selectedSubscriptionUrls.map { url ->
                Subscription(
                    url, url, "", "", ""
                )
            }
            settings.selectedSubscriptions = Utils.chooseSelectedSubscriptions(
                settings.availableSubscriptions,
                selectedSubscriptionUrls
            ).map { subscription ->
                customSubscriptions.firstOrNull { it.url == subscription.url } ?: subscription
            }
            storage.save(settings)
        }
    }

    fun retainAdblockProvider() {
        adblockEngineProvider.retain(false)
    }

    fun releaseAdblockProvider() {
        adblockEngineProvider.release()
    }

    fun AdblockEngineProvider.lockEngine(): AdblockEngine? {
        val locked = readEngineLock.tryLock()
        return when {
            !locked -> null
            engine != null -> engine
            else -> {
                readEngineLock.unlock()
                null
            }
        }
    }

    fun isBlocked(host: String): Boolean {
        val url = Uri.parse(("http://$host")).toString()
        val result = getEngine().matches(
            url,
            setOf(
                FilterEngine.ContentType.OTHER
            ),
            "",
            "",
            false
        )
        return result == AdblockEngine.MatchesResult.BLOCKED
    }

    fun AdblockEngineProvider.unlockEngine() {
        readEngineLock.unlock()
    }

    fun updateCustomBlocklists(config: CustomBlocklistConfig, currentConfig: CustomBlocklistConfig) {
        removeCurrentFilters(currentConfig)
        if (config.isAllowed.isNotEmpty()) initAllowlistedDomains(config.isAllowed)
        if (config.isDenied.isNotEmpty()) initDennyListedDomains(config.isDenied)
    }

    private fun removeCurrentFilters(currentConfig: CustomBlocklistConfig) {
        adblockEngineProvider
            .lockEngine()
            ?.apply {
                currentConfig.isDenied.forEach { domain ->
                    val filter = filterEngine.getFilter("||$domain^")
                    filterEngine.removeFilter(filter)
                }
                currentConfig.isAllowed.forEach { domain ->
                    val filter = createDomainAllowlistingFilter(filterEngine, domain)
                    filterEngine.removeFilter(filter)
                }
                adblockEngineProvider.unlockEngine()
            }
    }

    private fun initAllowlistedDomains(allowed: List<String>) {
        adblockEngineProvider
            .lockEngine()
            ?.apply {
                initAllowlistedDomains(allowed)
                adblockEngineProvider.unlockEngine()
            }
    }

    private fun initDennyListedDomains(denied: List<String>) {
        adblockEngineProvider
            .lockEngine()
            ?.apply {
                initDennyListedDomains(denied)
                adblockEngineProvider.unlockEngine()
            }
    }

    private fun AdblockEngine.initDennyListedDomains(denied: List<String>) {
        denied.forEach { domain ->
            val filter = filterEngine.getFilter("||$domain^")
            filterEngine.addFilter(filter)
        }
    }

    fun Boolean?.orFalse() = this ?: false
    fun Boolean?.orTrue() = this ?: true
}
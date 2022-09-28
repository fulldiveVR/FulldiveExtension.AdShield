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

package service

import com.fulldive.wallet.extensions.orTrue
import org.adshield.BuildConfig
import remoteconfig.*

object RemoteConfigService {

    private var remoteConfig: IRemoteConfigFetcher? = null

    fun setRemoteConfigFetcher(configFetcher: IRemoteConfigFetcher) {
        this.remoteConfig = configFetcher
    }

    fun getIsProLimited(): Boolean {
        return remoteConfig?.isAdShieldProLimited().orTrue()
    }

    fun getIsLegalStateDescriptionEnabled(): Boolean {
        return remoteConfig?.isAdShieldLegalDescriptionEnabled().orTrue()
    }

    fun getIsWebAppsSettingsLimited(): Boolean {
        return remoteConfig?.isAdShieldWebAppSettingsLimited().orTrue()
    }

    fun getIsWebCustomSettingsEnabled(): Boolean {
        return remoteConfig?.isAdShieldWebCustomSettingsLimited().orTrue()
    }

    fun getIsAdShieldAdsCounterLimited(): Boolean {
        return remoteConfig?.isAdShieldAdsCounterLimited().orTrue()
    }

    fun getIsRewardsLimited(): Boolean {
        return remoteConfig?.getIsRewardsLimited().orTrue()
    }


    fun getIsStatsLimited(): Boolean {
        return remoteConfig?.getIsStatsLimited().orTrue()
    }

    fun getAdblockWorkCheckUrl(): String {
        return remoteConfig?.getAdblockWorkCheckUrl().orEmpty()
    }

    fun getAdblockWorkCheckDomain(): String {
        return remoteConfig?.getAdblockWorkCheckDomain().orEmpty()
    }

    fun getAdblockTutorialUrl(): String {
        return remoteConfig?.getAdblockTutorialUrl().orEmpty()
    }
}
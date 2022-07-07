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

package com.fulldive.wallet.interactors

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.fulldive.wallet.di.modules.DefaultInteractorsModule
import com.fulldive.wallet.extensions.safeSingle
import com.fulldive.wallet.models.AppIcon
import com.joom.lightsaber.ProvidedBy
import io.reactivex.Observable
import io.reactivex.Single
import org.jsoup.Jsoup
import repository.AppIconLocalDataSource
import service.ContextService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ProvidedBy(DefaultInteractorsModule::class)
class AppSettingsRepository @Inject constructor(
    private val appIconLocalDataSource: AppIconLocalDataSource
) {

    fun loadAppIconUrls(): Single<List<String>> {
        return safeSingle {
            val ctx = ContextService.requireContext()
            ctx.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { appInfo ->
                    appInfo.packageName != ctx.packageName && (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                }
        }.flatMap { appInfoList ->
            Observable
                .fromIterable(appInfoList)
                .flatMapSingle { appInfo ->
                    val appId = appInfo.packageName
                    appIconLocalDataSource
                        .getAppIconByAppId(appId)
                        .onErrorResumeNext {
                            val appIconUrl = parseGooglePlayMarketIcon(appId)
                            appIconLocalDataSource.saveAppIcon(
                                AppIcon(
                                    appId,
                                    appIconUrl
                                )
                            ).toSingle { appIconUrl }
                        }
                }.toList()
        }
    }

    fun parseGooglePlayMarketIcon(packageName: String): String {
        return try {
            val document = Jsoup
                .connect("https://play.google.com/store/apps/details?id=$packageName")
                .get()

            val appIconUrl = document.select("meta[property=\"og:image\"]").attr("content")
            appIconUrl
        } catch (e: Exception) {
            ""
        }
    }
}
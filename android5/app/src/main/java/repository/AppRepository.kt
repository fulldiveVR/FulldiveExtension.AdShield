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

package repository

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import com.fulldive.wallet.di.IInjectorHolder
import com.fulldive.wallet.di.components.ApplicationComponent
import com.fulldive.wallet.extensions.orEmptyString
import com.fulldive.wallet.models.AppIcon
import com.joom.lightsaber.Injector
import com.joom.lightsaber.Lightsaber
import com.joom.lightsaber.getInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.App
import model.AppId
import model.BypassedAppIds
import service.ContextService
import service.PersistenceService
import utils.Logger
import utils.cause

object AppRepository : IInjectorHolder {

    private val log = Logger("AppRepository")
    private val persistence = PersistenceService

    private var appInjector: Injector = Lightsaber.Builder().build().createInjector(
        ApplicationComponent(ContextService.requireContext())
    )

    private val appIconLocalDataSource: AppIconLocalDataSource = appInjector.getInstance()

    override fun getInjector(): Injector {
        return appInjector
    }

    private var bypassedAppIds = persistence.load(BypassedAppIds::class).ids
        set(value) {
            persistence.save(BypassedAppIds(value))
            field = value
        }

    private val alwaysBypassed by lazy {
        listOf(
            // This app package name
            //context.requireContext().packageName
            "com.fulldive.mobile",
            "com.fulldive.mobile.stage"
        )
    }

    private val bypassedForFakeVpn = listOf(
        "com.android.vending",
        "com.android.providers.downloads",
        "com.google.android.apps.fireball",
        "com.google.android.apps.authenticator2",
        "com.google.android.apps.docs",
        "com.google.android.apps.tachyon",
        "com.google.android.gm",
        "com.google.android.apps.photos",
        "com.google.android.play.games",
        "org.thoughtcrime.securesms",
        "com.plexapp.android",
        "org.kde.kdeconnect_tp",
        "com.samsung.android.email.provider",
        "com.xda.labs",
        "com.android.incallui",
        "com.android.phone",
        "com.android.providers.telephony",
        "com.huawei.systemmanager",
        "com.android.service.ims.RcsServiceApp",
        "com.google.android.carriersetup",
        "com.google.android.ims",
        "com.codeaurora.ims",
        "com.android.carrierconfig",
        "ch.threema.app",
        "ch.threema.app.work",
        "ch.threema.app.hms",
        "ch.threema.app.work.hms",
        "com.xiaomi.discover",
        "eu.siacs.conversations",
        "org.jitsi.meet",
        "com.tomtom.speedcams.android.map",
        "com.tomtom.amigo.huawei",
        // RCS: https://github.com/blokadaorg/blokadaorg.github.io/pull/31
        "com.android.service.ims.RcsServiceApp",
        "com.google.android.carriersetup",
        "com.google.android.ims",
        "com.codeaurora.ims",
        "com.android.carrierconfig"
    )

    fun getPackageNamesOfAppsToBypass(forRealTunnel: Boolean = false): List<AppId> {
        return if (forRealTunnel) alwaysBypassed + bypassedAppIds
        else alwaysBypassed + bypassedForFakeVpn + bypassedAppIds
    }

    suspend fun getApps(): List<App> {
        return withContext(Dispatchers.Default) {
            log.v("Fetching apps")
            val ctx = ContextService.requireContext()
            val installed = try {
                ctx.packageManager.queryIntentActivities(
                    Intent(Intent.ACTION_MAIN),
                    PackageManager.MATCH_ALL
                )
            } catch (ex: Exception) {
                log.w("Could not fetch apps, ignoring".cause(ex))
                emptySet<ResolveInfo>()
            }.map { it.activityInfo.applicationInfo }.toSet()

            val appIcons = try {
                appIconLocalDataSource.getAllAppIcons()
            } catch (ex: Exception) {
                log.w("Could not apps icons, ignoring".cause(ex))
                emptyList<AppIcon>()
            }

            log.v("Fetched ${installed.size} apps, mapping")
            val apps = installed.mapNotNull { appInfo ->
                try {
                    val appId = appInfo.packageName
                    App(
                        id = appId,
                        name = ctx.packageManager.getApplicationLabel(appInfo).toString(),
                        isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                        isBypassed = isAppBypassed(appInfo.packageName),
                        iconUrl = appIcons.firstOrNull { it.appId == appId }?.iconUrl.orEmptyString()
                    )
                } catch (ex: Exception) {
                    log.w("Could not map app, ignoring".cause(ex))
                    null
                }
            }
                .toSet() // Since we get apps from activities, ic could be doubles.
                .toList()
            log.v("Mapped ${apps.size} apps")
            apps
        }
    }

    fun isAppBypassed(id: AppId): Boolean {
        return bypassedAppIds.contains(id) || alwaysBypassed.contains(id)
    }

    fun switchBypassForApp(id: AppId) {
        when {
            alwaysBypassed.contains(id) -> {
                // Do nothing.
            }
            isAppBypassed(id) -> {
                bypassedAppIds = bypassedAppIds - id
            }
            else -> {
                bypassedAppIds = bypassedAppIds + id
            }
        }
    }

    fun getAppIcon(id: AppId): Drawable? {
        return try {
            val ctx = ContextService.requireContext()
            ctx.packageManager.getApplicationIcon(
                ctx.packageManager.getApplicationInfo(id, PackageManager.GET_META_DATA)
            )
        } catch (e: Exception) {
            null
        }
    }
}

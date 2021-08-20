/*
 * This file is part of Blokada.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright © 2021 Blocka AB. All rights reserved.
 *
 * @author Karol Gusak (karol@blocka.net)
 */

package appextension

import android.content.Context
import android.util.Log
import engine.EngineService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import model.BlockaConfig
import model.TunnelStatus
import service.PersistenceService
import service.VpnPermissionService

object LaunchHelper {

    fun isNeedPermissions(): Boolean {
        val isNeedPermissions = !VpnPermissionService.hasPermission()
        Log.d("isNeedPermissions", "isNeedPermissions:$isNeedPermissions")
        return isNeedPermissions
    }

    fun getCurrentState(): String {
        return getCurrentState(EngineService.getTunnelStatus())
    }

    fun getCurrentState(status: TunnelStatus): String {
        return if (status.active) {
            AppExtensionState.START.id
        } else {
            AppExtensionState.STOP.id
        }
    }

    fun isChangingState() : Boolean {
        return EngineService.getTunnelStatus().inProgress
    }

    fun start(context: Context) {
        val engine = EngineService
        val persistence = PersistenceService
        val currentConfig = persistence.load(BlockaConfig::class)
        val status = engine.getTunnelStatus()
        if (!status.inProgress && !status.active) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val config = currentConfig.copy(tunnelEnabled = true)
                    engine.updateConfig(user = config)
                    persistence.save(config)
                } catch (ex: Exception) {
                }
                val uri = getContentUri(AppExtensionState.START.id)
                context.contentResolver.insert(uri, null)
            }
        }
    }

    fun stop(context: Context) {
        val engine = EngineService
        val persistence = PersistenceService
        val currentConfig = persistence.load(BlockaConfig::class)
        val status = engine.getTunnelStatus()
        if (!status.inProgress && status.active) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val config = currentConfig.copy(tunnelEnabled = false)
                    engine.updateConfig(user = config)
                    persistence.save(config)
                } catch (ex: Exception) {
                }
                val uri = getContentUri(AppExtensionState.STOP.id)
                context.contentResolver.insert(uri, null)
            }
        }
    }
}
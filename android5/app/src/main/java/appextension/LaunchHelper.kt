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

package appextension

import android.content.Context
import engine.EngineService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import model.BlockaConfig
import model.TunnelStatus
import service.PersistenceService

object LaunchHelper {

    fun getCurrentState(): String {
        return getCurrentState(EngineService.getTunnelStatus())
    }

    fun getCurrentState(status: TunnelStatus): String {
        return when {
            status.inProgress -> AppExtensionState.PROGRESS.id
            status.active -> AppExtensionState.START.id
            else -> AppExtensionState.STOP.id
        }
    }

    fun isChangingState(): Boolean {
        return EngineService.getTunnelStatus().inProgress
    }

    fun start(context: Context) {
        val engine = EngineService
        val persistence = PersistenceService
        val currentConfig = persistence.load(BlockaConfig::class)
        val status = engine.getTunnelStatus()
        if (!status.inProgress && !status.active) {
            GlobalScope.launch(Dispatchers.IO) {
                context.contentResolver.insert(getContentUri(AppExtensionState.PROGRESS.id), null)
                try {
                    val config = currentConfig.copy(tunnelEnabled = true)
                    engine.updateConfig(user = config)
                    persistence.save(config)
                } catch (ex: Exception) {
                }
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
                context.contentResolver.insert(getContentUri(AppExtensionState.PROGRESS.id), null)
                try {
                    val config = currentConfig.copy(tunnelEnabled = false)
                    engine.updateConfig(user = config)
                    persistence.save(config)
                } catch (ex: Exception) {
                }
            }
        }
    }
}
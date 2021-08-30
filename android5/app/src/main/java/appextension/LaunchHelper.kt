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
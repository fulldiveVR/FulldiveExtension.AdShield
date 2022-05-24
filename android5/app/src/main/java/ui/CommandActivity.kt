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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import appextension.or
import engine.EngineService
import model.BlokadaException
import org.adshield.R
import service.ContextService
import service.EnvironmentService
import service.LogService
import service.NotificationService
import utils.cause
import utils.Logger
import utils.MonitorNotification.Companion.STATUS_NOTIFICATION_ID

enum class Command {
    OFF, ON, DNS, LOG, ACC, ESCAPE, TOAST, DOH, HIDE
}

const val ACC_MANAGE = "manage_account"
const val OFF = "off"
const val ON = "on"

private typealias Param = String

class CommandActivity : AppCompatActivity() {

    private val log = Logger("Command")

    private lateinit var tunnelVM: TunnelViewModel
    private lateinit var settingsVM: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tunnelVM = ViewModelProvider(app()).get(TunnelViewModel::class.java)
        settingsVM = ViewModelProvider(app()).get(SettingsViewModel::class.java)

        interpretCommand(intent.data.toString())?.let {
            val (cmd, param) = it
            log.w("Received command: $cmd")
            try {
                execute(cmd, param)
                log.v("Command executed successfully")
            } catch (ex: Exception) {
                log.e("Could not execute command".cause(ex))
            }
        } ?: run {
            log.e("Received unknown command: ${intent.data}")
        }

        finish()
    }

    private fun execute(command: Command, param: Param?) {
        when (command) {
            Command.OFF -> tunnelVM.turnOff()
            Command.ON -> tunnelVM.turnOn()
            Command.LOG -> LogService.shareLog()
            Command.ACC -> {
                if (param == ACC_MANAGE) {
                    log.v("Starting account management screen")
                    val intent = Intent(this, MainActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        it.putExtra(MainActivity.ACTION, ACC_MANAGE)
                    }
                    startActivity(intent)
                } else throw BlokadaException("Unknown param for command ACC: $param, ignoring")
            }
            Command.ESCAPE -> {
                if (param == null) {
                    settingsVM.setEscaped(true)
                } else {
                    val versionCode = param.toInt()
                    if (EnvironmentService.getVersionCode() <= versionCode) {
                        settingsVM.setEscaped(true)
                    } else {
                        log.v("Ignoring escape command, too new version code")
                    }
                }
            }
            Command.HIDE -> {
                val status = EngineService.getTunnelStatus()
                if (!status.inProgress && !status.active) {
                    tunnelVM.turnOff()
                    NotificationService.cancel(STATUS_NOTIFICATION_ID)
                } else {
                    Toast
                        .makeText(
                            this,
                            getText(R.string.cant_hide_toast),
                            Toast.LENGTH_LONG
                        )
                        .show()
                }
            }
            Command.TOAST -> {
                Toast.makeText(this, param, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun interpretCommand(input: String): Pair<Command, Param?>? {
        return when {
            input.startsWith("blocka://cmd/") -> {
                input.replace("blocka://cmd/", "")
                    .trimEnd('/')
                    .split("/")
                    .let {
                        try {
                            Command.valueOf(it[0].toUpperCase()) to it.getOrNull(1)
                        } catch (ex: Exception) {
                            null
                        }
                    }
            }
            // Legacy commands to be removed in the future
            input.startsWith("blocka://log") -> Command.LOG to null
            input.startsWith("blocka://acc") -> Command.ACC to ACC_MANAGE
            else -> null
        }
    }

    private fun ensureParam(param: Param?): Param {
        return param ?: throw BlokadaException("Required param not provided")
    }

}

//class CommandService : IntentService("cmd") {
//
//    override fun onHandleIntent(intent: Intent?) {
//        intent?.let {
//            val ctx = ContextService.requireContext()
//            ctx.startActivity(Intent(ACTION_VIEW, it.data).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            })
//        }
//    }
//
//}

class CommandWorker constructor(
    private val context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {
    override fun doWork(): Result {
        return try {
            val command = inputData.getString("serviceCommand")?.or { "" }
            context.startActivity(Intent(ACTION_VIEW, Uri.parse(command)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            Result.success()
        } catch (ex: Exception) {
            Result.failure()
        }

    }
}

class CommandReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.startActivity(Intent(ACTION_VIEW, intent.data).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}

fun getIntentForCommand(command: Command, param: Param? = null): Intent {
    val ctx = ContextService.requireContext()
    return Intent(ctx, CommandReceiver::class.java).apply {
        if (param == null) {
            data = Uri.parse("blocka://cmd/${command.name}")
        } else {
            data = Uri.parse("blocka://cmd/${command.name}/$param")
        }
    }
}

fun getIntentForCommand(cmd: String): Intent {
    val ctx = ContextService.requireContext()
    return Intent(ctx, CommandReceiver::class.java).apply {
        data = Uri.parse("blocka://cmd/$cmd")
    }
}

fun getWorkerRequestForCommand(command: Command): WorkRequest {
    val data = Data.Builder()
    val stringCommand = "blocka://cmd/$command"
    data.putString("serviceCommand", stringCommand)
    return OneTimeWorkRequest
        .Builder(CommandWorker::class.java)
        .addTag(stringCommand)
        .setInputData(data.build())
        .build()
}

fun getWorkerRequestForCommand(command: Command, param: Param? = null): WorkRequest {
    val data = Data.Builder()
    val stringCommand = if (param == null) {
        "blocka://cmd/${command.name}"
    } else {
        "blocka://cmd/${command.name}/$param"
    }
    data.putString("serviceCommand", stringCommand)
    return OneTimeWorkRequest
        .Builder(CommandWorker::class.java)
        .addTag(stringCommand)
        .setInputData(data.build())
        .build()
}

fun executeCommand(cmd: Command, param: Param? = null) {
    val context = ContextService.requireContext()
//    val intent = getIntentForCommand(cmd, param)
//    ctx.startService(intent)
    try {
        val command = "blocka://cmd/$cmd"
        val workManager = WorkManager.getInstance(context)
        val data = Data.Builder()
        data.putString("serviceCommand", command)
        OneTimeWorkRequest
            .Builder(CommandWorker::class.java)
            .addTag(command)
            .setInputData(data.build())
            .build()
            .let(workManager::enqueue)
    } catch (e: Throwable) {
//        Log.e(cmd, "Error while UpdateWidgetJob with work manager:", e)
    }
}
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

package utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import appextension.or
import engine.Host
import model.TunnelStatus
import org.adshield.R
import service.AppSettingsService
import service.Localised
import ui.Command
import ui.MainActivity
import ui.getIntentForCommand

private const val IMPORTANCE_NONE = 2
private const val IMPORTANCE_DEFAULT = 3
private const val IMPORTANCE_HIGH = 4

enum class NotificationChannels(val title: Localised, val importance: Int) {
    ACTIVITY("Activity", IMPORTANCE_NONE),
    ANNOUNCEMENT("Announcements", IMPORTANCE_HIGH),
    UPDATE("Updates", IMPORTANCE_HIGH);
}

sealed class NotificationPrototype(
    val id: Int,
    val channel: NotificationChannels,
    val autoCancel: Boolean = false,
    val create: (ctx: Context) -> NotificationCompat.Builder
)

class MonitorNotification(
    tunnelStatus: TunnelStatus,
    counter: Long,
    lastDenied: List<Host>,
    @StringRes infoStringRes: Int
) : NotificationPrototype(STATUS_NOTIFICATION_ID, NotificationChannels.ACTIVITY,
    create = { context ->
        val builder = NotificationCompat.Builder(context)

        builder.setSmallIcon(R.drawable.ic_stat_adshield)
        builder.priority = NotificationCompat.PRIORITY_MAX
        builder.setVibrate(LongArray(0))
        builder.setOngoing(true)

        val intentFlags = PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE

        when {
            tunnelStatus.inProgress -> {
                builder.setContentTitle(context.getString(R.string.universal_status_processing))
            }
            tunnelStatus.active -> {
                val protection = when {
                    tunnelStatus.isPlusMode() -> context.getString(R.string.home_level_high)
                    tunnelStatus.isDnsEncrypted() -> context.getString(R.string.home_level_high)
                    else -> context.getString(R.string.home_level_low)
                }

                val location = if (tunnelStatus.isPlusMode()) tunnelStatus.gatewayLabel
                else context.getString(R.string.home_status_active)

                val title = "%s - %s - %s".format(
                    location,
                    protection,
                    tunnelStatus.dns?.label?.or { "" }
                )

                builder.setContentTitle(title)

                val style = NotificationCompat.InboxStyle()

                if (infoStringRes != 0) {
                    style.addLine(context.getString(infoStringRes))
                }
                if (!AppSettingsService.getIsBlockHistoryAtNotification()) {
                    lastDenied.forEach {
                        style.addLine(it)
                    }
                }

                builder.setStyle(style)

                builder.addAction(run {
                    getIntentForCommand(Command.OFF).let {
                        PendingIntent.getBroadcast(context, 0, it, intentFlags)
                    }.let {
                        NotificationCompat.Action(
                            R.drawable.ic_baseline_power_settings_new_24,
                            context.getString(R.string.home_power_action_turn_off), it
                        )
                    }
                })

            }
            else -> {
                builder.setContentTitle(
                    context.getString(R.string.home_status_deactivated).toLowerCase().capitalize()
                )

                builder.addAction(run {
                    getIntentForCommand(Command.ON).let {
                        PendingIntent.getBroadcast(context, 0, it, intentFlags)
                    }.let {
                        NotificationCompat.Action(
                            R.drawable.ic_baseline_power_settings_new_24,
                            context.getString(R.string.home_power_action_turn_on), it
                        )
                    }
                })

                builder.addAction(run {
                    getIntentForCommand(
                        Command.HIDE,
                        context.getString(R.string.notification_desc_settings)
                    ).let {
                        PendingIntent.getBroadcast(context, 0, it, intentFlags)
                    }.let {
                        NotificationCompat.Action(
                            R.drawable.ic_baseline_power_settings_new_24,
                            context.getString(R.string.universal_action_hide), it
                        )
                    }
                })
            }
        }

        val intentActivity = Intent(context, MainActivity::class.java)
        intentActivity.putExtra("notification", true)
        val piActivity = PendingIntent.getActivity(
            context, 0, intentActivity, PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(piActivity)
    }
) {
    companion object {
        const val STATUS_NOTIFICATION_ID = 1
    }
}

class UpdateNotification(versionName: String) :
    NotificationPrototype(3, NotificationChannels.UPDATE,
        create = { ctx ->
            val b = NotificationCompat.Builder(ctx)
            b.setContentTitle(ctx.getString(R.string.notification_update_header))
            b.setContentText(ctx.getString(R.string.universal_action_learn_more))
            b.setSmallIcon(R.drawable.ic_stat_adshield)
            b.priority = NotificationCompat.PRIORITY_DEFAULT
            b.setVibrate(LongArray(0))

            val intentActivity = Intent(ctx, MainActivity::class.java)
            intentActivity.putExtra("update", true)
            val piActivity =
                PendingIntent.getActivity(ctx, 0, intentActivity, PendingIntent.FLAG_IMMUTABLE)
            b.setContentIntent(piActivity)
        }
    )

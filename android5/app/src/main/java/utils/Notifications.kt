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
import androidx.core.app.NotificationCompat
import appextension.or
import model.TunnelStatus
import engine.Host
import org.adshield.R
import service.Localised
import ui.Command
import ui.MainActivity
import ui.getIntentForCommand

private const val IMPORTANCE_NONE = 0
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
    lastDenied: List<Host>
): NotificationPrototype(STATUS_NOTIFICATION_ID, NotificationChannels.ACTIVITY,
    create = { ctx ->
        val b = NotificationCompat.Builder(ctx)

        b.setSmallIcon(R.drawable.ic_stat_adshield)
        b.priority = NotificationCompat.PRIORITY_MAX
        b.setVibrate(LongArray(0))
        b.setOngoing(true)

        val intentFlags = PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE

        when {
            tunnelStatus.inProgress -> {
                b.setContentTitle(ctx.getString(R.string.universal_status_processing))
            }
            tunnelStatus.active -> {
                val protection = when {
                    tunnelStatus.isPlusMode() -> ctx.getString(R.string.home_level_high)
                    tunnelStatus.isDnsEncrypted() -> ctx.getString(R.string.home_level_medium)
                    else -> ctx.getString(R.string.home_level_low)
                }

                val location = if (tunnelStatus.isPlusMode()) tunnelStatus.gatewayLabel
                else ctx.getString(R.string.home_status_active)

                val title = "%s - %s - %s".format(
                    location,
                    protection,
                    tunnelStatus.dns?.label?.or { "" }
                )

                b.setContentTitle(title)

                val style = NotificationCompat.InboxStyle()
                lastDenied.forEach {
                    style.addLine(it)
                }
                b.setStyle(style)

                b.addAction(run {
                    getIntentForCommand(Command.OFF).let {
                        PendingIntent.getBroadcast(ctx, 0, it, intentFlags)
                    }.let {
                        NotificationCompat.Action(R.drawable.ic_baseline_power_settings_new_24,
                            ctx.getString(R.string.home_power_action_turn_off), it)
                    }
                })

            }
            else -> {
                b.setContentTitle(
                    ctx.getString(R.string.home_status_deactivated).toLowerCase().capitalize()
                )

                b.addAction(run {
                    getIntentForCommand(Command.ON).let {
                        PendingIntent.getBroadcast(ctx, 0, it, intentFlags)
                    }.let {
                        NotificationCompat.Action(R.drawable.ic_baseline_power_settings_new_24,
                            ctx.getString(R.string.home_power_action_turn_on), it)
                    }
                })

                b.addAction(run {
                    getIntentForCommand(Command.HIDE, ctx.getString(R.string.notification_desc_settings)).let {
                        PendingIntent.getBroadcast(ctx, 0, it, intentFlags)
                    }.let {
                        NotificationCompat.Action(R.drawable.ic_baseline_power_settings_new_24,
                            ctx.getString(R.string.universal_action_hide), it)
                    }
                })
            }
        }

        val intentActivity = Intent(ctx, MainActivity::class.java)
        intentActivity.putExtra("notification", true)
        val piActivity = PendingIntent.getActivity(ctx, 0, intentActivity, PendingIntent.FLAG_IMMUTABLE)
        b.setContentIntent(piActivity)
    }
) {
    companion object {
        const val STATUS_NOTIFICATION_ID = 1
    }
}

class UpdateNotification(versionName: String): NotificationPrototype(3, NotificationChannels.UPDATE,
    create = { ctx ->
        val b = NotificationCompat.Builder(ctx)
        b.setContentTitle(ctx.getString(R.string.notification_update_header))
        b.setContentText(ctx.getString(R.string.universal_action_learn_more))
        b.setSmallIcon(R.drawable.ic_stat_adshield)
        b.priority = NotificationCompat.PRIORITY_DEFAULT
        b.setVibrate(LongArray(0))

        val intentActivity = Intent(ctx, MainActivity::class.java)
        intentActivity.putExtra("update", true)
        val piActivity = PendingIntent.getActivity(ctx, 0, intentActivity, 0)
        b.setContentIntent(piActivity)
    }
)

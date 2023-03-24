/*
 * This file is part of Blokada.
 *
 * Blokada is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Blokada is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Blokada.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright © 2020 Blocka AB. All rights reserved.
 *
 * @author Karol Gusak (karol@blocka.net)
 */

package utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import model.TunnelStatus
import engine.Host
import org.blokada.R
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
    UPDATE("Updates", IMPORTANCE_HIGH),
    BLOCKA("Blokada Plus", IMPORTANCE_HIGH);
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
): NotificationPrototype(1, NotificationChannels.ACTIVITY,
    create = { ctx ->
        val b = NotificationCompat.Builder(ctx)
//        b.setContentTitle(ctx.resources.getString(R.string.notification_keepalive_title, counter))
//        b.setContentText(ctx.getString(R.string.notification_blocked_text, reason))
        b.setSmallIcon(R.drawable.ic_stat_blokada)
        b.priority = NotificationCompat.PRIORITY_MAX
        b.setVibrate(LongArray(0))
        b.setOngoing(true)

        when {
            tunnelStatus.inProgress -> {
                b.setContentTitle(ctx.getString(R.string.universal_status_processing))
            }
            tunnelStatus.active -> {
                b.setContentTitle(
//                    ctx.getString(R.string.home_status_detail_active_with_counter, counter.toString())
//                        .replace("*", "")
//                        .capitalize()
                    ctx.getString(R.string.home_status_active).toLowerCase().capitalize()
                )
                lastDenied.firstOrNull()?.let {
                    b.setContentText(it)
                }

                val style = NotificationCompat.InboxStyle()
                lastDenied.forEach {
                    style.addLine(it)
                }
                b.setStyle(style)

                b.addAction(run {
                    getIntentForCommand(Command.OFF).let {
                        PendingIntent.getService(ctx, 0, it, PendingIntent.FLAG_UPDATE_CURRENT)
                    }.let {
                        NotificationCompat.Action(R.drawable.ic_baseline_power_settings_new_24,
                            ctx.getString(R.string.home_power_action_turn_off), it)
                    }
                })

                b.addAction(run {
                    getIntentForCommand(Command.TOAST, ctx.getString(R.string.notification_desc_settings)).let {
                        PendingIntent.getService(ctx, 0, it, PendingIntent.FLAG_UPDATE_CURRENT)
                    }.let {
                        NotificationCompat.Action(R.drawable.ic_baseline_power_settings_new_24,
                            ctx.getString(R.string.universal_action_hide), it)
                    }
                })
            }
            else -> {
                b.setContentTitle(
                    ctx.getString(R.string.home_status_deactivated).toLowerCase().capitalize()
                )

                b.addAction(run {
                    getIntentForCommand(Command.ON).let {
                        PendingIntent.getService(ctx, 0, it, PendingIntent.FLAG_UPDATE_CURRENT)
                    }.let {
                        NotificationCompat.Action(R.drawable.ic_baseline_power_settings_new_24,
                            ctx.getString(R.string.home_power_action_turn_on), it)
                    }
                })

                b.addAction(run {
                    getIntentForCommand(Command.TOAST, ctx.getString(R.string.notification_desc_settings)).let {
                        PendingIntent.getService(ctx, 0, it, PendingIntent.FLAG_UPDATE_CURRENT)
                    }.let {
                        NotificationCompat.Action(R.drawable.ic_baseline_power_settings_new_24,
                            ctx.getString(R.string.universal_action_hide), it)
                    }
                })
            }
        }

        val intentActivity = Intent(ctx, MainActivity::class.java)
        intentActivity.putExtra("notification", true)
        val piActivity = PendingIntent.getActivity(ctx, 0, intentActivity, 0)
        b.setContentIntent(piActivity)

//        val iw = Intent(ctx, ANotificationsWhitelistService::class.java)
//        iw.putExtra("host", reason)
//        val piw = PendingIntent.getService(ctx, ++requestCode, iw, 0)
//        val actionWhitelist = NotificationCompat.Action(R.drawable.ic_verified,
//            ctx.getString(R.string.notification_blocked_whitelist), piw)
//        b.addAction(actionWhitelist)

//        val intent = Intent(ctx, ANotificationsOffService::class.java)
//        val pi = PendingIntent.getService(ctx, 0, intent, 0)
//        val actionNotificationsOff = NotificationCompat.Action(R.drawable.ic_blocked,
//            ctx.getString(R.string.notification_blocked_off), pi)
//        b.addAction(actionNotificationsOff)
    }
)

//class UsefulKeepAliveNotification(val count: Int, val last: String): BlokadaNotification(2,
//    NotificationChannels.KEEP_ALIVE,
//    create = { ctx ->
//        val i18n = ctx.inject().instance<I18n>()
//        val servers = printServers(ctx.inject().instance<Dns>().dnsServers())
//        val t: Tunnel = ctx.inject().instance()
//
//        val b = NotificationCompat.Builder(ctx)
//        if (Product.current(ctx) == Product.GOOGLE) {
//            val choice = ctx.inject().instance<Dns>().choices().first { it.active }
//            val id = if (choice.id.startsWith("custom")) "custom" else choice.id
//            val provider = i18n.localisedOrNull("dns_${id}_name") ?: id.capitalize()
//
//            b.setContentTitle(provider)
//            b.setContentText(ctx.getString(R.string.dns_keepalive_content, servers))
//        } else {
//            val domainList = NotificationCompat.InboxStyle()
//
//            RequestLog.getRecentHistory().filter { it.state == RequestState.BLOCKED_NORMAL }.take(15).asReversed().distinct().forEach { request ->
//                domainList.addLine(request.domain)
//            }
//
//            val intent = Intent(ctx, ANotificationsToggleService::class.java).putExtra("new_state",!t.enabled())
//            intent.putExtra("setting", NotificationsToggleSeviceSettings.GENERAL)
//            val statePendingIntent = PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//            if(t.enabled()) {
//                b.addAction(R.drawable.ic_stat_blokada, ctx.resources.getString(R.string.notification_keepalive_deactivate), statePendingIntent)
//            }else{
//                b.addAction(R.drawable.ic_stat_blokada, ctx.resources.getString(R.string.notification_keepalive_activate), statePendingIntent)
//            }
//
//            b.setContentTitle(ctx.resources.getString(R.string.notification_keepalive_title, count))
//            b.setContentText(ctx.getString(R.string.notification_keepalive_content, last))
//            b.setStyle(domainList)
//        }
//        b.setSmallIcon(R.drawable.ic_stat_blokada)
//        b.priority = NotificationCompat.PRIORITY_MIN
//        b.setOngoing(true)
//
//        val intentActivity = Intent(ctx, PanelActivity::class.java)
//        intentActivity.putExtra("notification", true)
//        val piActivity = PendingIntent.getActivity(ctx, 0, intentActivity, 0)
//        b.setContentIntent(piActivity)
//    }
//)

//fun getIntentForNotificationChannelsSettings(ctx: Context) = Intent().apply {
//    action = "android.settings.APP_NOTIFICATION_SETTINGS"
//    putExtra("app_package", ctx.packageName)
//    putExtra("app_uid", ctx.applicationInfo.uid)
//    putExtra("android.provider.extra.APP_PACKAGE", ctx.packageName)
//}

class UpdateNotification(versionName: String): NotificationPrototype(3, NotificationChannels.UPDATE,
    create = { ctx ->
        val b = NotificationCompat.Builder(ctx)
        b.setContentTitle(ctx.getString(R.string.notification_update_header))
        b.setContentText(ctx.getString(R.string.universal_action_learn_more))
        b.setSmallIcon(R.drawable.ic_stat_blokada)
        b.setPriority(NotificationCompat.PRIORITY_DEFAULT)
        b.setVibrate(LongArray(0))

        val intentActivity = Intent(ctx, MainActivity::class.java)
        intentActivity.putExtra("update", true)
        val piActivity = PendingIntent.getActivity(ctx, 0, intentActivity, 0)
        b.setContentIntent(piActivity)
    }
)

class ExpiredNotification: NotificationPrototype(4, NotificationChannels.BLOCKA,
    create = { ctx ->
        val b = NotificationCompat.Builder(ctx)
        b.setContentTitle(ctx.getString(R.string.notification_vpn_expired_header))
        b.setContentText(ctx.getString(R.string.notification_vpn_expired_subtitle))
        b.setStyle(NotificationCompat.BigTextStyle().bigText(ctx.getString(R.string.notification_vpn_expired_body)))
        //b.setSmallIcon(R.drawable.ic_stat_blokada)
        b.setSmallIcon(R.drawable.ic_stat_blokada)
        b.setPriority(NotificationCompat.PRIORITY_MAX)
        b.setVibrate(LongArray(0))

        val intentActivity = Intent(ctx, MainActivity::class.java)
        val piActivity = PendingIntent.getActivity(ctx, 0, intentActivity, 0)
        b.setContentIntent(piActivity)
    }
)

//class AnnouncementNotification(announcement: Announcement): BlokadaNotification(6,
//    NotificationChannels.ANNOUNCEMENT, autoCancel = true,
//    create = { ctx ->
//        val b = NotificationCompat.Builder(ctx)
//        b.setContentTitle(announcement.title)
//        b.setContentText(announcement.tagline)
//        b.setStyle(NotificationCompat.BigTextStyle().bigText(announcement.tagline))
//        b.setSmallIcon(R.drawable.ic_stat_blokada)
//        b.priority = NotificationCompat.PRIORITY_MAX
//        b.setVibrate(LongArray(0))
//
//        val intent = Intent(ctx, AnnouncementNotificationTappedService::class.java)
//        val pi = PendingIntent.getService(ctx, 0, intent, 0)
//        b.setContentIntent(pi)
//    }
//)
//
//class AnnouncementNotificationTappedService : IntentService("announcementNotification") {
//
//    override fun onHandleIntent(intent: Intent) {
//        v("announcement notification tapped")
//        markAnnouncementAsSeen()
//        openInExternalBrowser(this, URL(getAnnouncementUrl()))
//    }
//
//}

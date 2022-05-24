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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.fulldive.wallet.di.modules.DefaultInteractorsModule
import com.fulldive.wallet.extensions.safeCompletable
import com.fulldive.wallet.models.ExchangePack
import com.joom.lightsaber.ProvidedBy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.adshield.R
import ui.MainActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ProvidedBy(DefaultInteractorsModule::class)
open class ExperienceExchangeInterator @Inject constructor(
    private val context: Context,
    private val experienceExchangeRepository: ExperienceExchangeRepository
) {

    fun observeExchangePacks(): Observable<List<ExchangePack>> {
        return experienceExchangeRepository.observeExchangePacks()
    }

    fun getAvailableExchangePacks(): Completable {
        return experienceExchangeRepository.getAvailableExchangePacks()
    }

    fun exchangeExperience(title: String, address: String): Completable {
        return experienceExchangeRepository.exchangeExperience(title, address)
    }

    fun setExperience(adsCount: Long): Completable {
        return experienceExchangeRepository.setExperience(adsCount)
            .flatMapCompletable { experience ->
                if (experience >= SettingsLocalDataSource.EXPERIENCE_MIN_EXCHANGE_COUNT) {
                    showNotification()
                } else {
                    Completable.complete()
                }
            }
    }

    private fun showNotification(): Completable {
        return safeCompletable {
            val title = context.getString(R.string.str_notification_experience_limit_title)
            val description =
                context.getString(R.string.str_notification_experience_limit_description)

            val notificationLayout = RemoteViews(
                context.packageName,
                R.layout.layout_notification_exchange_xp
            ).apply {
                setImageViewResource(
                    R.id.imageView,
                    R.drawable.ic_crypto_push
                )
                setTextViewText(R.id.titleView, title)
                setTextViewText(R.id.messageView, description)
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_adshield)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setContentTitle(title)
                .setContentText(description)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)

            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("update", true)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            builder.setContentIntent(pendingIntent)
                .setCustomContentView(notificationLayout)

            val notificationManager = context
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
                builder.setChannelId(CHANNEL_ID)
            }

            notificationManager.notify(NOTIFICATION_ID, builder.build())
        }
    }

    fun observeExperience(): Observable<Pair<Int, Int>> {
        return experienceExchangeRepository.observeExperience()
    }

    fun getExperience(): Single<Int> {
        return experienceExchangeRepository.getExperience()
    }

    fun observeIfExchangeTimeIntervalPassed(): Observable<Boolean> {
        return experienceExchangeRepository.observeIfExchangeTimeIntervalPassed()
    }

    fun clearExchangedExperience(): Completable {
        return experienceExchangeRepository.clearExchangedExperience()
    }

    companion object {
        const val CHANNEL_ID = "adshield_channel_id"
        const val CHANNEL_NAME = "Adshield Channel"
        const val NOTIFICATION_ID = 98765
    }
}
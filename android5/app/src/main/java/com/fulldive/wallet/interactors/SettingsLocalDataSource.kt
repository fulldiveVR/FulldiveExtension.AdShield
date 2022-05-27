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

import android.content.Context
import android.text.format.DateFormat
import appextension.*
import com.fulldive.wallet.di.modules.DefaultModule
import com.fulldive.wallet.extensions.or
import com.fulldive.wallet.extensions.safeCompletable
import com.fulldive.wallet.extensions.safeSingle
import com.joom.lightsaber.ProvidedBy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import service.AppSettingsService
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ProvidedBy(DefaultModule::class)
class SettingsLocalDataSource @Inject constructor(
    context: Context
) {
    val sharedPreferences = context.getPrivateSharedPreferences()

    fun setExperience(adsCount: Long) {
        val previousExperience = sharedPreferences.getProperty(KEY_EXPERIENCE, 0)
        val newExperience = (adsCount * ADSCOUNT_EXPERIENCE_COEFFICIENT).toInt()

        val experience = when {
            previousExperience + newExperience >= EXPERIENCE_MIN_EXCHANGE_COUNT -> EXPERIENCE_MIN_EXCHANGE_COUNT
            else -> previousExperience + newExperience
        }
        sharedPreferences.setProperty(KEY_EXPERIENCE, experience)
    }

    fun observeExperience(): Observable<Pair<Int, Int>> {
        return sharedPreferences
            .observeSettingsInt(KEY_EXPERIENCE, 0)
            .map { experience ->
                Pair(experience, EXPERIENCE_MIN_EXCHANGE_COUNT)
            }
    }

    fun getExperience(): Single<Int> {
        return safeSingle { sharedPreferences.getInt(KEY_EXPERIENCE, 0) }
    }

    fun observeIfExchangeTimeIntervalPassed(): Observable<Boolean> {
        return sharedPreferences
            .observeSettingsLong(LAST_EXCHANGE_DATE)
            .map { exchangeDateTime ->
                isDaysIntervalPassed(System.currentTimeMillis(), exchangeDateTime)
            }
    }

    fun removeExchangedExperience(): Completable {
        return safeCompletable {
            sharedPreferences.setProperty(KEY_EXPERIENCE, 0)
            sharedPreferences.setProperty(LAST_EXCHANGE_DATE, getDateWithoutHours().time)
        }
    }

    private fun getDateWithoutHours(): Date {
        val dateFormat = DateFormat.getDateFormat(AppSettingsService.context)
        return dateFormat.parse(dateFormat.format(Date())).or(Date())
    }

    fun isDaysIntervalPassed(currentTime: Long, time: Long): Boolean {
        val timeIntervalBetweenDays = (currentTime - time)
        val daysIntervalBetweenDays = TimeUnit.DAYS.convert(
            timeIntervalBetweenDays,
            TimeUnit.MILLISECONDS
        )
        return daysIntervalBetweenDays >= EXCHANGE_DAYS_INTERVAL
    }

    fun setExchangeRateForToken(denom: String, rate: Int): Completable {
        return safeCompletable { sharedPreferences.setProperty("$KEY_DENOM_RATE$denom", rate) }
    }

    fun observeExchangeRateForToken(denom: String): Observable<Int> {
        return sharedPreferences.observeSettingsInt("$KEY_DENOM_RATE$denom")
    }

    fun setExchangePushShownTime(pushShownTime: Long): Completable {
        return safeCompletable {
            sharedPreferences.setProperty(KEY_EXCHANGE_PUSH_SHOW_TIME, pushShownTime)
        }
    }

    fun observeExchangePushShownTime(): Observable<Long> {
        return sharedPreferences.observeSettingsLong(KEY_EXCHANGE_PUSH_SHOW_TIME)
    }

    companion object {
        private const val KEY_EXPERIENCE = "KEY_EXPERIENCE"
        private const val LAST_EXCHANGE_DATE = "LAST_EXCHANGE_DATE"
        private const val KEY_DENOM_RATE = "KEY_DENOM_RATE"
        private const val KEY_EXCHANGE_PUSH_SHOW_TIME = "KEY_EXCHANGE_PUSH_SHOW_TIME"

        const val EXPERIENCE_MIN_EXCHANGE_COUNT = 1000
        const val EXCHANGE_DAYS_INTERVAL = 1
        private const val ADSCOUNT_EXPERIENCE_COEFFICIENT = 0.3
    }
}
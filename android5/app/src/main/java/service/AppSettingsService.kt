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

package service

import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.util.Log
import appextension.*
import com.fulldive.wallet.extensions.or
import com.fulldive.wallet.extensions.safeCompletable
import com.fulldive.wallet.extensions.unsafeLazy
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Observable
import model.AppTheme
import model.ThemeHelper
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
object AppSettingsService {

    @SuppressLint("StaticFieldLeak")
    val context = ContextService.requireContext()
    val sharedPreferences = context.getPrivateSharedPreferences()

    private const val KEY_START_APP_COUNTER = "KEY_START_APP_COUNTER"
    private const val KEY_RATE_US_DONE = "KEY_RATE_US_DONE"
    private const val KEY_INSTALL_BROWSER_DONE = "KEY_INSTALL_BROWSER_DONE"
    private const val KEY_IS_IDO_ANNOUNCEMENT_POPUP_SHOWN = "KEY_IS_IDO_ANNOUNCEMENT_POPUP_SHOWN"
    private const val KEY_APP_THEME = "KEY_APP_THEME"
    private const val KEY_EXPERIENCE = "KEY_EXPERIENCE"
    private const val LAST_EXCHANGE_DATE = "LAST_EXCHANGE_DATE"

    private const val EXCHANGE_CURRENCY = 10 //todo 1000
    private const val EXCHANGE_DAYS_INTERVAL = 1
    private const val ADSCOUNT_EXPERIENCE_COEFFICIENT = 2

    fun updateAndGetCurrentStartUpCount(): Int {
        val startCounter = sharedPreferences.getProperty(KEY_START_APP_COUNTER, 0)
        sharedPreferences.setProperty(KEY_START_APP_COUNTER, startCounter + 1)
        return startCounter
    }

    fun isRateUsDone(): Boolean {
        return sharedPreferences.getProperty(KEY_RATE_US_DONE, false)
    }

    fun setRateUsDone() {
        sharedPreferences.setProperty(KEY_RATE_US_DONE, true)
    }

    fun isInstallBrowserDone(): Boolean {
        return sharedPreferences.getProperty(KEY_INSTALL_BROWSER_DONE, false)
    }

    fun setInstallBrowserDone() {
        sharedPreferences.setProperty(KEY_INSTALL_BROWSER_DONE, true)
    }

    fun setIdoAnnouncementClicked() {
        sharedPreferences.setProperty(KEY_IS_IDO_ANNOUNCEMENT_POPUP_SHOWN, true)
    }

    fun isIdoAnnouncementClicked(): Boolean {
        return sharedPreferences.getProperty(KEY_IS_IDO_ANNOUNCEMENT_POPUP_SHOWN, false)
    }

    fun getCurrentAppTheme(): String {
        return sharedPreferences.getProperty(KEY_APP_THEME, AppTheme.AUTO_THEME)
    }

    fun setCurrentAppTheme(theme: String) {
        sharedPreferences.setProperty(KEY_APP_THEME, theme)
        val appTheme = AppTheme.getThemeByType(getCurrentAppTheme())
        initCurrentAppTheme(appTheme)
    }

    fun setExperience(adsCount: Long) {
58    }

    fun observeExperience(): Observable<Pair<Int, Int>> {
        return sharedPreferences
            .observeSettingsInt(KEY_EXPERIENCE, 0)
            .map { experience ->
                Pair(experience, EXCHANGE_CURRENCY)
            }
    }

    fun observeIfExchangeTimeIntervalPassed(): Observable<Boolean> {
        return sharedPreferences
            .observeSettingsLong(LAST_EXCHANGE_DATE)
            .map { exchangeDateTime ->
                isDaysIntervalPassed(System.currentTimeMillis(), exchangeDateTime)
            }
    }

    fun clearExchangedExperience(): Completable {
        return safeCompletable {
            sharedPreferences.setProperty(KEY_EXPERIENCE, 0)
            sharedPreferences.setProperty(LAST_EXCHANGE_DATE, getDateWithoutHours().time)
        }
    }

    private fun initCurrentAppTheme(theme: AppTheme) {
        ThemeHelper.setCurrentAppTheme(theme.mode)
    }

    private fun getDateWithoutHours(): Date {
        val dateFormat = DateFormat.getDateFormat(context)
        return dateFormat.parse(dateFormat.format(Date())).or(Date())
    }

    private fun isDaysIntervalPassed(currentTime: Long, time: Long): Boolean {
        val timeIntervalBetweenDays = (currentTime - time)
        val daysIntervalBetweenDays = TimeUnit.DAYS.convert(
            timeIntervalBetweenDays,
            TimeUnit.MILLISECONDS
        )
        return daysIntervalBetweenDays >= EXCHANGE_DAYS_INTERVAL
    }
}
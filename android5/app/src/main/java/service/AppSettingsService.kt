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
import appextension.*
import model.AppTheme
import model.ThemeHelper

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

    private const val KEY_IS_BLOCK_HISTORY_AT_NOTIFICATIONS = "KEY_IS_SHOW_HISTORY_AT_NOTIFICATIONS"

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

    fun getIsBlockHistoryAtNotification(): Boolean {
        return sharedPreferences.getProperty(KEY_IS_BLOCK_HISTORY_AT_NOTIFICATIONS, false)
    }

    fun setIsBlockHistoryAtNotification(isBlock: Boolean) {
        sharedPreferences.setProperty(KEY_IS_BLOCK_HISTORY_AT_NOTIFICATIONS, isBlock)
    }

    private fun initCurrentAppTheme(theme: AppTheme) {
        ThemeHelper.setCurrentAppTheme(theme.mode)
    }
}
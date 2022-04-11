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

package ui

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import model.AppTheme
import model.ThemeHelper
import service.AppSettingsService

class AppSettingsViewModel : ViewModel() {

    private val isIdoAnnouncementClickedLd = MutableLiveData<Boolean>()
    val isIdoAnnouncementClicked: LiveData<Boolean> = isIdoAnnouncementClickedLd
        .distinctUntilChanged()

    private val currentThemeLd = MutableLiveData<AppTheme>()

    val currentTheme: LiveData<AppTheme> = currentThemeLd

    init {
        viewModelScope.launch {
            updateLiveData()
        }
    }

    fun setIdoAnnouncementClicked() {
        viewModelScope.launch {
            AppSettingsService.setIdoAnnouncementClicked()
            currentThemeLd.value = AppSettingsService
                .getCurrentAppTheme().let { AppTheme.getThemeByType(it) }
        }
    }

    fun updateLiveData() {
        isIdoAnnouncementClickedLd.value = AppSettingsService.isIdoAnnouncementClicked()
        currentThemeLd.value = AppSettingsService
            .getCurrentAppTheme().let { AppTheme.getThemeByType(it) }
    }

    fun initAppTheme() {
        val appTheme = AppTheme.getThemeByType(AppSettingsService.getCurrentAppTheme())
        ThemeHelper.setCurrentAppTheme(appTheme.mode)
    }

    fun setCurrentAppTheme(theme: String) {
        viewModelScope.launch {
            AppSettingsService.setCurrentAppTheme(theme)
            currentThemeLd.value = AppSettingsService
                .getCurrentAppTheme().let { AppTheme.getThemeByType(it) }
        }
    }
}
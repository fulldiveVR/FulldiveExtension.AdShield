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
import com.fulldive.wallet.di.IInjectorHolder
import com.fulldive.wallet.di.components.ApplicationComponent
import com.fulldive.wallet.extensions.orTrue
import com.fulldive.wallet.interactors.WalletInteractor
import com.joom.lightsaber.Injector
import com.joom.lightsaber.Lightsaber
import com.joom.lightsaber.getInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import model.AppTheme
import model.ThemeHelper
import service.AppSettingsService
import service.ContextService
import service.RemoteConfigService

class AppSettingsViewModel : ViewModel(), IInjectorHolder {

    val isTutorialPopupShowState = MutableStateFlow<Boolean>(false)

    private var appInjector: Injector = Lightsaber.Builder().build().createInjector(
        ApplicationComponent(ContextService.requireContext())
    )

    private val walletInteractor: WalletInteractor = appInjector.getInstance()

    private val repeatPopupCounts = listOf(4, 6)

    override fun getInjector(): Injector {
        return appInjector
    }

    private val isIdoAnnouncementClickedLd = MutableLiveData<Boolean>()
    val isIdoAnnouncementClicked: LiveData<Boolean> = isIdoAnnouncementClickedLd
        .distinctUntilChanged()

    private val isSubscribeSuccessShowLd = MutableLiveData<Boolean>()
    val isSubscribeSuccessShow: LiveData<Boolean> = isSubscribeSuccessShowLd
        .distinctUntilChanged()

    private val isRewardsLimitedLd = MutableLiveData<Boolean>()
    val isRewardsLimited: LiveData<Boolean> = isRewardsLimitedLd
        .distinctUntilChanged()

    private val isStatsLimitedLd = MutableLiveData<Boolean>()
    val isStatsLimited: LiveData<Boolean> = isStatsLimitedLd
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
            updateLiveData()
        }
    }

    fun updateLiveData() {
        isIdoAnnouncementClickedLd.value = AppSettingsService.isIdoAnnouncementClicked()
        isSubscribeSuccessShowLd.value = AppSettingsService.isSubscribeSuccessShow()
        currentThemeLd.value = AppSettingsService
            .getCurrentAppTheme().let { AppTheme.getThemeByType(it) }
        isRewardsLimitedLd.value =
            RemoteConfigService.getIsRewardsLimited() && walletInteractor.getCurrentAccount() == null
        isStatsLimitedLd.value = if (isRewardsLimitedLd.value.orTrue()) {
            RemoteConfigService.getIsStatsLimited()
        } else {
            true
        }
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

    fun setSubscribeSuccessShow(isShow: Boolean) {
        AppSettingsService.setSubscribeSuccessShow(isShow)
    }

    fun setTutorialClosePopup(isClose: Boolean) {
        isTutorialPopupShowState.value = !isClose
        AppSettingsService.setIsTutorialPopupClosed(true)
    }

    fun handleTutorialPopupState() {
        val isClosed = AppSettingsService.getIsTutorialPopupClosed()
        isTutorialPopupShowState.value = when {
            isClosed -> {
                val closeCount = AppSettingsService.getTutorialCloseStartCounter()
                val startCount = AppSettingsService.getCurrentStartCounter()
                val diff = startCount - closeCount
                repeatPopupCounts.any { it == diff }
            }

            else -> true
        } && AppSettingsService.compareVersions() >= 0 && AppSettingsService.getCurrentStartCounter() > 4
    }
}
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

import com.fulldive.wallet.di.modules.DefaultInteractorsModule
import com.joom.lightsaber.ProvidedBy
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

@ProvidedBy(DefaultInteractorsModule::class)
class SettingsInterator @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    fun setExchangePushShownTime(pushShownTime: Long): Completable {
        return settingsRepository.setExchangePushShownTime(pushShownTime)
    }

    fun observeExchangePushShownTime(): Observable<Long> {
        return settingsRepository.observeExchangePushShownTime()
    }
}
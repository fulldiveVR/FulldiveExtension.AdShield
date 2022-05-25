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
import com.fulldive.wallet.models.ExchangePack
import com.joom.lightsaber.ProvidedBy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ProvidedBy(DefaultInteractorsModule::class)
class ExperienceExchangeRepository @Inject constructor(
    private val experienceExchangeLocalDataSource: ExperienceExchangeLocalDataSource,
    private val experienceExchangeRemoteSource: ExperienceExchangeRemoteSource,
    private val settingsLocalDataSource: SettingsLocalDataSource
) {

    fun observeExchangePacks(): Observable<List<ExchangePack>> {
        return experienceExchangeLocalDataSource.observeExchangePacks()
    }

    fun getAvailableExchangePacks(): Completable {
        return experienceExchangeRemoteSource
            .getAvailableExchangePacks()
            .flatMapCompletable { exchangePacks ->
                experienceExchangeLocalDataSource
                    .setExchangePacks(exchangePacks)
            }
    }

    fun exchangeExperience(title: String, address: String): Completable {
        return experienceExchangeRemoteSource.exchangeExperience(title, address)
    }

    fun setExperience(adsCount: Long): Completable {
        return settingsLocalDataSource.setExperience(adsCount)
    }

    fun observeExperience(): Observable<Pair<Int, Int>> {
        return settingsLocalDataSource.observeExperience()
    }

    fun getExperience(): Single<Int> {
        return settingsLocalDataSource.getExperience()
    }

    fun observeIfExchangeTimeIntervalPassed(): Observable<Boolean> {
        return settingsLocalDataSource.observeIfExchangeTimeIntervalPassed()
    }

    fun clearExchangedExperience(): Completable {
        return settingsLocalDataSource.clearExchangedExperience()
    }

    fun isDaysIntervalPassed(currentTime: Long, time: Long): Boolean {
        return settingsLocalDataSource.isDaysIntervalPassed(currentTime, time)
    }
}
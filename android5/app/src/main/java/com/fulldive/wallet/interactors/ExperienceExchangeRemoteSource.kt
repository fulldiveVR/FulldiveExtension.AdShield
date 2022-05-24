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
import com.fulldive.wallet.models.ExchangeRequest
import com.fulldive.wallet.remote.FullDiveApi
import com.joom.lightsaber.ProvidedBy
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ProvidedBy(DefaultInteractorsModule::class)
class ExperienceExchangeRemoteSource @Inject constructor(
    private val fullDiveApi: FullDiveApi
) {

    fun getAvailableExchangePacks(): Single<List<ExchangePack>> =
        fullDiveApi.getAvailableExchangePacks()

    fun exchangeExperience(title: String, address: String): Completable {
        return fullDiveApi.exchangeExperience(ExchangeRequest(title, address))
    }
}
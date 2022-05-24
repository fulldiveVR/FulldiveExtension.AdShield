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

package com.fulldive.wallet.remote

import com.fulldive.wallet.models.ExchangePack
import com.fulldive.wallet.models.ExchangeRequest
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FullDiveApi {

    @GET("/sleep-money/xp/change/packs")
    fun getAvailableExchangePacks(): Single<List<ExchangePack>>

    @POST("/sleep-money/xp/change")
    fun exchangeExperience(@Body exchangeRequest: ExchangeRequest): Completable
}
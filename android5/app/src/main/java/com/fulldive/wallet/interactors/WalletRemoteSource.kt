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

import com.fulldive.wallet.di.modules.DefaultLocalStorageModule
import com.fulldive.wallet.extensions.safeSingle
import com.fulldive.wallet.models.Balance
import com.fulldive.wallet.models.Chain
import com.joom.lightsaber.ProvidedBy
import cosmos.bank.v1beta1.QueryGrpc
import cosmos.bank.v1beta1.QueryOuterClass
import cosmos.base.query.v1beta1.Pagination
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.reactivex.Single
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ProvidedBy(DefaultLocalStorageModule::class)
class WalletRemoteSource @Inject constructor(
) {
    private val channel: ManagedChannel by lazy {
        ManagedChannelBuilder.forAddress(Chain.grpcApiHost.url, Chain.grpcApiHost.port)
            .usePlaintext()
            .build()
    }

    fun requestBalances(address: String, limit: Int = 1000): Single<List<Balance>> {
        return safeSingle {
            QueryGrpc.newBlockingStub(channel)
                .allBalances(
                    QueryOuterClass
                        .QueryAllBalancesRequest.newBuilder()
                        .setPagination(
                            Pagination.PageRequest.newBuilder().setLimit(limit.toLong()).build()
                        )
                        .setAddress(address)
                        .build()
                )
                .balancesList
                .map { coin ->
                    Balance(
                        BigDecimal(coin.amount),
                        coin.denom
                    )
                }
        }
    }
}
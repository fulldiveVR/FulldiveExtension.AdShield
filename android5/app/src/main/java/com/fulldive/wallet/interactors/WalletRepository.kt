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

import com.fulldive.wallet.di.modules.DefaultRepositoryModule
import com.fulldive.wallet.models.Account
import com.fulldive.wallet.models.Balance
import com.joom.lightsaber.ProvidedBy
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

@ProvidedBy(DefaultRepositoryModule::class)
class WalletRepository @Inject constructor(
    private val walletLocalSource: WalletLocalSource,
    private val walletRemoteSource: WalletRemoteSource
) {
    fun getAccount(): Single<Account> {
        return walletLocalSource.getAccount()
    }

    fun setAccount(account: Account): Completable {
        return walletLocalSource.setAccount(account)
    }

    fun hasPassword(): Single<Boolean> {
        return walletLocalSource
            .getPassword()
            .map { true }
            .onErrorReturnItem(false)
    }

    fun setPassword(password: String): Completable {
        return walletLocalSource.setPassword(password)
    }

    fun deleteAccount(): Completable {
        return walletLocalSource.deleteAccount()
    }

    fun getBalances(): Single<List<Balance>> {
        return walletLocalSource.getBalances()
    }

    fun requestBalances(address: String): Single<List<Balance>> {
        return walletRemoteSource
            .requestBalances(address)
            .flatMap { balances ->
                walletLocalSource
                    .setBalances(balances)
                    .toSingleDefault(balances)
                    .onErrorReturnItem(balances)
            }
    }
}
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

import android.content.Context
import appextension.getPrivateSharedPreferences
import com.fulldive.wallet.di.modules.DefaultLocalStorageModule
import com.fulldive.wallet.extensions.completeCallable
import com.fulldive.wallet.extensions.or
import com.fulldive.wallet.extensions.safeCompletable
import com.fulldive.wallet.extensions.safeSingle
import com.fulldive.wallet.models.Account
import com.fulldive.wallet.models.Balance
import com.fulldive.wallet.models.EncodedData
import com.fulldive.wallet.utils.CryptoHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.joom.lightsaber.ProvidedBy
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ProvidedBy(DefaultLocalStorageModule::class)
class WalletLocalSource @Inject constructor(
    context: Context
) {
    private val gson = Gson()
    private var currentAccount: Account? = null

    private var sharedPreferences = context.getPrivateSharedPreferences(KEY_CRYPTO)

    fun getAccount(): Single<Account> {
        return safeSingle {
            getCurrentAccount()
        }
    }

    fun setAccount(account: Account): Completable {
        return safeCompletable {
            setCurrentAccount(account)
        }
    }

    fun checkPassword(password: String): Single<Boolean> {
        return getPassword()
            .flatMap { encodedData ->
                safeSingle {
                    CryptoHelper.verifyData(
                        password,
                        encodedData,
                        KEY_CRYPTO_PASSWORD
                    )
                }
            }
    }

    fun getPassword(): Single<String> {
        return safeSingle {
            sharedPreferences.getString(KEY_CRYPTO, null)
        }
    }

    fun setPassword(password: String): Completable {
        return safeSingle {
            CryptoHelper.signData(
                password,
                KEY_CRYPTO_PASSWORD
            )
        }
            .onErrorResumeNext {
                safeSingle {
                    CryptoHelper.deleteKey(KEY_CRYPTO_PASSWORD)
                    CryptoHelper.signData(
                        password,
                        KEY_CRYPTO_PASSWORD
                    )
                }
            }
            .flatMapCompletable { encodedData ->
                completeCallable {
                    sharedPreferences.edit().putString(KEY_CRYPTO, encodedData).apply()
                }
            }
    }

    fun deleteAccount(): Completable {
        return safeCompletable {
            currentAccount = null
            sharedPreferences
                .edit()
                .remove(KEY_ACCOUNT)
                .remove(KEY_BALANCES)
                .apply()
        }
    }

    fun getBalances(): Single<List<Balance>> {
        return safeSingle {
            sharedPreferences
                .getString(KEY_BALANCES, null)
                ?.let { json ->
                    val type = object : TypeToken<List<Balance>>() {}.type
                    gson.fromJson(json, type)
                }
        }
    }

    fun setBalances(balances: List<Balance>): Completable {
        return safeCompletable {
            sharedPreferences
                .edit()
                .putString(KEY_BALANCES, gson.toJson(balances))
                .apply()
        }
    }

    private fun getCurrentAccount(): Account? {
        return currentAccount
            .or {
                sharedPreferences
                    .getString(KEY_ACCOUNT, null)
                    ?.let { encodedJson ->
                        gson.fromJson(encodedJson, EncodedData::class.java)
                    }
                    ?.let { encodedData ->
                        CryptoHelper.decryptData(
                            KEY_CRYPTO,
                            encodedData.resource,
                            encodedData.spec
                        )
                    }
                    ?.let { decodedJson ->
                        gson.fromJson(decodedJson, Account::class.java)
                    }
                    ?.also { account ->
                        currentAccount = account
                    }
            }
    }

    private fun setCurrentAccount(account: Account) {
        currentAccount = account
        gson
            .toJson(account)
            .let { json ->
                CryptoHelper.encryptData(
                    KEY_CRYPTO,
                    json,
                    false
                )
            }
            .let { encResult ->
                gson.toJson(EncodedData(encResult.encDataString, encResult.ivDataString))
            }
            .let { encodedJson ->
                sharedPreferences.edit().putString(KEY_ACCOUNT, encodedJson).apply()
            }

    }

    companion object {
        private const val KEY_CRYPTO = "crypto_wallet"
        private const val KEY_CRYPTO_PASSWORD = "crypto_password"

        private const val KEY_ACCOUNT = "account"
        private const val KEY_BALANCES = "balances"
    }
}
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
import com.fulldive.wallet.extensions.safeSingle
import com.fulldive.wallet.extensions.singleCallable
import com.fulldive.wallet.models.Account
import com.fulldive.wallet.models.AccountSecrets
import com.fulldive.wallet.models.Balance
import com.fulldive.wallet.models.EncResult
import com.fulldive.wallet.utils.CryptoHelper
import com.fulldive.wallet.utils.MnemonicUtils
import com.joom.lightsaber.ProvidedBy
import io.reactivex.Completable
import io.reactivex.Single
import org.bitcoinj.crypto.MnemonicCode
import java.security.SecureRandom
import java.util.*
import javax.inject.Inject

@ProvidedBy(DefaultInteractorsModule::class)
class WalletInteractor @Inject constructor(
    private val accountsRepository: WalletRepository,
) {

    fun createSecrets(path: Int = 0): Single<AccountSecrets> {
        return safeSingle {
            val entropy = getEntropy()
            val words = MnemonicCode.INSTANCE.toMnemonic(entropy)
            val hexEntropy = MnemonicUtils.byteArrayToHexString(entropy)
            val address = MnemonicUtils.createAddress(
                hexEntropy,
                path
            )

            AccountSecrets(hexEntropy, words, address, path)
        }
    }

    fun createAccount(accountSecrets: AccountSecrets): Completable {
        return singleCallable { UUID.randomUUID().toString() }
            .flatMap { uuid ->
                entropyFromMnemonic(uuid, accountSecrets.entropy)
                    .map { encryptData ->
                        Account(
                            uuid,
                            accountSecrets.address,
                            true,
                            encryptData.encDataString,
                            encryptData.ivDataString,
                            true,
                            accountSecrets.path
                        )
                    }
            }
            .flatMapCompletable(accountsRepository::setAccount)
    }

    fun hasPassword(): Single<Boolean> {
        return accountsRepository.hasPassword()
    }

    fun getAccount(): Single<Account> {
        return accountsRepository.getAccount()
    }

    fun setAccount(account: Account): Completable {
        return accountsRepository.setAccount(account)
    }

    fun deleteAccount(): Completable {
        return accountsRepository.deleteAccount()
    }

    fun getBalances(address: String): Single<List<Balance>> {
        return accountsRepository
            .requestBalances(address)
            .onErrorResumeNext(accountsRepository.getBalances())
    }

    private fun getEntropy(): ByteArray {
        return ByteArray(ENTROPY_SIZE).also { seeds ->
            SecureRandom().nextBytes(seeds)
        }
    }

    private fun entropyFromMnemonic(uuid: String, entropy: String): Single<EncResult> {
        return encryptText(MNEMONIC_KEY + uuid, entropy)
    }

    private fun encryptText(key: String, text: String): Single<EncResult> {
        return safeSingle {
            CryptoHelper.encryptData(key, text, false)
        }
    }

    companion object {
        const val PRIVATE_KEY_PREFIX = "0x"
        private const val ENTROPY_SIZE = 32

        private const val PASSWORD_KEY = "PASSWORD_KEY"
        private const val PRIVATE_KEY = "PRIVATE_KEY"
        private const val MNEMONIC_KEY = "MNEMONIC_KEY"
    }
}

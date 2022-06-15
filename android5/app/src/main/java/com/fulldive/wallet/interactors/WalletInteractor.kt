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
import com.fulldive.wallet.extensions.toSingle
import com.fulldive.wallet.models.Account
import com.fulldive.wallet.models.AccountSecrets
import com.fulldive.wallet.models.Balance
import com.fulldive.wallet.models.EncResult
import com.fulldive.wallet.utils.CryptoHelper
import com.fulldive.wallet.utils.MnemonicUtils
import com.joom.lightsaber.ProvidedBy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.MnemonicCode
import java.security.SecureRandom
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

@ProvidedBy(DefaultInteractorsModule::class)
class WalletInteractor @Inject constructor(
    private val walletRepository: WalletRepository,
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
                encryptFromMnemonic(uuid, accountSecrets.entropy)
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
            .flatMapCompletable(walletRepository::setAccount)
    }

    fun createAccount(
        address: String,
        entropy: String,
        path: Int = 0,
        fromMnemonic: Boolean
    ): Completable {
        return singleCallable { UUID.randomUUID().toString() }
            .flatMap { uuid ->
                if(fromMnemonic) {
                    encryptFromMnemonic(uuid, entropy)
                } else {
                    encryptFromPrivateKey(uuid, entropy)
                }
                    .map { encryptData ->
                        Account(
                            uuid,
                            address,
                            true,
                            encryptData.encDataString,
                            encryptData.ivDataString,
                            fromMnemonic,
                            path
                        )
                    }
            }
            .flatMapCompletable(walletRepository::setAccount)
    }

    fun setPassword(password: String): Completable {
        return walletRepository.setPassword(password)
    }

    fun checkPassword(password: String): Single<Boolean> {
        return walletRepository.checkPassword(password)
    }

    fun hasPassword(): Single<Boolean> {
        return walletRepository.hasPassword()
    }

    fun getAccount(): Single<Account> {
        return walletRepository.getAccount()
    }

    fun observeAccount(): Observable<Account> = walletRepository.observeAccount()

    fun setAccount(account: Account): Completable {
        return walletRepository.setAccount(account)
    }

    fun deleteAccount(): Completable {
        return walletRepository.deleteAccount()
    }

    fun getBalances(address: String): Single<List<Balance>> {
        return walletRepository
            .requestBalances(address)
            .onErrorResumeNext(walletRepository.getBalances())
    }

    fun isPasswordValid(text: String): Boolean {
        var result = false
        if (text.length == 5) {
            result = Pattern
                .compile("^\\d{4}+[A-Z]$")
                .matcher(text)
                .matches()
        }
        return result
    }

    fun isPrivateKeyValid(text: String): Boolean {
        return Pattern
            .compile("^(0x|0X)?[a-fA-F0-9]{64}")
            .matcher(text)
            .matches()
    }

    fun getRandomMnemonic(entropy: String): Single<List<String>> {
        return safeSingle {
            MnemonicCode.INSTANCE.toMnemonic(
                MnemonicUtils.hexStringToByteArray(entropy)
            )
        }
    }

    fun decryptFromMnemonic(uuid: String, resource: String, spec: String): Single<String> {
        return decryptText(MNEMONIC_KEY + uuid, resource, spec)
    }

    fun encryptFromMnemonic(uuid: String, entropy: String): Single<EncResult> {
        return encryptText(MNEMONIC_KEY + uuid, entropy)
    }

    fun decryptFromPrivateKey(uuid: String, resource: String, spec: String): Single<String> {
        return decryptText(PRIVATE_KEY + uuid, resource, spec)
    }

    fun encryptFromPrivateKey(uuid: String, entropy: String): Single<EncResult> {
        return encryptText(PRIVATE_KEY + uuid, entropy)
    }

    fun createKeyWithPathFromEntropy(
        entropy: String,
        path: Int
    ): Single<DeterministicKey> {
        return safeSingle {
            MnemonicUtils.createKeyWithPathFromEntropy(
                entropy, path
            )
        }
    }

    fun entropyHexFromMnemonicWords(words: List<String>): String {
        return MnemonicUtils.byteArrayToHexString(MnemonicCode.INSTANCE.toEntropy(words))
    }

    fun isValidMnemonicArray(words: Array<String>): Boolean {
        return words.size == 24 && isValidMnemonicWords(words)
    }

    fun isValidMnemonicWords(words: Array<String>): Boolean {
        val mnemonics = MnemonicCode.INSTANCE.wordList
        return words.all(mnemonics::contains)
    }

    fun checkMnemonicWords(words: Array<String>): List<Boolean> {
        val mnemonics = MnemonicCode.INSTANCE.wordList
        return words.map { word -> !mnemonics.contains(word) }
    }

    fun isValidMnemonicWord(word: String): Boolean {
        return word.isNotEmpty() && MnemonicCode.INSTANCE.wordList.contains(word)
    }

    fun getMnemonicDictionary(): Single<List<String>> {
        return MnemonicCode.INSTANCE.wordList.toSingle()
    }

    private fun getEntropy(): ByteArray {
        return ByteArray(ENTROPY_SIZE).also { seeds ->
            SecureRandom().nextBytes(seeds)
        }
    }

    private fun encryptText(key: String, text: String): Single<EncResult> {
        return safeSingle {
            CryptoHelper.encryptData(key, text, false)
        }
    }

    private fun decryptText(alias: String, resource: String, spec: String): Single<String> {
        return safeSingle {
            CryptoHelper.decryptData(alias, resource, spec)
        }
    }

    companion object {
        const val PRIVATE_KEY_PREFIX = "0x"
        private const val ENTROPY_SIZE = 32

        private const val PRIVATE_KEY = "PRIVATE_KEY"
        private const val MNEMONIC_KEY = "MNEMONIC_KEY"
    }
}

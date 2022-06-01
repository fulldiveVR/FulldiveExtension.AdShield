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

package com.fulldive.wallet.presentation.accounts.privatekey

import com.fulldive.wallet.di.modules.DefaultPresentersModule
import com.fulldive.wallet.extensions.withDefaults
import com.fulldive.wallet.extensions.withPrefix
import com.fulldive.wallet.interactors.ClipboardInteractor
import com.fulldive.wallet.interactors.WalletInteractor
import com.fulldive.wallet.interactors.WalletInteractor.Companion.PRIVATE_KEY_PREFIX
import com.fulldive.wallet.presentation.base.BaseMoxyPresenter
import com.fulldive.wallet.rx.AppSchedulers
import com.joom.lightsaber.ProvidedBy
import io.reactivex.Single
import org.adshield.R
import javax.inject.Inject

@ProvidedBy(DefaultPresentersModule::class)
class ShowPrivateKeyPresenter @Inject constructor(
    private val walletInteractor: WalletInteractor,
    private val clipboardInteractor: ClipboardInteractor
) : BaseMoxyPresenter<ShowPrivateKeyMoxyView>() {
    private var privateKey: String = ""

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        generatePrivateKey()
    }

    fun onMnemonicCopyClicked() {
        if (privateKey.isNotEmpty()) {
            clipboardInteractor
                .copyToClipboard(privateKey)
                .subscribeOn(AppSchedulers.ui())
                .observeOn(AppSchedulers.ui())
                .compositeSubscribe(
                    onSuccess = {
                        viewState.showMessage(R.string.str_copied)
                    }
                )
        }
    }

    private fun generatePrivateKey() {
        viewState.showWaitDialog()

        walletInteractor
            .getAccount()
            .flatMap { account ->
                val resource = account.resource
                val spec = account.spec
                when {
                    resource == null || spec == null -> Single.error(IllegalStateException())
                    account.fromMnemonic -> {
                        walletInteractor
                            .decryptFromMnemonic(
                                account.uuid,
                                resource,
                                spec
                            )
                            .flatMap { entropy ->
                                walletInteractor
                                    .createKeyWithPathFromEntropy(
                                        entropy,
                                        account.path
                                    )
                                    .map {
                                        it.privateKeyAsHex
                                    }
                            }
                    }
                    else -> {
                        walletInteractor
                            .decryptFromPrivateKey(
                                account.uuid,
                                resource,
                                spec
                            )
                    }
                }
            }
            .map { key ->
                key.withPrefix(PRIVATE_KEY_PREFIX)
            }
            .withDefaults()
            .compositeSubscribe(
                onSuccess = ::onPrivateKeyReceived,
                onError = object : OnErrorConsumer() {
                    override fun onError(error: Throwable) {
                        super.onError(error)
                        viewState.showMessage(R.string.str_unknown_error_msg)
                        viewState.finish()
                    }
                }
            )
    }

    private fun onPrivateKeyReceived(privateKey: String) {
        this.privateKey = privateKey
        viewState.hideWaitDialog()
        viewState.showPrivateKey(privateKey)
    }
}
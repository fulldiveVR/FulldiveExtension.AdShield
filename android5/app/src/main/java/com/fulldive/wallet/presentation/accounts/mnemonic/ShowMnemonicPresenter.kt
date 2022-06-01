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

package com.fulldive.wallet.presentation.accounts.mnemonic

import com.fulldive.wallet.di.modules.DefaultPresentersModule
import com.fulldive.wallet.extensions.withDefaults
import com.fulldive.wallet.interactors.ClipboardInteractor
import com.fulldive.wallet.interactors.WalletInteractor
import com.fulldive.wallet.presentation.base.BaseMoxyPresenter
import com.fulldive.wallet.rx.AppSchedulers
import com.joom.lightsaber.ProvidedBy
import io.reactivex.Single
import org.adshield.R
import javax.inject.Inject

@ProvidedBy(DefaultPresentersModule::class)
class ShowMnemonicPresenter @Inject constructor(
    private val walletInteractor: WalletInteractor,
    private val clipboardInteractor: ClipboardInteractor
) : BaseMoxyPresenter<ShowMnemonicMoxyView>() {
    private var mnemonic: List<String> = emptyList()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        generateMnemonic()
    }

    fun onMnemonicCopyClicked() {
        if (mnemonic.isNotEmpty()) {
            clipboardInteractor
                .copyToClipboard(mnemonic.joinToString(" ", transform = String::trim))
                .subscribeOn(AppSchedulers.ui())
                .observeOn(AppSchedulers.ui())
                .compositeSubscribe(
                    onSuccess = {
                        viewState.showMessage(R.string.str_copied)
                    }
                )
        }
    }

    private fun generateMnemonic() {
        viewState.showWaitDialog()

        walletInteractor
            .getAccount()
            .flatMap { account ->
                val resource = account.resource
                val spec = account.spec
                when {
                    resource == null || spec == null -> Single.error(IllegalStateException())
                    account.fromMnemonic -> {
                        walletInteractor.decryptFromMnemonic(
                            account.uuid,
                            resource,
                            spec
                        )
                    }
                    else -> {
                        walletInteractor.decryptFromPrivateKey(
                            account.uuid,
                            resource,
                            spec
                        )
                    }
                }
            }
            .flatMap(walletInteractor::getRandomMnemonic)
            .withDefaults()
            .compositeSubscribe(
                onSuccess = ::onMnemonicReceived,
                onError = object : OnErrorConsumer() {
                    override fun onError(error: Throwable) {
                        super.onError(error)
                        viewState.showMessage(R.string.str_unknown_error_msg)
                        viewState.finish()
                    }
                }
            )
    }

    private fun onMnemonicReceived(mnemonic: List<String>) {
        this.mnemonic = mnemonic
        viewState.hideWaitDialog()
        viewState.showMnemonic(mnemonic)
    }
}
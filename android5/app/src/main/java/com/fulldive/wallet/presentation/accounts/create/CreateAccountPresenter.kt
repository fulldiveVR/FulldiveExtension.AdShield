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

package com.fulldive.wallet.presentation.accounts.create

import com.fulldive.wallet.di.modules.DefaultPresentersModule
import com.fulldive.wallet.extensions.withDefaults
import com.fulldive.wallet.interactors.ClipboardInteractor
import com.fulldive.wallet.interactors.WalletInteractor
import com.fulldive.wallet.models.AccountSecrets
import com.fulldive.wallet.presentation.base.BaseMoxyPresenter
import com.fulldive.wallet.rx.AppSchedulers
import com.joom.lightsaber.ProvidedBy
import org.adshield.R
import javax.inject.Inject

@ProvidedBy(DefaultPresentersModule::class)
class CreateAccountPresenter @Inject constructor(
    private val walletInteractor: WalletInteractor,
    private val clipboardInteractor: ClipboardInteractor
) : BaseMoxyPresenter<CreateAccountMoxyView>() {
    private var accountSecrets: AccountSecrets? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        generateMnemonic()
    }

    fun onShowMnemonicClicked() {
        walletInteractor
            .hasPassword()
            .withDefaults()
            .compositeSubscribe(
                onSuccess = viewState::requestPassword
            )
    }

    fun onCreateAccountClicked() {
        accountSecrets?.let { secrets ->
            viewState.showWaitDialog()
            walletInteractor
                .createAccount(secrets)
                .withDefaults()
                .doOnError {
                    viewState.hideWaitDialog()
                }
                .compositeSubscribe(
                    onSuccess = {
                        viewState.finish()
                    }
                )
        }
    }

    fun onWalletAddressCopyClicked() {
        accountSecrets?.let { secrets ->
            clipboardInteractor
                .copyToClipboard(secrets.address)
                .subscribeOn(AppSchedulers.ui())
                .observeOn(AppSchedulers.ui())
                .compositeSubscribe(
                    onSuccess = {
                        viewState.showMessage(R.string.str_copied)
                    }
                )
        }
    }

    fun onMnemonicCopyClicked() {
        accountSecrets?.let { secrets ->
            clipboardInteractor
                .copyToClipboard(secrets.mnemonic.joinToString(" ", transform = String::trim))
                .subscribeOn(AppSchedulers.ui())
                .observeOn(AppSchedulers.ui())
                .compositeSubscribe(
                    onSuccess = {
                        viewState.showMessage(R.string.str_copied)
                    }
                )
        }
    }

    fun onCheckPasswordSuccessfully() {
        accountSecrets?.mnemonic?.let(viewState::showMnemonic)
    }

    private fun generateMnemonic() {
        walletInteractor
            .createSecrets()
            .withDefaults()
            .compositeSubscribe(
                onSuccess = ::onAccountSecretsReceived,
                onError = object : OnErrorConsumer() {
                    override fun onError(error: Throwable) {
                        super.onError(error)
                        viewState.finish()
                    }
                }
            )
    }

    private fun onAccountSecretsReceived(accountSecrets: AccountSecrets) {
        this.accountSecrets = accountSecrets
        viewState.showAccountAddress(accountSecrets.address)
    }
}
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

package com.fulldive.wallet.presentation.main

import com.fulldive.wallet.di.modules.DefaultPresentersModule
import com.fulldive.wallet.extensions.or
import com.fulldive.wallet.extensions.withDefaults
import com.fulldive.wallet.interactors.WalletInteractor
import com.fulldive.wallet.models.Account
import com.fulldive.wallet.models.Balance
import com.fulldive.wallet.models.Chain
import com.fulldive.wallet.presentation.accounts.AddAccountDialogFragment
import com.fulldive.wallet.presentation.base.BaseMoxyPresenter
import com.fulldive.wallet.utils.WalletHelper
import com.joom.lightsaber.ProvidedBy
import java.math.BigDecimal
import javax.inject.Inject

@ProvidedBy(DefaultPresentersModule::class)
class MainPresenter @Inject constructor(
    private val accountsInteractor: WalletInteractor
) : BaseMoxyPresenter<MainMoxyView>() {

    private var checkForMnemonic = false

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        requestAccount()
    }

    fun onCreateWalletClicked() {
        viewState.showDialog(
            AddAccountDialogFragment.newInstance(),
            "dialog",
            true
        )
    }

    fun onDeleteWalletClicked() {
        accountsInteractor
            .deleteAccount()
            .withDefaults()
            .compositeSubscribe(
                onSuccess = {
                    viewState.showCreateWalletButton()
                }
            )
    }

    fun onShowMnemonicClicked() {
        checkForMnemonic = true
        viewState.showCheckPassword()
    }

    fun onShowPrivateKeyClicked() {
        checkForMnemonic = false
        viewState.showCheckPassword()
    }

    private fun requestAccount() {
        accountsInteractor
            .getAccount()
            .withDefaults()
            .compositeSubscribe(
                onSuccess = { account ->
                    viewState.showAccount(account)
                    requestBalance(account)
                },
                onError = {
                    viewState.showCreateWalletButton()
                }
            )
    }

    private fun requestBalance(account: Account) {
        accountsInteractor
            .getBalances(account.address)
            .withDefaults()
            .compositeSubscribe(
                onSuccess = { balances ->
                    val balance = balances.find { balance ->
                        balance.denom == Chain.mainDenom
                    }.or {
                        Balance(BigDecimal.ZERO, Chain.mainDenom)
                    }
                    onBalanceReceived(balance)
                },
                onError = { error ->
                    onBalanceReceived(Balance(BigDecimal.ZERO, Chain.mainDenom))
                }
            )
    }

    private fun onBalanceReceived(balance: Balance) {
        val spannableString = WalletHelper.getReadableBalance(
            balance.amount,
            Chain.divideDecimal,
            Chain.displayDecimal
        )
        viewState.showBalance(spannableString, Chain.symbolTitle)
    }

    fun onCheckPasswordSuccessfully() {
        if (checkForMnemonic) {
            viewState.showMnemonic()
        } else {
            viewState.showPrivateKey()
        }
    }
}
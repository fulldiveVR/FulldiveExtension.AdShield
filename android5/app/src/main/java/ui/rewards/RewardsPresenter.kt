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

package ui.rewards

import com.fulldive.wallet.di.modules.DefaultModule
import com.fulldive.wallet.extensions.or
import com.fulldive.wallet.extensions.withDefaults
import com.fulldive.wallet.interactors.ClipboardInteractor
import com.fulldive.wallet.interactors.WalletInteractor
import com.fulldive.wallet.models.Account
import com.fulldive.wallet.models.Balance
import com.fulldive.wallet.models.Chain
import com.fulldive.wallet.presentation.base.BaseMoxyPresenter
import com.fulldive.wallet.rx.AppSchedulers
import com.fulldive.wallet.utils.WalletHelper
import com.joom.lightsaber.ProvidedBy
import moxy.InjectViewState
import org.adshield.R
import java.math.BigDecimal
import javax.inject.Inject

@InjectViewState
@ProvidedBy(DefaultModule::class)
class RewardsPresenter @Inject constructor(
    private val clipboardInteractor: ClipboardInteractor,
    private val accountsInteractor: WalletInteractor
) : BaseMoxyPresenter<RewardsView>() {

    override fun attachView(view: RewardsView?) {
        super.attachView(view)
        requestAccount()
    }

    fun onViewPrivateKeyClicked() {

    }

    fun onViewMnemonicClicked() {

    }

    fun onWalletAddressCopyClicked(address: String) {
        clipboardInteractor
            .copyToClipboard(address)
            .subscribeOn(AppSchedulers.ui())
            .observeOn(AppSchedulers.ui())
            .compositeSubscribe(
                onSuccess = {
                    viewState.showMessage(R.string.str_copied)
                }
            )
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
                    viewState.showCreateWalletLayout()
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
                        balance.denom == Chain.fdCoinDenom
                    }.or {
                        Balance(BigDecimal.ZERO, Chain.fdCoinDenom)
                    }
                    onBalanceReceived(balance)
                },
                onError = { error ->
                    onBalanceReceived(Balance(BigDecimal.ZERO, Chain.fdCoinDenom))
                }
            )
    }

    private fun onBalanceReceived(balance: Balance) {
        val spannableString = WalletHelper.getReadableBalance(
            balance.amount,
            Chain.divideDecimal,
            Chain.displayDecimal
        )
        viewState.showBalance(spannableString, Chain.fdCoinSymbolTitle)
    }

    //    fun onDeleteWalletClicked() {
//        accountsInteractor
//            .deleteAccount()
//            .withDefaults()
//            .compositeSubscribe(
//                onSuccess = {
//                    viewState.showCreateWalletLayout()
//                }
//            )
//    }
}
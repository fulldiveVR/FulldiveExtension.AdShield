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

package ui.rewards.board.base

import android.content.Context
import android.widget.Toast
import com.fulldive.wallet.extensions.withDefaults
import com.fulldive.wallet.interactors.ExperienceExchangeInterator
import com.fulldive.wallet.interactors.WalletInteractor
import com.fulldive.wallet.models.Account
import com.fulldive.wallet.models.Chain
import com.fulldive.wallet.presentation.base.BaseMoxyPresenter
import com.fulldive.wallet.rx.ISchedulersProvider

abstract class BaseExperiencePresenter<VS : ExperienceView> constructor(
    private val context: Context,
    private val experienceExchangeInterator: ExperienceExchangeInterator,
    private val walletInteractor: WalletInteractor,
    private val schedulers: ISchedulersProvider
) : BaseMoxyPresenter<VS>() {

    private var userExperience = 0

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        experienceExchangeInterator
            .observeIfExperienceExchangeAvailable(Chain.fdCoinDenom)
            .withDefaults()
            .compositeSubscribe(
                onNext = { (experience, minExperience, isExchangeAvailable, _, isExchangeTimeout, isEmptyAddress) ->
                    viewState.setExperience(
                        experience,
                        minExperience,
                        isExchangeAvailable,
                        isExchangeTimeout,
                        isEmptyAddress
                    )
                    if (userExperience != 0 && userExperience != experience) {
                        viewState.updateExperienceProgress(
                            experience,
                            minExperience
                        )
                    }
                    userExperience = experience
                }
            )

        experienceExchangeInterator
            .getExchangeRateForToken(Chain.fdCoinDenom)
            .withDefaults()
            .compositeSubscribe()
    }

    fun onExchangeClicked() {
        walletInteractor
            .getAccount()
            .map(Account::address)
            .onErrorReturnItem("")
            .withDefaults()
            .compositeSubscribe(
                onSuccess = { address ->
                    if (address.isEmpty()) {
                        Toast.makeText(context, "Create the wallet", Toast.LENGTH_SHORT).show()
                    } else {
                        viewState.navigateToExchangeScreen()
                    }
                }
            )
    }
}
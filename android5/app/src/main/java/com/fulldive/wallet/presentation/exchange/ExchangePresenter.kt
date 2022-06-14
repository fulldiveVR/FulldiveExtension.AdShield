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

package com.fulldive.wallet.presentation.exchange

import com.fulldive.wallet.di.modules.DefaultModule
import com.fulldive.wallet.extensions.withDefaults
import com.fulldive.wallet.interactors.ExperienceExchangeInterator
import com.fulldive.wallet.interactors.WalletInteractor
import com.fulldive.wallet.models.Chain
import com.fulldive.wallet.presentation.base.BaseMoxyPresenter
import com.fulldive.wallet.rx.ISchedulersProvider
import com.joom.lightsaber.ProvidedBy
import io.reactivex.Observable
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
@ProvidedBy(DefaultModule::class)
class ExchangePresenter @Inject constructor(
    private val walletInteractor: WalletInteractor,
    private val experienceExchangeInterator: ExperienceExchangeInterator,
    private val schedulers: ISchedulersProvider
) : BaseMoxyPresenter<ExchangeView>() {

    private var fdTokenAmount = 0
    private var userExperience = 0

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Observable.combineLatest(
            experienceExchangeInterator
                .observeIfExperienceExchangeAvailable(Chain.fdCoinDenom)
                .subscribeOn(schedulers.io()),
            experienceExchangeInterator
                .observeExchangeRateForToken(Chain.fdCoinDenom)
                .subscribeOn(schedulers.io())
        ) { exchangeConfig, rate ->
            Pair(exchangeConfig, rate)
        }
            .withDefaults()
            .compositeSubscribe(
                onNext = { (exchangeConfig, rate) ->
                    val experience = exchangeConfig.experience
                    fdTokenAmount = experienceExchangeInterator
                        .getAvailableTokenAmount(experience, rate)
                    userExperience = exchangeConfig.experience
                    viewState.showUserExperience(
                        experience = experience,
                        minExchangeExperience = exchangeConfig.minExperience,
                        availableFdTokens = fdTokenAmount
                    )
                    viewState.experienceIsValid(exchangeConfig.isExchangeAvailable)
                }
            )
    }

    fun exchangeExperience() {
        walletInteractor
            .getAccount()
            .flatMapCompletable { account ->
                experienceExchangeInterator
                    .exchangeExperience(
                        Chain.fdCoinDenom,
                        userExperience,
                        account.address
                    ).andThen(experienceExchangeInterator.removeExchangedExperience())
            }
            .withDefaults()
            .compositeSubscribe(
                onSuccess = {
                    viewState.showSuccessExchange(userExperience)
                }
            )
    }
}
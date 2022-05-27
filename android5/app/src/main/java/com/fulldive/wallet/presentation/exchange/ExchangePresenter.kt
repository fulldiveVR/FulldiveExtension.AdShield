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

import android.util.Log
import com.fulldive.wallet.di.modules.DefaultModule
import com.fulldive.wallet.extensions.toSingle
import com.fulldive.wallet.extensions.withDefaults
import com.fulldive.wallet.interactors.ExperienceExchangeInterator
import com.fulldive.wallet.interactors.WalletInteractor
import com.fulldive.wallet.models.ExchangeRequest
import com.fulldive.wallet.presentation.base.BaseMoxyPresenter
import com.fulldive.wallet.rx.ISchedulersProvider
import com.joom.lightsaber.ProvidedBy
import io.reactivex.Observable
import io.reactivex.Single
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
@ProvidedBy(DefaultModule::class)
class ExchangePresenter @Inject constructor(
    private val walletInteractor: WalletInteractor,
    private val experienceExchangeInterator: ExperienceExchangeInterator,
    private val schedulers: ISchedulersProvider
) : BaseMoxyPresenter<ExchangeView>() {

    private var userExperience = 0
    private var minExchangeExperience = 0
    private var rate: Int = 0

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        Observable.combineLatest(
            experienceExchangeInterator
                .observeExperience().subscribeOn(schedulers.io()),
            experienceExchangeInterator
                .observeExchangeRateForToken(ExchangeRequest.DENOM_FD_TOKEN)
                .subscribeOn(schedulers.io())
        ) { (experience, minExchangeExperience), rate ->
            Triple(experience, minExchangeExperience, rate)
        }
            .withDefaults()
            .compositeSubscribe(
                onNext = { (experience, minExchangeExperience, rate) ->
                    this.userExperience = experience
                    this.minExchangeExperience = minExchangeExperience
                    this.rate = rate
                    val fdTokenAmount = experienceExchangeInterator
                        .getAvailableTokenAmount(experience, rate)
                    viewState.showUserExperience(
                        experience = experience,
                        minExchangeExperience = minExchangeExperience,
                        availableFdTokens = fdTokenAmount
                    )
                }
            )
    }

    fun validateExperience(experienceString: String) {
        experienceString
            .toSingle()
            .map { experienceString ->
                val experience = if (experienceString.isEmpty()) 0 else experienceString.toInt()
                val isValid = experience >= minExchangeExperience &&
                        experience in 1..userExperience
                Log.d("TestB", "isValid $isValid")
                val availableFdTokens = experienceExchangeInterator.getAvailableTokenAmount(
                    experience,
                    rate
                )
                Pair(availableFdTokens, isValid)
            }.withDefaults()
            .compositeSubscribe(
                onSuccess = { (availableFdTokens, isValid) ->
                    viewState.showAvailableFdTokens(availableFdTokens)
                    viewState.experienceIsValid(isValid)
                }
            )
    }

    fun exchangeExperience(experience: String) {
        Single.zip(
            experience.toSingle().subscribeOn(schedulers.io()),
            walletInteractor.getAccount().subscribeOn(schedulers.io())
        ) { experienceString, account ->
            Pair(experienceString.toInt(), account)
        }
            .flatMapCompletable { (experience, account) ->
                experienceExchangeInterator
                    .exchangeExperience(
                        ExchangeRequest.DENOM_FD_TOKEN,
                        experienceExchangeInterator.getAvailableTokenAmount(experience, rate),
                        account.address
                    ).andThen(experienceExchangeInterator.removeExchangedExperience())
            }
            .withDefaults()
            .compositeSubscribe(
                onSuccess = {
                    //todo success && close
                    Log.d("TestB", "Success exchange!!!")
                }
            )
    }
}
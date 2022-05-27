package ui.rewards.board.base

import com.fulldive.wallet.extensions.withDefaults
import com.fulldive.wallet.interactors.ExperienceExchangeInterator
import com.fulldive.wallet.models.ExchangeRequest
import com.fulldive.wallet.presentation.base.BaseMoxyPresenter
import com.fulldive.wallet.rx.ISchedulersProvider

abstract class BaseExperiencePresenter<VS : ExperienceView> constructor(
    private val experienceExchangeInterator: ExperienceExchangeInterator,
    private val schedulers: ISchedulersProvider
) : BaseMoxyPresenter<VS>() {

    private var userExperience = 0

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        experienceExchangeInterator
            .observeIfExperienceExchangeAvailable(ExchangeRequest.DENOM_FD_TOKEN)
            .withDefaults()
            .compositeSubscribe(
                onNext = { (experience, minExperience, isExchangeAvailable) ->
                    if (userExperience == 0 || userExperience == experience) {
                        viewState.setExperience(experience, minExperience, isExchangeAvailable)
                    } else {
                        viewState.updateExperienceProgress(
                            experience,
                            minExperience,
                            isExchangeAvailable
                        )
                    }
                    userExperience = experience
                }
            )

        experienceExchangeInterator
            .getExchangeRateForToken(ExchangeRequest.DENOM_FD_TOKEN)
            .withDefaults()
            .compositeSubscribe()
    }
}
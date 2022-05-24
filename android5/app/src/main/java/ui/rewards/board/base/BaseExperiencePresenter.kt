package ui.rewards.board.base

import com.fulldive.wallet.extensions.withDefaults
import com.fulldive.wallet.interactors.ExperienceExchangeInterator
import com.fulldive.wallet.presentation.base.BaseMoxyPresenter
import com.fulldive.wallet.rx.ISchedulersProvider
import io.reactivex.Observable

abstract class BaseExperiencePresenter<VS : ExperienceView> constructor(
    private val experienceExchangeInterator: ExperienceExchangeInterator,
    private val schedulers: ISchedulersProvider
) : BaseMoxyPresenter<VS>() {

    private var userExperience = 0

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Observable.combineLatest(
            experienceExchangeInterator.observeExperience().subscribeOn(schedulers.io()),
            experienceExchangeInterator.observeIfExchangeTimeIntervalPassed()
                .subscribeOn(schedulers.io()),
            experienceExchangeInterator.observeExchangePacks().subscribeOn(schedulers.io()),
        ) { (experience, maxExperience), isExchangeAvailable, exchangePacks ->
            Triple(
                experience,
                maxExperience,
                (exchangePacks.isNotEmpty() && isExchangeAvailable && experience >= maxExperience)
            )
        }
            .withDefaults()
            .compositeSubscribe(
                onNext = { (experience, maxExperience, isExchangeAvailable) ->
                    if (userExperience == 0 || userExperience == experience) {
                        viewState.setExperience(experience, maxExperience, isExchangeAvailable)
                    } else {
                        viewState.updateExperienceProgress(
                            experience,
                            maxExperience,
                            isExchangeAvailable
                        )
                    }
                    userExperience = experience
                }
            )

        experienceExchangeInterator
            .getAvailableExchangePacks()
            .withDefaults()
            .compositeSubscribe()
    }

    fun onExchangeClicked() {
        //todo clear if exchange is successful
        experienceExchangeInterator
            .clearExchangedExperience()
            .withDefaults()
            .compositeSubscribe()
    }
}
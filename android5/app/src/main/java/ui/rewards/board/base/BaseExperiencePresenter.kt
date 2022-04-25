package ui.rewards.board.base

import com.fulldive.wallet.extensions.withDefaults
import com.fulldive.wallet.presentation.base.BaseMoxyPresenter
import com.fulldive.wallet.rx.ISchedulersProvider
import io.reactivex.Observable
import service.AppSettingsService

abstract class BaseExperiencePresenter<VS : ExperienceView> constructor(private val schedulers: ISchedulersProvider) :
    BaseMoxyPresenter<VS>() {

    private var isFirstAttach = true

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Observable.combineLatest(
            AppSettingsService.observeExperience().subscribeOn(schedulers.io()),
            AppSettingsService.observeIfExchangeTimeIntervalPassed().subscribeOn(schedulers.io())
        ) { (experience, maxExperience), isExchangeAvailable ->
            Triple(experience, maxExperience, isExchangeAvailable && experience >= maxExperience)
        }
            .withDefaults()
            .compositeSubscribe(
                onNext = { (experience, maxExperience, isExchangeAvailable) ->
                    if (isFirstAttach) {
                        isFirstAttach = false
                        viewState.setExperience(experience, maxExperience, isExchangeAvailable)
                    } else {
                        viewState.updateExperienceProgress(
                            experience,
                            maxExperience,
                            isExchangeAvailable
                        )
                    }
                }
            )
    }

    fun onExchangeClicked() {
        //todo clear if exchange is successful
        AppSettingsService
            .clearExchangedExperience()
            .withDefaults()
            .compositeSubscribe()
    }
}
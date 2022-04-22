package ui.rewards.board.base

import com.fulldive.wallet.presentation.base.BaseMoxyView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface ExperienceView : BaseMoxyView {
    fun setExperience(experience: Int, maxExperience: Int, isExchangeAvailable: Boolean)
    fun setProgress(progress: Int, maxProgress: Int)
    fun updateExperienceProgress(experience: Int, maxExperience: Int)
}
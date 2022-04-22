package ui.rewards.board

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import ui.rewards.board.base.ExperienceView

@StateStrategyType(AddToEndSingleStrategy::class)
interface ExperienceBoardView : ExperienceView
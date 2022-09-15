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

import com.fulldive.wallet.presentation.base.BaseMoxyView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface ExperienceView : BaseMoxyView {
    fun setExperience(experience: Int, maxExperience: Int, isExchangeAvailable: Boolean, isExchangeTimeout: Boolean, isEmptyAddress: Boolean)
    fun setProgress(progress: Int, maxProgress: Int)
    fun updateExperienceProgress(experience: Int, maxExperience: Int)
    fun navigateToExchangeScreen()
}
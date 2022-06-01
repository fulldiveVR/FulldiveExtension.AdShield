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
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.viewbinding.ViewBinding
import com.fulldive.wallet.extensions.fromHtmlToSpanned
import com.fulldive.wallet.extensions.getHexColor
import com.fulldive.wallet.extensions.unsafeLazy
import com.fulldive.wallet.presentation.base.BaseMvpFrameLayout
import org.adshield.R
import ui.rewards.RewardsFragmentDirections
import ui.rewards.board.ExperienceProgressViewLayout

abstract class BaseExperienceLayout<V : ViewBinding, P> : BaseMvpFrameLayout<V>,
    ExperienceView where P : BaseExperiencePresenter<out ExperienceView> {

    abstract val experienceProgressViewLayout: ExperienceProgressViewLayout?
    abstract val experienceProgressTextView: TextView?
    abstract val experienceTextView: TextView?
    abstract val exchangeButton: TextView?

    abstract var presenter: P

    private val levelAnimationSet by unsafeLazy {
        AnimationSet(true)
            .apply {
                interpolator = DecelerateInterpolator()
                fillAfter = true
                isFillEnabled = true
            }
    }

    private val levelAnimation by unsafeLazy {
        RotateAnimation(
            0.0f,
            -3.0f,
            RotateAnimation.RELATIVE_TO_SELF,
            0.5f,
            RotateAnimation.RELATIVE_TO_SELF,
            0.5f
        ).apply {
            repeatCount = REPEAT_ROTATION_COUNT
            fillAfter = true
            repeatMode = Animation.REVERSE
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun initLayout() {
        super.initLayout()
        exchangeButton?.setOnClickListener {
            findNavController()
                .apply {
                    navigate(RewardsFragmentDirections.actionNavigationRewardsToExchangeFragment())
                }
        }
    }

    override fun setExperience(experience: Int, maxExperience: Int, isExchangeAvailable: Boolean) {
        setExperienceProgressText(experience, maxExperience)
        experienceProgressViewLayout?.setProgress(experience, maxExperience)
        exchangeButton?.isVisible = isExchangeAvailable
    }

    override fun setProgress(progress: Int, maxProgress: Int) {
        experienceProgressViewLayout?.setProgress(
            progress = progress,
            maxProgress = maxProgress
        )
    }

    override fun updateExperienceProgress(
        experience: Int,
        maxExperience: Int,
        isExchangeAvailable: Boolean
    ) {
        experienceProgressViewLayout?.animateExperience(
            progress = experience,
            maxProgress = maxExperience,
            animationDuration = ANIMATION_DURATION,
            endAction = { setExperienceProgressText(experience, maxExperience) }
        )
        exchangeButton?.isVisible = isExchangeAvailable
    }

    private fun setExperienceProgressText(experience: Int, maxLevelExperience: Int) {
        experienceTextView?.text = "$experience"
        experienceProgressTextView?.text = fromHtmlToSpanned(
            context.getString(R.string.experience_description)
                .replace("%experienceTextColor%", context.getHexColor(R.color.textColorPrimary))
                .replace("%currentExperience%", experience.toString())
                .replace("%maxLevelExperience%", maxLevelExperience.toString())
        )
    }

    companion object {
        private const val ANIMATION_DURATION = 1000L
        private const val REPEAT_ROTATION_COUNT = 5
    }
}
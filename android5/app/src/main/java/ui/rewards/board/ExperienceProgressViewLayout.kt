package ui.rewards.board

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import appextension.or
import org.adshield.R
import utils.find
import utils.unsafeLazy

class ExperienceProgressViewLayout : FrameLayout {

    private val experienceProgressBarView by unsafeLazy { find<ProgressBar>(R.id.experienceProgressBarView) }

    private var progressViewHeight = 0

    constructor(context: Context) : super(context) {
        initLayout()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initLayout(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initLayout(attrs)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initLayout(attrs)
    }


    fun loadAttrs(attrs: AttributeSet?) {
        attrs?.let {
            val styledAttrs =
                context.obtainStyledAttributes(attrs, R.styleable.ExperienceProgressLayout, 0, 0)
            progressViewHeight = styledAttrs.getDimensionPixelSize(
                R.styleable.ExperienceProgressLayout_progressViewHeight,
                R.dimen.size_12dp
            )
            styledAttrs.recycle()
        }.or {
            progressViewHeight = context.resources.getDimensionPixelSize(R.dimen.size_12dp)
        }
    }

    fun setProgress(progress: Int, maxProgress: Int) {
        experienceProgressBarView.max = maxProgress
        experienceProgressBarView.progress = progress
    }

    fun getProgress() = experienceProgressBarView.progress

    fun animationStarted(
        maxProgress: Int,
        levelUpAnimationDuration: Long
    ) {
        experienceProgressBarView.max = maxProgress
        animateProgress(
            progress = maxProgress,
            delayBeforeStart = 0,
            animationDuration = levelUpAnimationDuration
        )
    }

    fun onLevelAnimationEnded(
        progress: Int,
        maxProgress: Int,
        animationDuration: Long
    ) {
        setProgress(0, maxProgress)
        maxProgress
            .takeIf { it > 0 }
            ?.let { max ->
                animateProgress(
                    progress = progress,
                    delayBeforeStart = EXPERIENCE_PROGRESS_ANIMATION_DELAY,
                    animationDuration = (animationDuration * (progress)) / max
                )
            }
    }

    fun animateExperience(
        progress: Int,
        maxProgress: Int,
        animationDuration: Long,
        endAction: (() -> Unit)
    ) {
        experienceProgressBarView.max = maxProgress
        maxProgress
            .takeIf { it > 0 }
            ?.let { max ->
                animateProgress(
                    progress,
                    EXPERIENCE_PROGRESS_ANIMATION_DELAY,
                    (animationDuration * (progress - getProgress())) / max,
                    endAction
                )
            }
    }

    private fun animateProgress(
        progress: Int,
        delayBeforeStart: Long,
        animationDuration: Long,
        endAction: (() -> Unit)? = null
    ) {
        ObjectAnimator.ofInt(
            experienceProgressBarView,
            "progress",
            getProgress(),
            progress
        ).apply {
            duration = animationDuration
            interpolator = DecelerateInterpolator()
            startDelay = delayBeforeStart
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    endAction?.invoke()
                }

                override fun onAnimationStart(animation: Animator?) = Unit
                override fun onAnimationCancel(animation: Animator?) = Unit
                override fun onAnimationRepeat(animation: Animator?) = Unit
            }
            )
            start()
        }
    }

    private fun initLayout(attrs: AttributeSet? = null) {
        loadAttrs(attrs)
        LayoutInflater.from(context).inflate(R.layout.layout_experience_progress_view, this)
        experienceProgressBarView.layoutParams = ConstraintLayout
            .LayoutParams(MATCH_PARENT, progressViewHeight)
    }

    companion object {
        private const val EXPERIENCE_PROGRESS_ANIMATION_DELAY = 400L
    }
}
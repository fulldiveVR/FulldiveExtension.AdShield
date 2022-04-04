package com.fulldive.evry.presentation.tabs

import android.animation.TimeInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlin.math.roundToInt

object AnimationUtils {
    @JvmField
    val FAST_OUT_SLOW_IN_INTERPOLATOR: TimeInterpolator = FastOutSlowInInterpolator()
    fun lerp(startValue: Float, endValue: Float, fraction: Float): Float {
        return startValue + fraction * (endValue - startValue)
    }

    fun lerp(startValue: Int, endValue: Int, fraction: Float): Int {
        return startValue + (fraction * (endValue - startValue)).roundToInt()
    }
}
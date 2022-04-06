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

package ui.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.Dimension
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import com.fulldive.evry.presentation.tabs.AnimationUtils
import com.fulldive.evry.presentation.tabs.dpToPx
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal class SlidingTabIndicator(private val tabLayout: TabLayout, context: Context) :
    LinearLayout(context) {
    var selectedPosition = -1
    var selectionOffset = 0f
    var marginBottom: Int = 0

    private var selectedIndicatorHeight = 0
    private val defaultSelectionIndicator by lazy(LazyThreadSafetyMode.NONE) { GradientDrawable() }
    private var indicatorLeft = -1
    private var indicatorRight = -1
    private var indicatorAnimator: ValueAnimator? = null

    init {
        setWillNotDraw(false)
    }

    fun setSelectedIndicatorHeight(height: Int) {
        if (selectedIndicatorHeight != height) {
            selectedIndicatorHeight = height
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    fun childrenNeedLayout(): Boolean {
        var i = 0
        val z = childCount
        while (i < z) {
            val child = getChildAt(i)
            if (child.width <= 0) {
                return true
            }
            i++
        }
        return false
    }

    fun setIndicatorPositionFromTabPosition(position: Int, positionOffset: Float) {
        val indicatorAnimator = indicatorAnimator
        if (indicatorAnimator?.isRunning == true) {
            indicatorAnimator.cancel()
        }
        selectedPosition = position
        selectionOffset = positionOffset
        updateIndicatorPosition()
    }


    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        super.onRtlPropertiesChanged(layoutDirection)

        // Workaround for a bug before Android M where LinearLayout did not re-layout itself when
        // layout direction changed
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (this.layoutDirection != layoutDirection) {
                requestLayout()
                this.layoutDirection = layoutDirection
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            // HorizontalScrollView will first measure use with UNSPECIFIED, and then with
            // EXACTLY. Ignore the first call since anything we do will be overwritten anyway
            return
        }

        // GRAVITY_CENTER will make all tabs the same width as the largest tab, and center them in the
        // SlidingTabIndicator's width (with a "gutter" of padding on either side). If the Tabs do not
        // fit in the SlidingTabIndicator, then fall back to GRAVITY_FILL behavior.
        if (tabLayout.tabGravity == TabLayout.GRAVITY_CENTER || tabLayout.mode == TabLayout.MODE_AUTO) {
            val count = childCount

            // First we'll find the widest tab
            var largestTabWidth = 0
            var i = 0
            while (i < count) {
                val child = getChildAt(i)
                if (child.visibility == View.VISIBLE) {
                    largestTabWidth = max(largestTabWidth, child.measuredWidth)
                }
                i++
            }
            if (largestTabWidth <= 0) {
                // If we don't have a largest child yet, skip until the next measure pass
                return
            }
            val gutter = dpToPx(context, TabLayout.FIXED_WRAP_GUTTER_MIN).toInt()
            var remeasure = false
            if (largestTabWidth * count <= measuredWidth - gutter * 2) {
                // If the tabs fit within our width minus gutters, we will set all tabs to have
                // the same width
                for (i in 0 until count) {
                    val lp = getChildAt(i).layoutParams as LayoutParams
                    if (lp.width != largestTabWidth || lp.weight != 0f) {
                        lp.width = largestTabWidth
                        lp.weight = 0f
                        remeasure = true
                    }
                }
            } else {
                // If the tabs will wrap to be larger than the width minus gutters, we need
                // to switch to GRAVITY_FILL.
                // TODO (b/129799806): This overrides the user TabGravity setting.
                tabLayout.tabGravity = TabLayout.GRAVITY_FILL
                tabLayout.updateTabViews(false)
                remeasure = true
            }
            if (remeasure) {
                // Now re-measure after our changes
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        } else if (tabLayout.gravityToEnd) {
            val count = childCount
            var largestTabWidth = 0
            var i = 0
            while (i < count) {
                val child = getChildAt(i)
                if (child.visibility == View.VISIBLE) {
                    largestTabWidth = max(largestTabWidth, child.measuredWidth)
                }
                i++
            }
            if (largestTabWidth <= 0) {
                // If we don't have a largest child yet, skip until the next measure pass
                return
            }
            val gutter = dpToPx(context, TabLayout.FIXED_WRAP_GUTTER_MIN).toInt()
            gravity = if (largestTabWidth * count <= measuredWidth - gutter * 2) {
                Gravity.END
            } else {
                Gravity.START
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        val indicatorAnimator = indicatorAnimator
        if (indicatorAnimator?.isRunning == true) {
            // If we're currently running an animation, lets cancel it and start a
            // new animation with the remaining duration
            indicatorAnimator.cancel()
            val duration = indicatorAnimator.duration
            animateIndicatorToPosition(
                selectedPosition,
                ((1f - indicatorAnimator.animatedFraction) * duration).roundToInt()
            )
        } else {
            // If we've been layed out, update the indicator position
            updateIndicatorPosition()
        }
    }

    private fun updateIndicatorPosition() {
        val selectedTitle = getChildAt(selectedPosition)
        var left: Int
        var right: Int
        if (selectedTitle != null && selectedTitle.width > 0) {
            left = selectedTitle.left
            right = selectedTitle.right
            if (!tabLayout.tabIndicatorFullWidth && selectedTitle is TabView) {
                calculateTabViewContentBounds(selectedTitle, tabLayout.tabViewContentBounds)
                left = tabLayout.tabViewContentBounds.left.toInt()
                right = tabLayout.tabViewContentBounds.right.toInt()
            }
            if (selectionOffset > 0f && selectedPosition < childCount - 1) {
                // Draw the selection partway between the tabs
                val nextTitle = getChildAt(selectedPosition + 1)
                var nextTitleLeft = nextTitle.left
                var nextTitleRight = nextTitle.right
                if (!tabLayout.tabIndicatorFullWidth && nextTitle is TabView) {
                    calculateTabViewContentBounds(nextTitle, tabLayout.tabViewContentBounds)
                    nextTitleLeft = tabLayout.tabViewContentBounds.left.toInt()
                    nextTitleRight = tabLayout.tabViewContentBounds.right.toInt()
                }
                left = (selectionOffset * nextTitleLeft + (1.0f - selectionOffset) * left).toInt()
                right =
                    (selectionOffset * nextTitleRight + (1.0f - selectionOffset) * right).toInt()
            }
        } else {
            right = -1
            left = right
        }
        setIndicatorPosition(left, right)
    }

    fun animateIndicatorToPosition(position: Int, duration: Int) {
        indicatorAnimator?.let { indicatorAnimator ->
            if (indicatorAnimator.isRunning) {
                indicatorAnimator.cancel()
            }
        }
        val targetView = getChildAt(position)
        if (targetView == null) {
            // If we don't have a view, just update the position now and return
            updateIndicatorPosition()
            return
        }
        var targetLeft = targetView.left
        var targetRight = targetView.right
        if (!tabLayout.tabIndicatorFullWidth && targetView is TabView) {
            calculateTabViewContentBounds(targetView, tabLayout.tabViewContentBounds)
            targetLeft = tabLayout.tabViewContentBounds.left.toInt()
            targetRight = tabLayout.tabViewContentBounds.right.toInt()
        }
        val finalTargetLeft = targetLeft
        val finalTargetRight = targetRight
        val startLeft = indicatorLeft
        val startRight = indicatorRight
        if (startLeft != finalTargetLeft || startRight != finalTargetRight) {
            val animator = ValueAnimator().also { indicatorAnimator = it }
            animator.interpolator = AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR
            animator.duration = duration.toLong()
            animator.setFloatValues(0f, 1f)
            animator.addUpdateListener { valueAnimator ->
                val fraction = valueAnimator.animatedFraction
                setIndicatorPosition(
                    AnimationUtils.lerp(startLeft, finalTargetLeft, fraction),
                    AnimationUtils.lerp(startRight, finalTargetRight, fraction)
                )
            }
            animator.addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animator: Animator) {
                        selectedPosition = position
                        selectionOffset = 0f
                    }
                })
            animator.start()
        }
    }

    private fun calculateTabViewContentBounds(
        tabView: TabView, contentBounds: RectF
    ) {
        var tabViewContentWidth = tabView.contentWidth
        val minIndicatorWidth = dpToPx(context, MIN_INDICATOR_WIDTH).toInt()
        if (tabViewContentWidth < minIndicatorWidth) {
            tabViewContentWidth = minIndicatorWidth
        }
        val tabViewCenter = (tabView.left + tabView.right) / 2
        val contentLeftBounds = tabViewCenter - tabViewContentWidth / 2
        val contentRightBounds = tabViewCenter + tabViewContentWidth / 2
        contentBounds[contentLeftBounds.toFloat(), 0f, contentRightBounds.toFloat()] = 0f
    }

    override fun draw(canvas: Canvas) {
        var indicatorHeight = 0
        tabLayout.tabSelectedIndicator?.let { tabSelectedIndicator ->
            indicatorHeight = tabSelectedIndicator.intrinsicHeight
        }
        if (selectedIndicatorHeight >= 0) {
            indicatorHeight = selectedIndicatorHeight
        }
        var indicatorTop = 0
        var indicatorBottom = 0

        when (tabLayout.tabIndicatorGravity) {
            TabLayout.INDICATOR_GRAVITY_BOTTOM -> {
                indicatorTop = max(0, height - indicatorHeight - marginBottom)
                indicatorBottom = max(0, height - marginBottom)
            }
            TabLayout.INDICATOR_GRAVITY_CENTER -> {
                indicatorTop = max(0, (height - indicatorHeight) / 2 - marginBottom)
                indicatorBottom = min(indicatorTop + indicatorHeight, height)
            }
            TabLayout.INDICATOR_GRAVITY_TOP -> {
                indicatorTop = 0
                indicatorBottom = max(0, indicatorHeight - marginBottom)
            }
            TabLayout.INDICATOR_GRAVITY_STRETCH -> {
                indicatorTop = 0
                indicatorBottom = max(0, height - marginBottom)
            }
        }

        // Draw the selection indicator on top of tab item backgrounds
        if (indicatorLeft in 0 until indicatorRight) {
            val selectedIndicator = DrawableCompat.wrap(
                tabLayout.tabSelectedIndicator ?: defaultSelectionIndicator
            )
            selectedIndicator.setBounds(
                indicatorLeft,
                indicatorTop,
                indicatorRight,
                indicatorBottom
            )
            selectedIndicator.draw(canvas)
        }

        // Draw the tab item contents (icon and label) on top of the background + indicator layers
        super.draw(canvas)
    }

    private fun setIndicatorPosition(left: Int, right: Int) {
        if (left != indicatorLeft || right != indicatorRight) {
            // If the indicator's left/right has changed, invalidate
            indicatorLeft = left
            indicatorRight = right
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    companion object {
        @Dimension(unit = Dimension.DP)
        val MIN_INDICATOR_WIDTH = 24
    }
}
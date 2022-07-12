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

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.core.view.isVisible
import org.adshield.R

class CircleProgressBar : View {

    private var startTime: Long = 0L
    private var postedHide = false
    private var postedShow = false
    private var dismissed = false
    private val delayedHide = Runnable {
        postedHide = false
        startTime = 0L
        isVisible = false
    }
    private val delayedShow = Runnable {
        postedShow = false
        if (!dismissed) {
            startTime = System.currentTimeMillis()
            isVisible = true
        }
    }

    private val indicator: Indicator = BallSpinFadeLoaderIndicator()

    private var indicatorColor: Int = 0
        set(value) {
            field = value
            indicator.color = value
        }

    private var shouldStartAnimationDrawable: Boolean = false

    constructor(context: Context) : super(context) {
        init(context, null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr, 0)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init(context, attrs, defStyleAttr, 0)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.CircleProgressBar,
            defStyleAttr,
            defStyleRes
        )
        indicatorColor = typedArray.getColor(
            R.styleable.CircleProgressBar_indicatorColor,
            0
        )
        typedArray.recycle()

        indicator.callback = this@CircleProgressBar
    }

    fun hide() {
        dismissed = true
        removeCallbacks(delayedShow)
        val diff = System.currentTimeMillis() - startTime
        if (diff >= MIN_SHOW_TIME || startTime == 0L) {
            // The progress spinner has been shown long enough
            // OR was not shown yet. If it wasn't shown yet,
            // it will just never be shown.
            isVisible = false
        } else {
            // The progress spinner is shown, but not long enough,
            // so put a delayed message in to hide it when its been
            // shown long enough.
            if (!postedHide) {
                postDelayed(delayedHide, MIN_SHOW_TIME - diff)
                postedHide = true
            }
        }
    }

    fun show() {
        // Reset the start time.
        startTime = 0L
        dismissed = false
        removeCallbacks(delayedHide)
        if (!postedShow) {
            postDelayed(delayedShow, MIN_DELAY.toLong())
            postedShow = true
        }
    }

    override fun verifyDrawable(who: Drawable) = who === indicator || super.verifyDrawable(who)

    private fun startAnimation() {
        if (visibility == VISIBLE) {
            shouldStartAnimationDrawable = true
            postInvalidate()
        }
    }

    private fun stopAnimation() {
        if (visibility == VISIBLE) {
            indicator.stop()
            shouldStartAnimationDrawable = false
            postInvalidate()
        }
    }

    override fun setVisibility(v: Int) {
        if (visibility != v) {
            super.setVisibility(v)
            startOrStopAnimation()
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        startOrStopAnimation()
    }

    override fun invalidateDrawable(dr: Drawable) {
        if (verifyDrawable(dr)) {
            invalidate()
        } else {
            super.invalidateDrawable(dr)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        updateDrawableBounds(w, h)
    }

    private fun startOrStopAnimation() {
        if (visibility == VISIBLE) startAnimation() else stopAnimation()
    }

    private fun updateDrawableBounds(width: Int, height: Int) {
        var w = width
        var h = height
        // onDraw will translate the canvas so we draw starting at 0,0.
        // Subtract out padding for the purposes of the calculations below.
        w -= paddingRight + paddingLeft
        h -= paddingTop + paddingBottom

        var right = w
        var bottom = h
        var top = 0
        var left = 0

        // Maintain aspect ratio. Certain kinds of animated drawables
        // get very confused otherwise.
        val intrinsicWidth = indicator.intrinsicWidth
        val intrinsicHeight = indicator.intrinsicHeight
        val intrinsicAspect = intrinsicWidth.toFloat() / intrinsicHeight
        val boundAspect = w.toFloat() / h
        if (intrinsicAspect != boundAspect) {
            if (boundAspect > intrinsicAspect) {
                // New width is larger. Make it smaller to match height.
                val widthNew = (h * intrinsicAspect).toInt()
                left = (w - widthNew) / 2
                right = left + widthNew
            } else {
                // New height is larger. Make it smaller to match width.
                val heightNew = (w * (1 / intrinsicAspect)).toInt()
                top = (h - heightNew) / 2
                bottom = top + heightNew
            }
        }
        indicator.setBounds(left, top, right, bottom)
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawTrack(canvas)
    }

    private fun drawTrack(canvas: Canvas) {
        val saveCount = canvas.save()
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        indicator.draw(canvas)
        canvas.restoreToCount(saveCount)
        if (shouldStartAnimationDrawable) {
            (indicator as Animatable).start()
            shouldStartAnimationDrawable = false
        }
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var dw = 0
        var dh = 0

        updateDrawableState()

        dw += paddingLeft + paddingRight
        dh += paddingTop + paddingBottom

        val measuredWidth = resolveSizeAndState(dw, widthMeasureSpec, 0)
        val measuredHeight = resolveSizeAndState(dh, heightMeasureSpec, 0)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        updateDrawableState()
    }

    private fun updateDrawableState() {
        if (indicator.isStateful) {
            indicator.state = drawableState
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun drawableHotspotChanged(x: Float, y: Float) {
        super.drawableHotspotChanged(x, y)
        indicator.setHotspot(x, y)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()
        removeCallbacks()
    }

    override fun onDetachedFromWindow() {
        stopAnimation()
        removeCallbacks()
        super.onDetachedFromWindow()
    }

    private fun removeCallbacks() {
        removeCallbacks(delayedHide)
        removeCallbacks(delayedShow)
    }

    companion object {
        private const val MIN_SHOW_TIME = 150 // ms
        private const val MIN_DELAY = 150 // ms
    }
}

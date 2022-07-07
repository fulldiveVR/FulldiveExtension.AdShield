package ui.utils

import android.animation.ValueAnimator
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable

abstract class Indicator : Drawable(), Animatable {

    private lateinit var updateListener: ValueAnimator.AnimatorUpdateListener

    private var animator = ValueAnimator()
    private var alpha = 255
    private var drawBounds = ZERO_BOUNDS_RECT
        set(value) {
            field = Rect(value.left, value.top, value.right, value.bottom)
        }

    private var hasAnimators: Boolean = false

    private val paint = Paint()

    var color: Int
        get() = paint.color
        set(value) {
            paint.color = value
        }

    private val isStarted: Boolean
        get() {
            return animator.isStarted
        }

    val width: Int get() = drawBounds.width()

    val height: Int get() = drawBounds.height()

    init {
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
    }

    override fun setAlpha(alpha: Int) {
        this.alpha = alpha
    }

    override fun getAlpha(): Int = alpha

    override fun getOpacity(): Int = PixelFormat.OPAQUE

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    override fun draw(canvas: Canvas) = draw(canvas, paint)

    abstract fun draw(canvas: Canvas, paint: Paint)

    abstract fun onCreateAnimator(): ValueAnimator

    override fun start() {
        ensureAnimator()

        if (!isStarted) {
            startAnimator()
            invalidateSelf()
        }
    }

    private fun startAnimator() {
        animator.addUpdateListener(updateListener)
        animator.start()
    }

    private fun stopAnimator() {
        if (animator.isStarted) {
            animator.removeAllUpdateListeners()
            animator.end()
        }
    }

    private fun ensureAnimator() {
        if (!hasAnimators) {
            stopAnimator()
            animator = onCreateAnimator()
            hasAnimators = true
        }
    }

    override fun stop() = stopAnimator()

    override fun isRunning(): Boolean {
        return animator.isRunning
    }

    fun setUpdateListener(updateListener: ValueAnimator.AnimatorUpdateListener) {
        this.updateListener = updateListener
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        drawBounds = bounds
    }

    fun postInvalidate() = invalidateSelf()

    companion object {
        private val ZERO_BOUNDS_RECT = Rect()
    }
}
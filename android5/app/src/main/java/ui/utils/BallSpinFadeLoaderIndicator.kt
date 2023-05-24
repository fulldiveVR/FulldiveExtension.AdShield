package ui.utils

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import android.view.animation.LinearInterpolator
import kotlin.math.cos
import kotlin.math.sin

/**
 * Original idea and code are taken from https://github.com/81813780/AVLoadingIndicatorView
 */
class BallSpinFadeLoaderIndicator : Indicator() {

    private val scaleFloats = FloatArray(CIRCLES_COUNT)
    private val alphas = IntArray(CIRCLES_COUNT)
    private val valueAnimator: ValueAnimator get() = ValueAnimator.ofInt(0, CIRCLES_COUNT)
    private val radius: Float get() = (0.8F * width) / CIRCLES_COUNT
    private val points by lazy {
        arrayListOf<Point>().apply {
            (0 until CIRCLES_COUNT).forEach { i ->
                this.add(
                    circleAt(
                        width,
                        height,
                        width / 2 - radius,
                        i * ((Math.PI * 2.0) / CIRCLES_COUNT)
                    )
                )
            }
        }
    }

    init {
        (0 until CIRCLES_COUNT).forEach { i ->
            scaleFloats[i] = interpolate(i, 0, CIRCLES_COUNT - 1, SCALE_MIN, 1f)
            alphas[i] = interpolate(i, 0, CIRCLES_COUNT - 1, ALPHA_MIN, 255f).toInt()
        }
    }

    private var animationIndex = 0
        set(value) {
            if (field != value) {
                field = value
                postInvalidate()
            }
        }

    override fun draw(canvas: Canvas, paint: Paint) {
        val radius = this.radius
        for (i in 0 until CIRCLES_COUNT) {
            val index = (i + animationIndex) % CIRCLES_COUNT
            canvas.save()
            val point = points[index]
            canvas.translate(point.x, point.y)
            canvas.scale(scaleFloats[i], scaleFloats[i])
            paint.alpha = alphas[i]
            canvas.drawCircle(0f, 0f, radius, paint)
            canvas.restore()
        }
    }

    override fun onCreateAnimator(): ValueAnimator {
        val scaleAnim = valueAnimator.apply {
            duration = DURATION
            repeatCount = REPEAT_COUNT
            startDelay = 0
            interpolator = LinearInterpolator()
        }
        setUpdateListener { animation ->
            animationIndex = (animation.animatedValue as Int)
        }
        return scaleAnim
    }

    private fun circleAt(width: Int, height: Int, radius: Float, angle: Double): Point {
        val x = (width / 2.0 + radius * cos(angle)).toFloat()
        val y = (height / 2.0 + radius * sin(angle)).toFloat()
        return Point(x, y)
    }

    private fun interpolate(
        value: Int,
        min: Int,
        max: Int,
        minValue: Float,
        maxValue: Float
    ): Float {
        return if (maxValue == minValue || min == max) {
            minValue
        } else {
            minValue + (maxValue - minValue) * (value - min).toFloat() / (max - min).toFloat()
        }
    }

    private class Point(var x: Float, var y: Float)

    companion object {
        private const val ALPHA_MIN = 77f
        private const val SCALE_MIN = 0.3f
        private const val DURATION = 500L
        private const val REPEAT_COUNT = -1
        private const val CIRCLES_COUNT = 12
    }
}
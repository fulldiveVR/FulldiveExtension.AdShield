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

package ui.base

import android.R
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.fulldive.wallet.extensions.setColor

abstract class BaseCompoundView<VB : ViewBinding> : ConstraintLayout {

    protected var binding: VB? = null

    constructor(context: Context) : super(context) {
        initLayout()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initLayout()
        loadAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initLayout()
        loadAttrs(attrs)
    }

    abstract fun loadAttrs(attrs: AttributeSet)

    abstract fun getViewBinding(): VB

    fun initLayout() {
        getViewBinding().apply {
            binding = this
        }.root
    }

    override fun onDetachedFromWindow() {
        binding = null
        super.onDetachedFromWindow()
    }

    protected fun binding(viewBinding: VB.() -> Unit) {
        binding?.apply { viewBinding() }
    }

    /**
     * Elevation support for compound views
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outlineProvider = CustomOutline(w, h)
        }
    }

    fun getDrawable(styledAttrs: TypedArray, id: Int, color: Int): Drawable {
        return ContextCompat.getDrawable(context, styledAttrs.getResourceId(id, 0))
            ?.apply { setColor(color) }
            ?: throw IllegalArgumentException("Resource $id was not found")
    }

    fun getDrawable(id: Int, color: Int): Drawable {
        return ContextCompat
            .getDrawable(context, id)
            ?.apply { setColor(color) }
            ?: throw IllegalArgumentException("Resource $id was not found")
    }

    fun setTextColorState(textView: TextView, colorActive: Int, colorPressed: Int) {
        textView.setTextColor(
            ColorStateList(
                arrayOf(
                    intArrayOf(R.attr.state_pressed),
                    intArrayOf(-R.attr.state_pressed)
                ),
                intArrayOf(colorPressed, colorActive)
            )
        )
    }
}

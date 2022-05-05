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

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import appextension.getColorCompat
import org.adshield.R

abstract class BaseSettingItem<VB : ViewBinding> : BaseCompoundView<VB> {

    abstract val textView: TextView?
    abstract val arrowImageView: ImageView?
    abstract val separateTopView: View?
    abstract val separateBottomView: View?

    private val colorBackgroundActive: Int
        get() = context.getColorCompat(R.color.colorOppositePrimary)
    private val colorBackgroundPressed: Int
        get() = context.getColorCompat(R.color.colorBar)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun loadAttrs(attrs: AttributeSet) {
        val colorActive = context.getColorCompat(R.color.textColorPrimary)
        val colorPressed = context.getColorCompat(R.color.textColorTertiary)

        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.SettingsItem, 0, 0)
        binding?.apply {
            styledAttrs.getString(R.styleable.SettingsItem_settingsItemTitle)?.let { text ->
                textView?.let { textView ->
                    textView.text = text
                    val colorTextActive = styledAttrs.getColor(
                        R.styleable.SettingsItem_settingsItemTitleColor,
                        colorActive
                    )
                    setTextColorState(textView, colorTextActive, colorPressed)
                }
            }

            val showTopSeparator = styledAttrs.getBoolean(
                R.styleable.SettingsItem_settingsItemShowTopSeparator, false
            )
            separateTopView?.isVisible = showTopSeparator

            val showBottomSeparator = styledAttrs.getBoolean(
                R.styleable.SettingsItem_settingsItemShowBottomSeparator,
                true
            )
            separateBottomView?.isVisible =showBottomSeparator

            val showArrow = styledAttrs.getBoolean(
                R.styleable.SettingsItem_settingsItemShowArrow, true
            )
            arrowImageView?.isVisible = showArrow
        }
        setCommonAttributes(styledAttrs)
        styledAttrs.recycle()
    }

    fun setText(text: String) {
        textView?.text = text
    }

    protected open fun setCommonAttributes(styledAttrs: TypedArray) = Unit
}
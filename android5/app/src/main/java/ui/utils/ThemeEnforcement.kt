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

package com.fulldive.evry.presentation.tabs

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import com.google.android.material.R

object ThemeEnforcement {
    private val APPCOMPAT_CHECK_ATTRS = intArrayOf(R.attr.colorPrimary)
    private const val APPCOMPAT_THEME_NAME = "Theme.AppCompat"
    private val MATERIAL_CHECK_ATTRS = intArrayOf(R.attr.colorPrimaryVariant)
    private const val MATERIAL_THEME_NAME = "Theme.MaterialComponents"

    @JvmStatic
    fun obtainStyledAttributes(
        context: Context,
        set: AttributeSet,
        @StyleableRes attrs: IntArray,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int,
        @StyleableRes vararg textAppearanceResIndices: Int
    ): TypedArray {
        checkCompatibleTheme(context, set, defStyleAttr, defStyleRes)
        checkTextAppearance(
            context,
            set,
            attrs,
            defStyleAttr,
            defStyleRes,
            *textAppearanceResIndices
        )
        return context.obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes)
    }

    private fun checkCompatibleTheme(
        context: Context,
        set: AttributeSet,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) {
        val a = context.obtainStyledAttributes(
            set, R.styleable.ThemeEnforcement, defStyleAttr, defStyleRes
        )
        val enforceMaterialTheme =
            a.getBoolean(R.styleable.ThemeEnforcement_enforceMaterialTheme, false)
        a.recycle()
        if (enforceMaterialTheme) {
            val isMaterialTheme = TypedValue()
            val resolvedValue =
                context.theme.resolveAttribute(R.attr.isMaterialTheme, isMaterialTheme, true)
            if (!resolvedValue
                || isMaterialTheme.type == TypedValue.TYPE_INT_BOOLEAN && isMaterialTheme.data == 0
            ) {
                // If we were unable to resolve isMaterialTheme boolean attribute, or isMaterialTheme is
                // false, check for Material Theme color attributes
                checkMaterialTheme(context)
            }
        }
        checkAppCompatTheme(context)
    }

    private fun checkTextAppearance(
        context: Context,
        set: AttributeSet,
        @StyleableRes attrs: IntArray,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int,
        @StyleableRes vararg textAppearanceResIndices: Int
    ) {
        val themeEnforcementAttrs = context.obtainStyledAttributes(
            set, R.styleable.ThemeEnforcement, defStyleAttr, defStyleRes
        )
        val enforceTextAppearance = themeEnforcementAttrs.getBoolean(
            R.styleable.ThemeEnforcement_enforceTextAppearance,
            false
        )
        if (!enforceTextAppearance) {
            themeEnforcementAttrs.recycle()
            return
        }
        val validTextAppearance: Boolean = if (textAppearanceResIndices.isEmpty()) {
            // No custom TextAppearance attributes passed in, check android:textAppearance
            themeEnforcementAttrs.getResourceId(
                R.styleable.ThemeEnforcement_android_textAppearance,
                -1
            ) != -1
        } else {
            // Check custom TextAppearances are valid
            isCustomTextAppearanceValid(
                context,
                set,
                attrs,
                defStyleAttr,
                defStyleRes,
                *textAppearanceResIndices
            )
        }
        themeEnforcementAttrs.recycle()
        require(validTextAppearance) {
            ("This component requires that you specify a valid TextAppearance attribute. Update your "
                + "app theme to inherit from Theme.MaterialComponents (or a descendant).")
        }
    }

    private fun isCustomTextAppearanceValid(
        context: Context,
        set: AttributeSet,
        @StyleableRes attrs: IntArray,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int,
        @StyleableRes vararg textAppearanceResIndices: Int
    ): Boolean {
        val componentAttrs = context.obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes)
        for (customTextAppearanceIndex in textAppearanceResIndices) {
            if (componentAttrs.getResourceId(customTextAppearanceIndex, -1) == -1) {
                componentAttrs.recycle()
                return false
            }
        }
        componentAttrs.recycle()
        return true
    }

    private fun checkAppCompatTheme(context: Context) {
        checkTheme(context, APPCOMPAT_CHECK_ATTRS, APPCOMPAT_THEME_NAME)
    }

    private fun checkMaterialTheme(context: Context) {
        checkTheme(context, MATERIAL_CHECK_ATTRS, MATERIAL_THEME_NAME)
    }

    private fun isTheme(context: Context, themeAttributes: IntArray): Boolean {
        val a = context.obtainStyledAttributes(themeAttributes)
        for (i in themeAttributes.indices) {
            if (!a.hasValue(i)) {
                a.recycle()
                return false
            }
        }
        a.recycle()
        return true
    }

    private fun checkTheme(
        context: Context, themeAttributes: IntArray, themeName: String
    ) {
        require(isTheme(context, themeAttributes)) {
            ("The style on this component requires your app theme to be "
                + themeName
                + " (or a descendant).")
        }
    }
}
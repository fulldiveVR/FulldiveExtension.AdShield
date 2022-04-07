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
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.StyleableRes
import androidx.appcompat.content.res.AppCompatResources

object MaterialResources {
    @JvmStatic
    fun getColorStateList(
        context: Context, attributes: TypedArray, @StyleableRes index: Int
    ): ColorStateList? {
        if (attributes.hasValue(index)) {
            val resourceId = attributes.getResourceId(index, 0)
            if (resourceId != 0) {
                val value = AppCompatResources.getColorStateList(context, resourceId)
                if (value != null) {
                    return value
                }
            }
        }
        return attributes.getColorStateList(index)
    }

    @JvmStatic
    fun getDrawable(
        context: Context, attributes: TypedArray, @StyleableRes index: Int
    ): Drawable? {
        if (attributes.hasValue(index)) {
            val resourceId = attributes.getResourceId(index, 0)
            if (resourceId != 0) {
                val value = AppCompatResources.getDrawable(context, resourceId)
                if (value != null) {
                    return value
                }
            }
        }
        return attributes.getDrawable(index)
    }

    fun getDimensionPixelSize(
        context: Context,
        attributes: TypedArray,
        @StyleableRes index: Int,
        defaultValue: Int
    ): Int {
        val value = TypedValue()
        if (!attributes.getValue(index, value) || value.type != TypedValue.TYPE_ATTRIBUTE) {
            return attributes.getDimensionPixelSize(index, defaultValue)
        }
        val styledAttrs = context.theme.obtainStyledAttributes(intArrayOf(value.data))
        val dimension = styledAttrs.getDimensionPixelSize(0, defaultValue)
        styledAttrs.recycle()
        return dimension
    }
}
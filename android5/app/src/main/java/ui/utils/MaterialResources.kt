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
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

package com.fulldive.wallet.extensions

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

fun Context.getColorCompat(@ColorRes resId: Int) = ContextCompat.getColor(this, resId)
fun Context.getDrawableCompat(resId: Int) = ContextCompat.getDrawable(this, resId)
fun View.getColorCompat(@ColorRes resId: Int) = ContextCompat.getColor(context, resId)

fun Context.toast(message: Int): Toast = Toast
    .makeText(this.applicationContext, message, Toast.LENGTH_SHORT)
    .apply {
        show()
    }

fun Context.toast(message: CharSequence): Toast = Toast
    .makeText(this.applicationContext, message, Toast.LENGTH_SHORT)
    .apply {
        show()
    }


fun Context.longToast(message: Int): Toast = Toast
    .makeText(this.applicationContext, message, Toast.LENGTH_LONG)
    .apply {
        show()
    }

fun Context.longToast(message: CharSequence): Toast = Toast
    .makeText(this.applicationContext, message, Toast.LENGTH_LONG)
    .apply {
        show()
    }

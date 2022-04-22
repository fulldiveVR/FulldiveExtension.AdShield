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

package utils

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat

inline fun <reified T> unsafeLazy(noinline initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE, initializer)

inline fun <reified T : View> View.find(@IdRes id: Int): T = findViewById(id)

fun fromHtmlToSpanned(html: String?): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html.orEmpty(), Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(html.orEmpty())
    }
}

fun <T> T?.or(value: T) = this ?: value

fun Context.getHexColor(@ColorRes id: Int): String {
    return String.format("#%06x", ContextCompat.getColor(this, id).and(0xffffff))
}
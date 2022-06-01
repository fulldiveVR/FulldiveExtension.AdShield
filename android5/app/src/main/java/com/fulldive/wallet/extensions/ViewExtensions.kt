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

import android.app.Activity
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.fulldive.wallet.presentation.base.BaseMvpFrameLayout
import timber.log.Timber

fun ViewGroup.forEachChild(action: (View) -> Unit) {
    for (i in 0 until childCount) {
        action(getChildAt(i))
    }
}

fun Fragment.clearUi() {
    (this.view as? ViewGroup)?.clear()
}

fun Activity.clearUi() {
    (this.findViewById<View>(android.R.id.content).rootView as? ViewGroup)?.clear()
}

fun ViewGroup.clear() {
    this.forEachChild { view ->
        try {
            if (view is BaseMvpFrameLayout<*>) {
                view.onDestroy()
            }
            when (view) {
                is ViewGroup -> {
                    if (view is RecyclerView) {
                        view.clearOnScrollListeners()
                        view.adapter = null
                    }
                    view.clear()
                }
                is ImageView -> {
                    view.setImageDrawable(null)
                    view.setImageResource(0)
                    view.setImageURI(null)
                }
                is Toolbar -> view.setNavigationOnClickListener(null)
                is SearchView -> view.setOnQueryTextListener(null)
                is AdapterView<*> -> view.setOnItemClickListener(null)
            }
            if (view !is AdapterView<*>) {
                view.setOnClickListener(null)
            }
            view.setOnLongClickListener(null)
            view.setOnTouchListener(null)
            view.setOnKeyListener(null)
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }
}

fun Context.getHexColor(@ColorRes id: Int): String {
    return String.format("#%06x", ContextCompat.getColor(this, id).and(0xffffff))
}

fun Drawable.setColor(color: Int, mode: PorterDuff.Mode = PorterDuff.Mode.SRC_ATOP) {
    this.mutate().colorFilter = PorterDuffColorFilter(color, mode)
}

inline fun <reified T : View> View.find(@IdRes id: Int): T = findViewById(id)

fun fromHtmlToSpanned(html: String?): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html.orEmptyString(), Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(html.orEmptyString())
    }
}

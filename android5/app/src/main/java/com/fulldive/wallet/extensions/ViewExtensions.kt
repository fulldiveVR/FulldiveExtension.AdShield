package com.fulldive.wallet.extensions

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
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

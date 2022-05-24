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

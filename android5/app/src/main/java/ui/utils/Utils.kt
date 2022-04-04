package com.fulldive.evry.presentation.tabs

import android.content.Context
import android.util.TypedValue
import androidx.annotation.Dimension

fun dpToPx(context: Context, @Dimension(unit = Dimension.DP) dp: Int): Float {
    val r = context.resources
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), r.displayMetrics)
}

inline fun <T1, T2> runIfNotNullable(p1: T1?, p2: T2?, block: (p1: T1, p2: T2) -> Unit) {
    if (p1 != null && p2 != null) {
        block.invoke(p1, p2)
    }
}
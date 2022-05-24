package ui.base

import android.annotation.TargetApi
import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class CustomOutline internal constructor(
    internal var width: Int,
    internal var height: Int
) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        outline.setRect(0, 0, width, height)
    }
}

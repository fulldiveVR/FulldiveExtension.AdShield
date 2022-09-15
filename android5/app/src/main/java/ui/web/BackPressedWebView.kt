package ui.web

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent

open class BackPressedWebView : ThemedWebView {
    var backListener: BackClickListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {

        setOnKeyListener { _, keyCode, event ->
            var result = false
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    if (event.isLongPress) {
                        backListener?.let {
                            it.onLongBackClick()
                            result = true
                        }
                    }
                } else {
                    if (event.action == KeyEvent.ACTION_UP) {
                        backListener?.let {
                            it.onBackClick()
                            result = true
                        }
                    }
                }
            }
            result
        }
    }
}

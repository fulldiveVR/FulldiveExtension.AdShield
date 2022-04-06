package ui.home

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import org.adshield.R

abstract class BaseFrameLayout : FrameLayout {

    abstract val layoutResId: Int

    constructor(context: Context) : super(context) {
        initLayout()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        loadAttrs(attrs)
        initLayout()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        loadAttrs(attrs)
        initLayout()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        loadAttrs(attrs)
        initLayout()
    }

    protected open fun loadAttrs(attrs: AttributeSet?) = Unit

    @CallSuper
    protected open fun initLayout() {
        LayoutInflater.from(context).inflate(layoutResId, this)
    }
}
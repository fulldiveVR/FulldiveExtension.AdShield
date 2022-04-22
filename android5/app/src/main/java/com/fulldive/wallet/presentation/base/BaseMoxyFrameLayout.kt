package com.fulldive.wallet.presentation.base

import android.content.Context
import android.util.AttributeSet
import androidx.viewbinding.ViewBinding

abstract class BaseMoxyFrameLayout<VB : ViewBinding> : MoxyFrameLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    protected var binding: VB? = null

    abstract fun getViewBinding(): VB

    override fun inflateLayout() {
        getViewBinding().apply {
            binding = this
        }.root
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }

    protected fun binding(viewBinding: VB.() -> Unit) {
        binding?.apply { viewBinding() }
    }
}
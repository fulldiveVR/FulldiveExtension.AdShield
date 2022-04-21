package com.fulldive.wallet.presentation.base

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.viewbinding.ViewBinding
import com.fulldive.wallet.di.IEnrichableActivity
import com.fulldive.wallet.extensions.clearUi
import com.fulldive.wallet.extensions.toast
import com.joom.lightsaber.Injector
import moxy.MvpAppCompatActivity
import timber.log.Timber

abstract class BaseMvpActivity<VB : ViewBinding> : MvpAppCompatActivity(), IEnrichableActivity {
    private lateinit var injector: Injector

    protected var binding: VB? = null

    abstract fun getViewBinding(): VB

    override var appInjector: Injector
        get() = injector
        set(value) {
            injector = value
        }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            try {
                getViewBinding().also { binding = it }.root
            } catch (ex: Exception) {
                Timber.e(ex)
                throw ex
            }
        )
    }

    @CallSuper
    override fun onDestroy() {
        clearUi()
        binding = null
        super.onDestroy()
    }

    open fun showMessage(@StringRes resourceId: Int) {
        toast(resourceId)
    }

    open fun showMessage(message: String) {
        toast(message)
    }

    protected fun binding(viewBinding: VB.() -> Unit) {
        binding?.apply { viewBinding() }
    }
}
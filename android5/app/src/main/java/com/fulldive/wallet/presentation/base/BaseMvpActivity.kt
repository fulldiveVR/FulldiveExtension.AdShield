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

package com.fulldive.wallet.presentation.base

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.fulldive.wallet.di.IEnrichableActivity
import com.fulldive.wallet.extensions.clearUi
import com.fulldive.wallet.extensions.or
import com.fulldive.wallet.extensions.toast
import com.fulldive.wallet.presentation.system.WaitDialogFragment
import com.joom.lightsaber.Injector
import moxy.MvpAppCompatActivity
import timber.log.Timber

abstract class BaseMvpActivity<VB : ViewBinding> : MvpAppCompatActivity(), IEnrichableActivity {
    private lateinit var injector: Injector
    private var waitDialogFragment: WaitDialogFragment? = null

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

    open fun showDialog(
        dialogFragment: DialogFragment,
        tag: String = "dialog",
        cancelable: Boolean = true
    ) {
        dialogFragment.isCancelable = cancelable
        supportFragmentManager
            .beginTransaction()
            .add(dialogFragment, tag)
            .commitNowAllowingStateLoss()
    }

    fun showWaitDialog() {
        val fragment = supportFragmentManager.findFragmentByTag(TAG_WAIT_DIALOG)
        if (fragment == null || !fragment.isAdded) {
            val dialogFragment = waitDialogFragment.or {
                WaitDialogFragment.newInstance().also {
                    waitDialogFragment = it
                }
            }
            showDialog(dialogFragment, TAG_WAIT_DIALOG, false)
        }
    }

    fun hideWaitDialog() {
        waitDialogFragment?.dismissAllowingStateLoss()
    }

    protected fun binding(viewBinding: VB.() -> Unit) {
        binding?.apply { viewBinding() }
    }

    companion object {
        private const val TAG_WAIT_DIALOG = "wait"
    }
}
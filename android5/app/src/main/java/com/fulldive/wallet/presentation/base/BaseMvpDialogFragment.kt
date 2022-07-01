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

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.fulldive.wallet.di.IEnrichableActivity
import com.fulldive.wallet.di.IInjectorHolder
import com.fulldive.wallet.extensions.clearUi
import com.fulldive.wallet.extensions.toast
import com.joom.lightsaber.Injector
import moxy.MvpAppCompatDialogFragment

abstract class BaseMvpDialogFragment<VB : ViewBinding> : MvpAppCompatDialogFragment(),
    IInjectorHolder {

    val appInjector: Injector
        get() = (activity as IEnrichableActivity).appInjector

    protected var binding: VB? = null

    override fun getInjector() = appInjector

    abstract fun getViewBinding(): VB

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = getViewBinding().also { binding = it }.root

        return AlertDialog.Builder(activity)
            .setView(view)
            .create()
            .also(::onDialogCreated)
    }

    override fun onDestroyView() {
        binding = null
        clearUi()
        super.onDestroyView()
    }

    override fun getContext(): Context {
        return super.getContext() as Context
    }

    protected fun binding(viewBinding: VB.() -> Unit) {
        binding?.apply { viewBinding() }
    }

    open fun onDialogCreated(alertDialog: AlertDialog) {
    }

    open fun showMessage(@StringRes resourceId: Int) {
        context?.toast(resourceId)
    }

    open fun showMessage(message: String) {
        context?.toast(message)
    }

    open fun showDialog(
        dialogFragment: DialogFragment,
        tag: String = "dialog",
        cancelable: Boolean = true
    ) {
        dialogFragment.isCancelable = cancelable
        childFragmentManager
            .beginTransaction()
            .add(dialogFragment, tag)
            .commitNowAllowingStateLoss()
    }
}
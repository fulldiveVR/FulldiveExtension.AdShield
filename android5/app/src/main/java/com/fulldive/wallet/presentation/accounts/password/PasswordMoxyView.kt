package com.fulldive.wallet.presentation.accounts.password

import androidx.annotation.StringRes
import com.fulldive.wallet.presentation.base.BaseMoxyView
import com.fulldive.wallet.presentation.system.keyboard.KeyboardType
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

interface PasswordMoxyView : BaseMoxyView {

    @OneExecution
    fun showWaitDialog()

    @OneExecution
    fun hideWaitDialog()

    @AddToEndSingle
    fun setTitle(@StringRes textId: Int)

    @AddToEndSingle
    fun setMessage(@StringRes textId: Int)

    @OneExecution
    fun shakeView()

    @AddToEndSingle
    fun switchKeyboard(type: KeyboardType)

    @AddToEndSingle
    fun updatePasswordField(inputLength: Int)

    @OneExecution
    fun shuffleKeyboard()

    @OneExecution
    fun clear()

    @OneExecution
    fun finishWithResult(resultCode: Int)
}

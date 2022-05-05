package com.fulldive.wallet.presentation.accounts.mnemonic

import com.fulldive.wallet.presentation.base.BaseMoxyView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

interface RestoreMnemonicMoxyView : BaseMoxyView {

    @AddToEndSingle
    fun setDictionary(words: List<String>)

    @OneExecution
    fun setFieldError(index: Int, isError: Boolean)

    @OneExecution
    fun updateField(index: Int, text: String, requestFocus: Boolean)

    @OneExecution
    fun updateFields(items: Array<String>, errors: List<Boolean>, focusedFieldIndex: Int)

    @OneExecution
    fun requestPassword(checkPassword: Boolean)

    @OneExecution
    fun showWaitDialog()

    @OneExecution
    fun hideWaitDialog()

    @OneExecution
    fun finish()
}

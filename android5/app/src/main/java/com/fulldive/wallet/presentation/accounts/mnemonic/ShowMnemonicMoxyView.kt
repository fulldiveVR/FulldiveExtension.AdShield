package com.fulldive.wallet.presentation.accounts.mnemonic

import com.fulldive.wallet.presentation.base.BaseMoxyView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

interface ShowMnemonicMoxyView : BaseMoxyView {
    @AddToEndSingle
    fun showMnemonic(mnemonicWords: List<String>)

    @OneExecution
    fun showWaitDialog()

    @OneExecution
    fun hideWaitDialog()

    @OneExecution
    fun finish()
}

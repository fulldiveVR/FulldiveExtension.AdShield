package com.fulldive.wallet.presentation.accounts.privatekey

import com.fulldive.wallet.presentation.base.BaseMoxyView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

interface ShowPrivateKeyMoxyView : BaseMoxyView {
    @AddToEndSingle
    fun showPrivateKey(text: String)

    @OneExecution
    fun showWaitDialog()

    @OneExecution
    fun hideWaitDialog()

    @OneExecution
    fun finish()
}

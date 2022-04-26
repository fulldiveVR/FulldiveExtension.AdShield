package com.fulldive.wallet.presentation.keyboard.alphabet

import com.fulldive.wallet.presentation.base.BaseMoxyView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

interface AlphabetKeyboardMoxyView : BaseMoxyView {
    @AddToEndSingle
    fun showKeys(keyboardKeys: List<Char>)

    @OneExecution
    fun notifyKeyClicked(key: Char)
}

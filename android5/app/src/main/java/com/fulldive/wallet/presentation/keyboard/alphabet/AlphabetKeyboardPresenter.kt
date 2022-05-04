package com.fulldive.wallet.presentation.keyboard.alphabet

import com.fulldive.wallet.di.modules.DefaultPresentersModule
import com.fulldive.wallet.presentation.base.BaseMoxyPresenter
import com.joom.lightsaber.ProvidedBy
import javax.inject.Inject

@ProvidedBy(DefaultPresentersModule::class)
class AlphabetKeyboardPresenter @Inject constructor() :
    BaseMoxyPresenter<AlphabetKeyboardMoxyView>() {

    private var keyboardKeys = emptyList<Char>()
    private var uppercase = false
    private var shuffle = false
    private var leftButtonTextId = 0
    private var rightButtonTextId = 0

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        updateKeyboard()
    }


    fun onKeyClicked(index: Int) {
        keyboardKeys.getOrNull(index)?.let(viewState::notifyKeyClicked)
    }

    fun onShuffleChanged(value: Boolean) {
        if (shuffle != value) {
            shuffle = value
            updateKeyboard()
        }
    }

    fun onUppercaseChanged(value: Boolean) {
        if (uppercase != value) {
            uppercase = value
            updateKeyboard()
        }
    }

    private fun updateKeyboard() {
        var keys = ALPHABET_KEYS

        if (uppercase) {
            keys = keys.uppercase()
        }
        val charKeys = keys.toCharArray()
        if (shuffle) {
            charKeys.shuffle()
        }
        keyboardKeys = charKeys.toList()
        viewState.showKeys(keyboardKeys)
    }

    companion object {
        private const val ALPHABET_KEYS = "qwertyuiopasdfghjklzxcvbnm"
    }
}
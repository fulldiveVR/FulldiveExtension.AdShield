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
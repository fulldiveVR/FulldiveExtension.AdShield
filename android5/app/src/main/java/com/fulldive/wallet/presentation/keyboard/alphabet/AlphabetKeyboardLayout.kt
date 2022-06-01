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

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.core.view.isVisible
import com.fulldive.wallet.presentation.base.BaseMvpFrameLayout
import com.joom.lightsaber.getInstance
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import org.adshield.databinding.LayoutKeyboardAlphabetBinding

class AlphabetKeyboardLayout : BaseMvpFrameLayout<LayoutKeyboardAlphabetBinding>,
    AlphabetKeyboardMoxyView {

    var onKeyListener: ((Char) -> Unit)? = null
    var onNextKeyListener: (() -> Unit)? = null
    var onDeleteKeyListener: (() -> Unit)? = null

    override fun getViewBinding() = LayoutKeyboardAlphabetBinding
        .inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @InjectPresenter
    lateinit var presenter: AlphabetKeyboardPresenter

    @ProvidePresenter
    fun providePresenter() = appInjector.getInstance<AlphabetKeyboardPresenter>()

    override fun afterInitLayout() {
        super.afterInitLayout()
        binding {
            root.applyToViews<Button>(BUTTON_PREFIX, KEYS_COUNT) { index, button ->
                button.setOnClickListener {
                    presenter.onKeyClicked(index)
                }
            }
            deleteButton.setOnClickListener {
                onDeleteKeyListener?.invoke()
            }
            nextButton.setOnClickListener {
                onNextKeyListener?.invoke()
            }
        }
    }

    override fun showKeys(keyboardKeys: List<Char>) {
        binding {
            root.applyToViews<Button>(BUTTON_PREFIX, KEYS_COUNT) { index, button ->
                button.text = keyboardKeys[index].toString()
            }
        }
    }

    override fun notifyKeyClicked(key: Char) {
        onKeyListener?.invoke(key)
    }

    fun setShuffle(value: Boolean) {
        presenter.onShuffleChanged(value)
    }

    fun setUppercase(value: Boolean) {
        presenter.onUppercaseChanged(value)
    }

    fun setNextButtonVisible(value: Boolean) {
        binding?.nextButton?.isVisible = value
    }

    private fun <T> View.applyToViews(
        prefix: String,
        count: Int,
        block: (Int, T) -> Unit
    ) {
        (0 until count).forEach { index ->
            block(
                index,
                findViewById(
                    resources.getIdentifier(
                        "$prefix$index",
                        "id",
                        context.packageName
                    )
                )
            )
        }
    }

    companion object {
        private const val BUTTON_PREFIX = "alphabetButton"
        private const val KEYS_COUNT = 26
    }
}
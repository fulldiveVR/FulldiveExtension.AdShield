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

package com.fulldive.wallet.presentation.accounts.mnemonic

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.fulldive.wallet.presentation.base.BaseMvpFrameLayout
import com.fulldive.wallet.utils.MnemonicUtils
import org.adshield.R
import org.adshield.databinding.LayoutMnemonicBinding

class MnemonicLayout : BaseMvpFrameLayout<LayoutMnemonicBinding> {
    override fun getViewBinding() = LayoutMnemonicBinding
        .inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setMnemonicWords(mnemonicWords: List<String>) {
        binding {

            mnemonicsContainer.applyToViews<TextView>(
                WORD_PREFIX,
                MnemonicUtils.MNEMONIC_WORDS_COUNT
            ) { index, textView ->
                textView.text = if (index < mnemonicWords.size) {
                    context.getString(
                        R.string.str_mnemonic_template,
                        index + 1,
                        mnemonicWords[index]
                    )
                } else {
                    ""
                }
            }
        }
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
        private const val WORD_PREFIX = "mnemonicTextView"
    }
}
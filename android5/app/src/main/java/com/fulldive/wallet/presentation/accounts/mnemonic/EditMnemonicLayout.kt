package com.fulldive.wallet.presentation.accounts.mnemonic

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.EditText
import android.widget.TextView
import com.fulldive.wallet.extensions.getColorCompat
import com.fulldive.wallet.extensions.orFalse
import com.fulldive.wallet.presentation.base.BaseMvpFrameLayout
import com.fulldive.wallet.utils.MnemonicUtils
import org.adshield.R
import org.adshield.databinding.LayoutMnemonicEditBinding

class EditMnemonicLayout : BaseMvpFrameLayout<LayoutMnemonicEditBinding> {

    var onFocusChangeListener: ((Int) -> Unit)? = null

    override fun getViewBinding() = LayoutMnemonicEditBinding
        .inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun afterInitLayout() {
        super.afterInitLayout()
        binding {
            mnemonicsContainer.applyToViews<TextView>(
                HINT_PREFIX,
                MnemonicUtils.MNEMONIC_WORDS_COUNT
            ) { index, textView ->
                textView.text = context.getString(R.string.str_mnemonic_counter_template, index + 1)
            }
            mnemonicsContainer
                .applyToViews<EditText>(EDIT_FIELD_PREFIX, 24) { position, editText ->
                    editText.showSoftInputOnFocus = false
                    editText.onFocusChangeListener =
                        OnFocusChangeListener { _: View?, hasFocus: Boolean ->
                            if (hasFocus) {
                                onFocusChangeListener?.invoke(position)
                            }
                        }
                }
        }
    }

    fun focusOnFirst() {
        binding?.mnemonicsEditText0?.requestFocus()
    }

    fun setFieldError(index: Int, isError: Boolean) {
        binding {
            mnemonicsContainer.applyToView<EditText>(EDIT_FIELD_PREFIX, index) { editText ->
                editText.setTextColor(
                    getColorCompat(
                        if (isError) {
                            R.color.colorAlert
                        } else {
                            R.color.textColorPrimary
                        }
                    )
                )
            }
        }
    }

    fun updateField(index: Int, text: String, requestFocus: Boolean) {
        binding {
            mnemonicsContainer.applyToView<EditText>(EDIT_FIELD_PREFIX, index) { editText ->
                editText.setText(text)
                if (requestFocus) {
                    editText.requestFocus()
                }
                editText.setSelection(editText.text.length)
                editText.setTextColor(getColorCompat(R.color.textColorPrimary))
            }
        }
    }

    fun updateFields(items: Array<String>, errors: List<Boolean>, focusedFieldIndex: Int) {
        binding {
            mnemonicsContainer
                .applyToViews<EditText>(EDIT_FIELD_PREFIX, items.size) { index, editText ->
                    val incorrectWord = errors.getOrNull(index).orFalse()

                    editText.setText(items[index])
                    editText.setTextColor(
                        getColorCompat(
                            if (incorrectWord) {
                                R.color.colorAlert
                            } else {
                                R.color.textColorPrimary
                            }
                        )
                    )
                    if (index == focusedFieldIndex) {
                        editText.requestFocus()
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

    private fun <T> View.applyToView(
        prefix: String,
        index: Int,
        block: (T) -> Unit
    ) {
        block(
            findViewById(
                resources.getIdentifier(
                    "$prefix$index",
                    "id",
                    context.packageName
                )
            )
        )
    }

    companion object {
        private const val EDIT_FIELD_PREFIX = "mnemonicsEditText"
        private const val HINT_PREFIX = "mnemonicTextView"
    }
}
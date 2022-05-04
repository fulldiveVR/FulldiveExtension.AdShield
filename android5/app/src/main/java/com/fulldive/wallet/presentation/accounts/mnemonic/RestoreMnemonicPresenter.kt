package com.fulldive.wallet.presentation.accounts.mnemonic

import com.fulldive.wallet.di.modules.DefaultPresentersModule
import com.fulldive.wallet.extensions.*
import com.fulldive.wallet.interactors.ClipboardInteractor
import com.fulldive.wallet.interactors.WalletInteractor
import com.fulldive.wallet.presentation.base.BaseMoxyPresenter
import com.fulldive.wallet.utils.MnemonicUtils
import com.joom.lightsaber.ProvidedBy
import org.adshield.R
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@ProvidedBy(DefaultPresentersModule::class)
class RestoreMnemonicPresenter @Inject constructor(
    private val walletInteractor: WalletInteractor,
    private val clipboardInteractor: ClipboardInteractor
) : BaseMoxyPresenter<RestoreMnemonicMoxyView>() {
    private val fields = Array(WORDS_COUNT) { "" }
    private var currentField = 0
    private var entropy = ""
    private var address = ""

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        walletInteractor
            .getMnemonicDictionary()
            .withUiDefaults()
            .compositeSubscribe(
                onSuccess = viewState::setDictionary
            )
    }

    fun onSuggestionClicked(word: String) {
        val text = word.trim()
        fields[currentField] = text
        updateField(currentField, text, false)
        nextField()
    }

    fun onKeyboardKeyClicked(key: Char) {
        var text = fields[currentField]
        if (text.length + 1 < MAX_WORD_LENGTH) {
            text += key
            fields[currentField] = text
            updateField(currentField, text, false)
        }
    }

    fun onNextKeyClicked() {
        nextField()
    }

    fun onDeleteKeyClicked() {
        var text = fields[currentField]
        if (text.isNotEmpty()) {
            text = text.substring(0, text.length - 1)
            fields[currentField] = text
            updateField(currentField, text, false)
        } else if (currentField > 0) {
            selectField(currentField - 1)
        }
    }

    fun onFieldClicked(position: Int) {
        selectField(position)
    }

    fun onPasteClicked() {
        clipboardInteractor
            .getClip()
            .map { text ->
                text
                    .trim()
                    .split(" ")
                    .map { it.filter(Char::isLetter) }
                    .filter(String::isNotEmpty)
                    .map { it.safeSubstring(endIndex = MAX_WORD_LENGTH) }
            }
            .withUiDefaults()
            .compositeSubscribe(
                onSuccess = { words ->
                    val wordsArray = words.toTypedArray()
                    if (wordsArray.isNotEmpty()) {
                        val errors = walletInteractor.checkMnemonicWords(wordsArray)
                        fields.fill("")
                        val maxIndex = min(wordsArray.size, WORDS_COUNT)
                        wordsArray.copyInto(fields, startIndex = 0, endIndex = maxIndex)
                        currentField = max(0, maxIndex - 1)
                        viewState.updateFields(fields, errors.subList(0, maxIndex), currentField)
                    } else {
                        viewState.showMessage(R.string.str_clipboard_no_data)
                    }
                }
            )
    }


    fun onCheckPasswordSuccessfully() {
        viewState.showWaitDialog()

        walletInteractor
            .createAccount(
                address,
                entropy,
                fromMnemonic = true
            )
            .withDefaults()
            .doAfterTerminate {
                viewState.hideWaitDialog()
            }
            .compositeSubscribe(
                onSuccess = {
                    viewState.finish()
                },
                onError = {
                    viewState.showMessage(R.string.str_invalid_mnemonic)
                }
            )
    }

    fun onNextClicked() {
        viewState.showWaitDialog()
        entropy = ""
        address = ""
        safeSingle {
            val wordsList = fields.toList()
            var entropy: String? = null
            if (walletInteractor.isValidMnemonicArray(fields)
                && MnemonicUtils.isValidStringHdSeedFromWords(wordsList)
            ) {
                entropy = walletInteractor.entropyHexFromMnemonicWords(wordsList)
            }
            entropy
        }
            .flatMap { entropy ->
                safeSingle {
                    this.address = MnemonicUtils.createAddress(entropy, 0)
                    this.entropy = entropy
                    this.address.orNull()
                }
            }
            .flatMap {
                walletInteractor.hasPassword()
            }
            .withDefaults()
            .doAfterTerminate { viewState.hideWaitDialog() }
            .compositeSubscribe(
                onSuccess = viewState::requestPassword,
                onError = {
                    viewState.showMessage(R.string.str_invalid_mnemonic)
                }
            )
    }

    private fun nextField() {
        if (currentField < WORDS_COUNT - 1) {
            selectField(currentField + 1)
        }
    }

    private fun selectField(index: Int) {
        if (currentField != index) {
            val text = fields[currentField].trim()
            viewState.setFieldError(
                currentField,
                text.isNotEmpty() && !walletInteractor.isValidMnemonicWord(text)
            )
        }
        currentField = index
        updateField(currentField, fields[currentField], true)
    }

    private fun updateField(index: Int, text: String, requestFocus: Boolean) {
        viewState.updateField(index, text, requestFocus)
    }

    companion object {
        private const val WORDS_COUNT = 24
        private const val MAX_WORD_LENGTH = 15
    }
}
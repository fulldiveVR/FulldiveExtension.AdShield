package com.fulldive.wallet.presentation.accounts.password

import com.fulldive.wallet.di.modules.DefaultPresentersModule
import com.fulldive.wallet.extensions.withDefaults
import com.fulldive.wallet.interactors.WalletInteractor
import com.fulldive.wallet.presentation.base.BaseMoxyPresenter
import com.fulldive.wallet.presentation.system.keyboard.KeyboardType
import com.joom.lightsaber.ProvidedBy
import moxy.MvpAppCompatActivity
import org.adshield.R
import javax.inject.Inject

@ProvidedBy(DefaultPresentersModule::class)
class PasswordPresenter @Inject constructor(
    private val walletInteractor: WalletInteractor
) : BaseMoxyPresenter<PasswordMoxyView>() {
    var justCheckPassword = false

    private var userInput: String = ""
    private var confirmInput: String = ""
    private var isConfirmSequence = false

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        if (justCheckPassword) {
            viewState.setTitle(R.string.str_check_pincode)
            viewState.setMessage(R.string.str_password_check)
        } else {
            viewState.setTitle(R.string.str_set_pincode)
            viewState.setMessage(R.string.str_password_init)
        }

        clear()
    }

    fun onUserInsertKey(input: Char) {
        var updateField = when {
            userInput.isEmpty() -> {
                userInput = input.toString()
                true
            }
            userInput.length < 5 -> {
                userInput += input
                true
            }
            else -> false
        }

        when {
            userInput.length == 4 -> {
                viewState.switchKeyboard(KeyboardType.Alphabet)
            }
            userInput.length == 5 && walletInteractor.isPasswordValid(userInput) -> {
                checkPassword()
            }
            userInput.length == 5 -> {
                clear()
                updateField = false
            }
        }
        if (updateField) {
            viewState.updatePasswordField(userInput.length)
        }
    }

    fun userDeleteKey() {
        when {
            userInput.isEmpty() -> viewState.finishWithResult(MvpAppCompatActivity.RESULT_CANCELED)
            userInput.length == 4 -> {
                userInput = userInput.substring(0, userInput.length - 1)
                viewState.switchKeyboard(KeyboardType.Numeric)
            }
            else -> {
                userInput = userInput.substring(0, userInput.length - 1)
            }
        }
        viewState.updatePasswordField(userInput.length)
    }

    fun onShakeEnded() {
        clear()
    }

    private fun clear() {
        userInput = ""
        viewState.clear()
        viewState.switchKeyboard(KeyboardType.Numeric)
    }

    private fun checkPassword() {
        when {
            justCheckPassword -> {
                viewState.showWaitDialog()
                walletInteractor
                    .checkPassword(userInput)
                    .withDefaults()
                    .compositeSubscribe(
                        onSuccess = { isCorrect ->
                            if (isCorrect) {
                                viewState.finishWithResult(MvpAppCompatActivity.RESULT_OK)
                            } else {
                                viewState.hideWaitDialog()
                                viewState.shakeView()
                                viewState.showMessage(R.string.str_invalid_pincode)
                            }
                        }
                    ) {
                        viewState.hideWaitDialog()
                        viewState.shakeView()
                        viewState.showMessage(R.string.str_unknown_error_msg)
                    }
            }
            isConfirmSequence -> {
                if (confirmInput == userInput) {
                    viewState.showWaitDialog()
                    walletInteractor
                        .setPassword(userInput)
                        .withDefaults()
                        .compositeSubscribe(
                            onSuccess = {
                                viewState.finishWithResult(MvpAppCompatActivity.RESULT_OK)
                            }
                        ) {
                            viewState.hideWaitDialog()
                            viewState.shakeView()
                            isConfirmSequence = false
                            viewState.setMessage(R.string.str_password_init)
                            viewState.showMessage(R.string.str_unknown_error_msg)
                        }
                } else {
                    viewState.shakeView()
                    isConfirmSequence = false
                    viewState.setMessage(R.string.str_password_init)
                    viewState.showMessage(R.string.error_msg_password_not_same)
                }
            }
            else -> {
                isConfirmSequence = true
                confirmInput = userInput
                userInput = ""
                clear()
                viewState.shuffleKeyboard()
                viewState.setMessage(R.string.str_password_confirm)
            }
        }
    }
}
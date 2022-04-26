package com.fulldive.wallet.presentation.accounts.privatekey

import com.fulldive.wallet.di.modules.DefaultPresentersModule
import com.fulldive.wallet.extensions.safe
import com.fulldive.wallet.extensions.withDefaults
import com.fulldive.wallet.extensions.withUiDefaults
import com.fulldive.wallet.interactors.ClipboardInteractor
import com.fulldive.wallet.interactors.WalletInteractor
import com.fulldive.wallet.presentation.base.BaseMoxyPresenter
import com.fulldive.wallet.utils.MnemonicUtils
import com.joom.lightsaber.ProvidedBy
import org.adshield.R
import javax.inject.Inject

@ProvidedBy(DefaultPresentersModule::class)
class RestorePrivateKeyPresenter @Inject constructor(
    private val walletInteractor: WalletInteractor,
    private val clipboardInteractor: ClipboardInteractor
) : BaseMoxyPresenter<RestorePrivateKeyMoxyView>() {
    private var address = ""
    private var privateKey = ""

    fun onPasteClicked() {
        clipboardInteractor
            .getClip()
            .withUiDefaults()
            .compositeSubscribe(
                onSuccess = { text ->
                    viewState.showPrivateKey(text)
                },
                onError = {
                    viewState.showMessage(R.string.str_clipboard_no_data)
                }
            )
    }

    fun onCheckPasswordSuccessfully() {
        viewState.showWaitDialog()
        walletInteractor
            .createAccount(address, privateKey)
            .withDefaults()
            .doAfterTerminate {
                viewState.hideWaitDialog()
            }
            .compositeSubscribe(
                onSuccess = {
                    viewState.finish()
                },
                onError = object : OnErrorConsumer() {
                    override fun onError(error: Throwable) {
                        super.onError(error)
                        viewState.showWrongKey()
                    }
                }
            )
    }

    fun onNextClicked(text: String) {
        val privateKey = text.trim()
        var address = ""
        safe {
            if (walletInteractor.isPrivateKeyValid(privateKey)) {
                address = MnemonicUtils
                    .createAddress(privateKey.removePrefix(WalletInteractor.PRIVATE_KEY_PREFIX))
            }
        }
        when {
            address.isNotEmpty() -> {
                this.address = address
                this.privateKey = privateKey
                walletInteractor
                    .hasPassword()
                    .withDefaults()
                    .compositeSubscribe(
                        onSuccess = viewState::requestPassword
                    )
            }
            else -> viewState.showWrongKey()
        }
    }


    fun onScanQRResultsReceived(text: String) {
        text
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?.let(viewState::showPrivateKey)
    }
}
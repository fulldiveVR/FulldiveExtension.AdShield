package com.fulldive.wallet.presentation.accounts.create

import com.fulldive.wallet.di.modules.DefaultPresentersModule
import com.fulldive.wallet.extensions.withDefaults
import com.fulldive.wallet.interactors.ClipboardInteractor
import com.fulldive.wallet.interactors.WalletInteractor
import com.fulldive.wallet.models.AccountSecrets
import com.fulldive.wallet.presentation.base.BaseMoxyPresenter
import com.fulldive.wallet.rx.AppSchedulers
import com.joom.lightsaber.ProvidedBy
import org.adshield.R
import javax.inject.Inject

@ProvidedBy(DefaultPresentersModule::class)
class CreateAccountPresenter @Inject constructor(
    private val walletInteractor: WalletInteractor,
    private val clipboardInteractor: ClipboardInteractor
) : BaseMoxyPresenter<CreateAccountMoxyView>() {
    private var accountSecrets: AccountSecrets? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        generateMnemonic()
    }

    fun onShowMnemonicClicked() {
        walletInteractor
            .hasPassword()
            .withDefaults()
            .compositeSubscribe(
                onSuccess = viewState::requestPassword
            )
    }

    fun onCreateAccountClicked() {
        accountSecrets?.let { secrets ->
            viewState.showWaitDialog()
            walletInteractor
                .createAccount(secrets)
                .withDefaults()
                .doOnError {
                    viewState.hideWaitDialog()
                }
                .compositeSubscribe(
                    onSuccess = {
                        viewState.showMainActivity()
                        viewState.finish()
                    }
                )
        }
    }

    fun onWalletAddressCopyClicked() {
        accountSecrets?.let { secrets ->
            clipboardInteractor
                .copyToClipboard(secrets.address)
                .subscribeOn(AppSchedulers.ui())
                .observeOn(AppSchedulers.ui())
                .compositeSubscribe(
                    onSuccess = {
                        viewState.showMessage(R.string.str_copied)
                    }
                )
        }
    }

    fun onMnemonicCopyClicked() {
        accountSecrets?.let { secrets ->
            clipboardInteractor
                .copyToClipboard(secrets.mnemonic.joinToString(" ", transform = String::trim))
                .subscribeOn(AppSchedulers.ui())
                .observeOn(AppSchedulers.ui())
                .compositeSubscribe(
                    onSuccess = {
                        viewState.showMessage(R.string.str_copied)
                    }
                )
        }
    }

    fun onCheckPasswordSuccessfully() {
        accountSecrets?.mnemonic?.let(viewState::showMnemonic)
    }

    private fun generateMnemonic() {
        walletInteractor
            .createSecrets()
            .withDefaults()
            .compositeSubscribe(
                onSuccess = ::onAccountSecretsReceived,
                onError = object : OnErrorConsumer() {
                    override fun onError(error: Throwable) {
                        super.onError(error)
                        viewState.finish()
                    }
                }
            )
    }

    private fun onAccountSecretsReceived(accountSecrets: AccountSecrets) {
        this.accountSecrets = accountSecrets
        viewState.showAccountAddress(accountSecrets.address)
    }
}
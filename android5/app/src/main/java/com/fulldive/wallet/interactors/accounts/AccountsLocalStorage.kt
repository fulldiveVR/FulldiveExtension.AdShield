package com.fulldive.wallet.interactors.accounts

import android.content.Context
import appextension.getPrivateSharedPreferences
import com.fulldive.wallet.di.modules.DefaultLocalStorageModule
import com.fulldive.wallet.models.Account
import com.joom.lightsaber.ProvidedBy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ProvidedBy(DefaultLocalStorageModule::class)
class AccountsLocalStorage @Inject constructor(
    context: Context
) {
    private var currentAccount: Account? = null

    private var sharedPreferences = context.getPrivateSharedPreferences(CRYPTO_KEY)

    companion object {
        private const val CRYPTO_KEY = "crypto_wallet"
    }
}
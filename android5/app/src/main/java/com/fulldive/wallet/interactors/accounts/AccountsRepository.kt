package com.fulldive.wallet.interactors.accounts

import com.fulldive.wallet.di.modules.DefaultRepositoryModule
import com.joom.lightsaber.ProvidedBy
import javax.inject.Inject

@ProvidedBy(DefaultRepositoryModule::class)
class AccountsRepository @Inject constructor(
    private val accountsLocalStorage: AccountsLocalStorage
) {
}
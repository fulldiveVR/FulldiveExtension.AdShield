package com.fulldive.wallet.interactors.accounts

import com.fulldive.wallet.di.modules.DefaultInteractorsModule
import com.fulldive.wallet.models.Account
import com.joom.lightsaber.ProvidedBy
import io.reactivex.Single
import javax.inject.Inject

@ProvidedBy(DefaultInteractorsModule::class)
class AccountsInteractor @Inject constructor(
    private val accountsRepository: AccountsRepository,
) {

    fun getAccount(): Single<Account> {
        return Single.error(Exception("Okay, it's just RX test"))
    }
}

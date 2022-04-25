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

package com.fulldive.wallet.presentation.main

import android.text.SpannableString
import androidx.fragment.app.DialogFragment
import com.fulldive.wallet.models.Account
import com.fulldive.wallet.presentation.base.BaseMoxyView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

interface MainMoxyView : BaseMoxyView {

    @AddToEndSingle
    fun showCreateWalletButton()

    @AddToEndSingle
    fun showAccount(account: Account)

    @AddToEndSingle
    fun showBalance(spannableString: SpannableString, denom: String)

    @OneExecution
    fun showDialog(
        dialogFragment: DialogFragment,
        tag: String,
        cancelable: Boolean
    )

    @OneExecution
    fun showCheckPassword()

    @OneExecution
    fun showMnemonic()

    @OneExecution
    fun showPrivateKey()
}
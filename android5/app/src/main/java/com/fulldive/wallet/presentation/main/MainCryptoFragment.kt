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

import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.view.isVisible
import com.fulldive.wallet.models.Account
import com.fulldive.wallet.presentation.base.BaseMvpFragment
import com.joom.lightsaber.getInstance
import moxy.ktx.moxyPresenter
import org.adshield.databinding.FragmentCryptoMainBinding

class MainCryptoFragment : BaseMvpFragment<FragmentCryptoMainBinding>(), MainMoxyView {

    private val presenter by moxyPresenter {
        getInjector().getInstance<MainPresenter>()
    }

    override fun getViewBinding(): FragmentCryptoMainBinding {
        return FragmentCryptoMainBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding {
            createWalletButton.setOnClickListener {
                presenter.onCreateWalletClicked()
            }
            deleteWalletButton.setOnClickListener {
                presenter.onDeleteWalletClicked()
            }
        }
    }

    override fun showCreateWalletButton() {
        binding {
            progressView.isVisible = false
            createWalletButton.isVisible = true
            deleteWalletButton.isVisible = false
            addressTextView.isVisible = false
            balanceTextView.isVisible = false
        }
    }

    override fun showAccount(account: Account) {
        binding {
            progressView.isVisible = false
            deleteWalletButton.isVisible = true
            createWalletButton.isVisible = false
            addressTextView.text = "Address: ${account.address}"
            addressTextView.isVisible = true
            showMnemonicButton.isVisible = account.fromMnemonic
            showPrivateKeyButton.isVisible = account.hasPrivateKey
        }
    }

    override fun showBalance(spannableString: SpannableString, denom: String) {
        binding {
            balanceTextView.isVisible = true
            balanceTextView.text = SpannableStringBuilder("Balance: ")
                .append(spannableString)
                .append(" ")
                .append(denom)
            createWalletButton.isVisible = false
        }
    }
}
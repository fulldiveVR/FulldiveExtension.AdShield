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

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isVisible
import com.fulldive.wallet.models.Account
import com.fulldive.wallet.presentation.accounts.mnemonic.ShowMnemonicActivity
import com.fulldive.wallet.presentation.accounts.password.PasswordActivity
import com.fulldive.wallet.presentation.accounts.privatekey.ShowPrivateKeyActivity
import com.fulldive.wallet.presentation.base.BaseMvpFragment
import com.joom.lightsaber.getInstance
import moxy.MvpAppCompatActivity
import moxy.ktx.moxyPresenter
import org.adshield.R
import org.adshield.databinding.FragmentCryptoMainBinding

class MainCryptoFragment : BaseMvpFragment<FragmentCryptoMainBinding>(), MainMoxyView {

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == MvpAppCompatActivity.RESULT_OK) {
            presenter.onCheckPasswordSuccessfully()
        }
    }

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
            showMnemonicButton.setOnClickListener {
                presenter.onShowMnemonicClicked()
            }
            showPrivateKeyButton.setOnClickListener {
                presenter.onShowPrivateKeyClicked()
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
            showMnemonicButton.isVisible = false
            showPrivateKeyButton.isVisible = false
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

    override fun showCheckPassword() {
        launcher.launch(
            Intent(requireActivity(), PasswordActivity::class.java).putExtra(
                PasswordActivity.KEY_JUST_CHECK,
                true
            ),
            ActivityOptionsCompat.makeCustomAnimation(
                requireContext(),
                R.anim.slide_in_bottom,
                R.anim.fade_out
            )
        )
    }

    override fun showMnemonic() {
        startActivity(
            Intent(requireActivity(), ShowMnemonicActivity::class.java)
        )
    }

    override fun showPrivateKey() {
        startActivity(
            Intent(requireActivity(), ShowPrivateKeyActivity::class.java)
        )
    }
}
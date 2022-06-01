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

package com.fulldive.wallet.presentation.accounts.create

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isVisible
import com.fulldive.wallet.presentation.accounts.password.PasswordActivity
import com.fulldive.wallet.presentation.base.BaseMvpActivity
import com.joom.lightsaber.getInstance
import moxy.ktx.moxyPresenter
import org.adshield.R
import org.adshield.databinding.ActivityCreateBinding

class CreateAccountActivity : BaseMvpActivity<ActivityCreateBinding>(), CreateAccountMoxyView {

    private val launcher = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            presenter.onCheckPasswordSuccessfully()
        }
    }

    private val presenter by moxyPresenter {
        appInjector.getInstance<CreateAccountPresenter>()
    }

    override fun getViewBinding() = ActivityCreateBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        binding {
            setSupportActionBar(toolbar)

            copyWalletButton.setOnClickListener {
                presenter.onWalletAddressCopyClicked()
            }
            copyMnemonicButton.setOnClickListener {
                presenter.onMnemonicCopyClicked()
            }

            copyMnemonicButton.isVisible = false
        }
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun showAccountAddress(address: String) {
        binding {
            addressTextView.text = address
            addressTextView.visibility = View.VISIBLE
            warningTextView.visibility = View.VISIBLE

            nextButton.setText(R.string.str_show_mnemonic)
            nextButton.setOnClickListener {
                presenter.onShowMnemonicClicked()
            }
            nextButton.visibility = View.VISIBLE
        }
    }

    override fun showMnemonic(mnemonicWords: List<String>) {
        binding {
            warningTextView.visibility = View.VISIBLE
            nextButton.setText(R.string.str_create_wallet)
            nextButton.setOnClickListener {
                presenter.onCreateAccountClicked()
            }
            copyMnemonicButton.isVisible = true
            mnemonicsLayout.setMnemonicWords(mnemonicWords)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun requestPassword(checkPassword: Boolean) {
        launcher.launch(
            Intent(this, PasswordActivity::class.java).putExtra(
                PasswordActivity.KEY_JUST_CHECK,
                checkPassword
            ),
            ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slide_in_bottom, R.anim.fade_out)
        )
    }
}
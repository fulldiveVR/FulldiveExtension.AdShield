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

package com.fulldive.wallet.presentation.accounts.privatekey

import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.core.view.isVisible
import com.fulldive.wallet.presentation.base.BaseMvpActivity
import com.joom.lightsaber.getInstance
import moxy.ktx.moxyPresenter
import org.adshield.databinding.ActivityShowPrivateKeyBinding

class ShowPrivateKeyActivity : BaseMvpActivity<ActivityShowPrivateKeyBinding>(),
    ShowPrivateKeyMoxyView {

    private val presenter by moxyPresenter {
        appInjector.getInstance<ShowPrivateKeyPresenter>()
    }

    override fun getViewBinding() = ActivityShowPrivateKeyBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        binding {
            setSupportActionBar(toolbar)
            copyMnemonicButton.setOnClickListener {
                presenter.onMnemonicCopyClicked()
            }
        }
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun showPrivateKey(text: String) {
        binding {
            copyMnemonicButton.isVisible = true
            privateKeyTextView.text = text
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
}
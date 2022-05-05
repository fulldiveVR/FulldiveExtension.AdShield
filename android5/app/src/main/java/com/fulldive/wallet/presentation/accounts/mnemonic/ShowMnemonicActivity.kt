package com.fulldive.wallet.presentation.accounts.mnemonic

import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.core.view.isVisible
import com.fulldive.wallet.presentation.base.BaseMvpActivity
import com.joom.lightsaber.getInstance
import moxy.ktx.moxyPresenter
import org.adshield.databinding.ActivityCreateBinding
import org.adshield.databinding.ActivityShowMnemonicBinding

class ShowMnemonicActivity : BaseMvpActivity<ActivityShowMnemonicBinding>(), ShowMnemonicMoxyView {

    private val presenter by moxyPresenter {
        appInjector.getInstance<ShowMnemonicPresenter>()
    }

    override fun getViewBinding() = ActivityShowMnemonicBinding.inflate(layoutInflater)

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

    override fun showMnemonic(mnemonicWords: List<String>) {
        binding {
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
}
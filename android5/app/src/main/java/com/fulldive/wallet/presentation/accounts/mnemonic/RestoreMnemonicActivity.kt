package com.fulldive.wallet.presentation.accounts.mnemonic

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.fulldive.wallet.presentation.accounts.password.PasswordActivity
import com.fulldive.wallet.presentation.base.BaseMvpActivity
import com.joom.lightsaber.getInstance
import moxy.ktx.moxyPresenter
import org.adshield.R
import org.adshield.databinding.ActivityRestoreMnemonicBinding

class RestoreMnemonicActivity : BaseMvpActivity<ActivityRestoreMnemonicBinding>(),
    RestoreMnemonicMoxyView {

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            presenter.onCheckPasswordSuccessfully()
        }
    }

    private val presenter by moxyPresenter {
        appInjector.getInstance<RestoreMnemonicPresenter>()
    }

    private var mnemonicAdapter: WordsAdapter? = null

    override fun getViewBinding() = ActivityRestoreMnemonicBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_SECURE,
//            WindowManager.LayoutParams.FLAG_SECURE
//        )
        binding {
            setSupportActionBar(toolbar)

            recyclerView.layoutManager = LinearLayoutManager(
                this@RestoreMnemonicActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            recyclerView.setHasFixedSize(true)
            mnemonicAdapter = WordsAdapter(
                presenter::onSuggestionClicked
            )
            recyclerView.adapter = mnemonicAdapter

            pasteButton.setOnClickListener {
                presenter.onPasteClicked()
            }

            nextButton.setOnClickListener {
                presenter.onNextClicked()
            }

            keyboardLayout.setUppercase(false)
            keyboardLayout.setShuffle(false)
            keyboardLayout.setNextButtonVisible(true)
            keyboardLayout.onKeyListener = { key ->
                presenter.onKeyboardKeyClicked(key)
            }
            keyboardLayout.onDeleteKeyListener = {
                presenter.onDeleteKeyClicked()
            }
            keyboardLayout.onNextKeyListener = {
                presenter.onNextKeyClicked()
            }
            mnemonicsLayout.onFocusChangeListener = { index ->
                presenter.onFieldClicked(index)
            }
        }
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
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

    override fun setDictionary(words: List<String>) {
        mnemonicAdapter?.items = words
    }

    override fun setFieldError(index: Int, isError: Boolean) {
        binding {
            mnemonicsLayout.setFieldError(index, isError)
        }
    }

    override fun updateField(index: Int, text: String, requestFocus: Boolean) {
        binding {
            mnemonicsLayout.updateField(index, text, requestFocus)
            mnemonicAdapter?.filter?.filter(text)
        }
    }

    override fun updateFields(items: Array<String>, errors: List<Boolean>, focusedFieldIndex: Int) {
        binding {
            mnemonicsLayout.updateFields(items, errors, focusedFieldIndex)
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
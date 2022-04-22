package com.fulldive.wallet.presentation.accounts.create

//import com.fulldive.wallet.presentation.security.password.CheckPasswordActivity
//import com.fulldive.wallet.presentation.security.password.SetPasswordActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
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

    override fun requestCheckPassword() {
//        launcher.launch(
//            Intent(this, CheckPasswordActivity::class.java),
//            ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slide_in_bottom, R.anim.fade_out)
//        )
    }

    override fun requestCreatePassword() {
//        launcher.launch(
//            Intent(this, SetPasswordActivity::class.java),
//            ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slide_in_bottom, R.anim.fade_out)
//        )
    }

    override fun showMainActivity() {
//        Intent(this, MainActivity::class.java)
//            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
//            .let(::startActivity)
    }
}
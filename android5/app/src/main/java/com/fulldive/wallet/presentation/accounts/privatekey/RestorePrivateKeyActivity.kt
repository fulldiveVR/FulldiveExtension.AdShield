package com.fulldive.wallet.presentation.accounts.privatekey

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.fulldive.wallet.presentation.accounts.password.PasswordActivity
import com.fulldive.wallet.presentation.base.BaseMvpActivity
import com.joom.lightsaber.getInstance
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import moxy.ktx.moxyPresenter
import org.adshield.R
import org.adshield.databinding.ActivityRestorePrivateKeyBinding

class RestorePrivateKeyActivity : BaseMvpActivity<ActivityRestorePrivateKeyBinding>(),
    RestorePrivateKeyMoxyView {

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            presenter.onCheckPasswordSuccessfully()
        }
    }

    private val launcherQrScan = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            ScanIntentResult
                .parseActivityResult(result.resultCode, result.data)
                ?.contents
                ?.let(presenter::onScanQRResultsReceived)
        }
    }

    private val presenter by moxyPresenter {
        appInjector.getInstance<RestorePrivateKeyPresenter>()
    }

    override fun getViewBinding() = ActivityRestorePrivateKeyBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        binding {
            setSupportActionBar(toolbar)
            pasteButton.setOnClickListener {
                presenter.onPasteClicked()
            }
            qrcodeButton.setOnClickListener {
                onQrScanClicked()
            }
            nextButton.setOnClickListener {
                presenter.onNextClicked(privateKeyEditText.text.toString())
            }
            privateKeyEditText.setBackgroundResource(R.drawable.background_frame)
            errorTextView.isVisible = false
            privateKeyEditText.doAfterTextChanged {
                privateKeyEditText.setBackgroundResource(R.drawable.background_frame)
                errorTextView.isVisible = false
            }
        }
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun showPrivateKey(text: String) {
        binding {
            privateKeyEditText.setText(text)
        }
    }

    override fun showWrongKey() {
        binding {
            privateKeyEditText.setBackgroundResource(R.drawable.background_frame_alert)
            errorTextView.isVisible = true
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

    private fun onQrScanClicked() {
        launcherQrScan.launch(
            ScanContract()
                .createIntent(
                    this,
                    ScanOptions()
                        .setOrientationLocked(true)
                )
        )
    }
}

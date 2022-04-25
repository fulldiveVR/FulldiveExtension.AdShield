package com.fulldive.wallet.presentation.accounts.password

import android.os.Bundle
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.fulldive.wallet.extensions.unsafeLazy
import com.fulldive.wallet.presentation.base.BaseMvpActivity
import com.fulldive.wallet.presentation.keyboard.KeyboardFragment
import com.fulldive.wallet.presentation.keyboard.KeyboardListener
import com.fulldive.wallet.presentation.keyboard.KeyboardPagerAdapter
import com.fulldive.wallet.presentation.system.keyboard.KeyboardType
import com.joom.lightsaber.getInstance
import moxy.ktx.moxyPresenter
import org.adshield.R
import org.adshield.databinding.ActivityPasswordSetBinding

class PasswordActivity : BaseMvpActivity<ActivityPasswordSetBinding>(),
    PasswordMoxyView,
    KeyboardListener {

    private val justCheckPassword by unsafeLazy { intent.getBooleanExtra(KEY_JUST_CHECK, false) }

    private var adapter: KeyboardPagerAdapter? = null

    private val presenter by moxyPresenter {
        appInjector.getInstance<PasswordPresenter>().also {
            it.justCheckPassword = justCheckPassword
        }
    }

    override fun getViewBinding() = ActivityPasswordSetBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding {
            setSupportActionBar(toolbar)

            keyboardPager.offscreenPageLimit = 2
            keyboardPager.adapter = KeyboardPagerAdapter(
                supportFragmentManager,
                this@PasswordActivity
            ).also {
                adapter = it
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

    override fun onStop() {
        finishWithResult(RESULT_CANCELED)
        super.onStop()
    }

    override fun setTitle(textId: Int) {
        binding {
            titleTextView.setText(textId)
        }
    }

    override fun setMessage(textId: Int) {
        binding {
            messageTextView.setText(textId)
        }
    }

    override fun shakeView() {
        binding {
            pincodeLayout.clearAnimation()
            val animation = AnimationUtils.loadAnimation(
                this@PasswordActivity, R.anim.shake
            )
            animation.reset()
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    presenter.onShakeEnded()
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            pincodeLayout.startAnimation(animation)
        }
    }

    override fun userInsertKey(input: Char) {
        presenter.onUserInsertKey(input)
    }

    override fun userDeleteKey() {
        presenter.userDeleteKey()
    }

    override fun switchKeyboard(type: KeyboardType) {
        binding {
            keyboardPager.setCurrentItem(type.id, true)
        }
    }

    override fun updatePasswordField(inputLength: Int) {
        binding {
            listOf(circleImage0, circleImage1, circleImage2, circleImage3, circleImage4)
                .forEachIndexed { index, imageView ->
                    imageView.setImageResource(
                        if (index < inputLength) R.drawable.ic_pass_pu else R.drawable.ic_pass_gr
                    )
                }
        }
    }

    override fun shuffleKeyboard() {
        adapter?.fragments?.forEach(KeyboardFragment::shuffleKeyboard)
    }

    override fun clear() {
        binding {
            listOf(circleImage0, circleImage1, circleImage2, circleImage3, circleImage4)
                .forEach { imageView ->
                    imageView.setImageResource(R.drawable.ic_pass_gr)
                }
        }
    }

    override fun finishWithResult(resultCode: Int) {
        setResult(resultCode)
        finish()
        if (resultCode == RESULT_OK) {
            overridePendingTransition(R.anim.fade_in, R.anim.slide_out_bottom)
        }
    }

    companion object {
        const val KEY_JUST_CHECK = "KEY_JUST_CHECK"
    }
}

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

package ui.rewards

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
import com.fulldive.wallet.presentation.accounts.create.CreateAccountActivity
import com.fulldive.wallet.presentation.accounts.mnemonic.ShowMnemonicActivity
import com.fulldive.wallet.presentation.accounts.password.PasswordActivity
import com.fulldive.wallet.presentation.accounts.privatekey.ShowPrivateKeyActivity
import com.fulldive.wallet.presentation.base.BaseMvpFragment
import com.joom.lightsaber.getInstance
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import org.adshield.R
import org.adshield.databinding.FragmentRewardsBinding

class RewardsFragment : BaseMvpFragment<FragmentRewardsBinding>(), RewardsView {

    @InjectPresenter
    lateinit var presenter: RewardsPresenter

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == MvpAppCompatActivity.RESULT_OK) {
            presenter.onCheckPasswordSuccessfully()
        }
    }

    override fun getViewBinding() = FragmentRewardsBinding.inflate(layoutInflater)

    @ProvidePresenter
    fun providePresenter(): RewardsPresenter = appInjector.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding {
            with(cryptoMainLayout) {
                viewPrivatKeyItem.setOnClickListener { presenter.onShowPrivateKeyClicked() }
                viewMnemonicItem.setOnClickListener { presenter.onShowMnemonicClicked() }
                copyAddressButton.setOnClickListener {
                    presenter.onWalletAddressCopyClicked(addressTextView.text.toString())
                }
            }

            with(addAccountLayout) {
                restoreWithPrivateKey.setOnClickListener {
//        showActivity(packageContext, PrivateKeyRestoreActivity::class.java)
                }
                restoreWithMnemonic.setOnClickListener {
//        showActivity(packageContext, MnemonicRestoreActivity::class.java)
                }
                createButton.setOnClickListener {
                    showActivity(CreateAccountActivity::class.java)
                }
            }
        }
    }

    override fun showCreateWalletLayout() {
        binding {
            cryptoMainLayout.containerLayout.isVisible = false
            addAccountLayout.containerLayout.isVisible = true
        }
    }

    override fun showAccount(account: Account) {
        binding {
            addAccountLayout.containerLayout.isVisible = false
            cryptoMainLayout.containerLayout.isVisible = true
            cryptoMainLayout.viewMnemonicItem.isVisible = account.fromMnemonic
            cryptoMainLayout.viewPrivatKeyItem.isVisible = account.hasPrivateKey
            cryptoMainLayout.addressTextView.text = account.address
        }
    }

    override fun showBalance(spannableString: SpannableString, denom: String) {
        binding {
            cryptoMainLayout.balanceTextView.isVisible = true
            cryptoMainLayout.balanceTextView.text = SpannableStringBuilder(spannableString)
                .append(" ")
                .append(denom)
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

    private fun showActivity(clazz: Class<*>) {
        val activity = requireActivity()
        val intent = Intent(activity, clazz)
        activity.startActivity(intent)
    }
}
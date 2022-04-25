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
import androidx.core.view.isVisible
import com.fulldive.wallet.models.Account
import com.fulldive.wallet.presentation.accounts.create.CreateAccountActivity
import com.fulldive.wallet.presentation.base.BaseMvpFragment
import com.joom.lightsaber.getInstance
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import org.adshield.databinding.FragmentRewardsBinding

class RewardsFragment : BaseMvpFragment<FragmentRewardsBinding>(), RewardsView {

    @InjectPresenter
    lateinit var presenter: RewardsPresenter

    override fun getViewBinding() = FragmentRewardsBinding.inflate(layoutInflater)

    @ProvidePresenter
    fun providePresenter(): RewardsPresenter = appInjector.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding {
            with(cryptoMainLayout) {
                viewPrivatKeyItem.setOnClickListener { presenter.onViewPrivateKeyClicked() }
                viewMnemonicItem.setOnClickListener { presenter.onViewMnemonicClicked() }
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

    private fun showActivity(clazz: Class<*>) {
        val activity = requireActivity()
        val intent = Intent(activity, clazz)
        activity.startActivity(intent)
    }
}
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

package com.fulldive.wallet.presentation.exchange

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.fulldive.wallet.presentation.base.BaseMvpFragment
import com.joom.lightsaber.getInstance
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import org.adshield.R
import org.adshield.databinding.FragmentExchangeBinding
import ui.MainActivity

class ExchangeFragment : BaseMvpFragment<FragmentExchangeBinding>(), ExchangeView {

    @InjectPresenter
    lateinit var presenter: ExchangePresenter

    override fun getViewBinding() = FragmentExchangeBinding.inflate(layoutInflater)

    @ProvidePresenter
    fun providePresenter(): ExchangePresenter = appInjector.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.toolbar?.title = getString(R.string.str_exchange_title)
        binding {
            exchangeButton.setOnClickListener { presenter.exchangeExperience() }
        }
    }

    override fun showUserExperience(
        experience: Int,
        minExchangeExperience: Int,
        availableFdTokens: Int
    ) {
        binding {
            descriptionTextView.text = String.format(
                getString(R.string.str_exchange_description), minExchangeExperience
            )
            userExperienceTextView.text = experience.toString()
            coinsTextView.text = availableFdTokens.toString()
        }
    }

    override fun showAvailableFdTokens(availableFdTokens: Int) {
        binding?.coinsTextView?.text = availableFdTokens.toString()
    }

    override fun experienceIsValid(isValid: Boolean) {
        binding {
            exchangeButton.isEnabled = isValid
        }
    }

    override fun showSuccessExchange(experience: Int) {
        Toast.makeText(
            requireActivity(),
            String.format(
                requireActivity().getString(R.string.str_experience_exchenge_success),
                experience
            ),
            Toast.LENGTH_LONG
        ).show()
        findNavController()
            .apply {
                navigate(ExchangeFragmentDirections.actionNavigationExchangeToRewardsFragment())
            }
    }
}
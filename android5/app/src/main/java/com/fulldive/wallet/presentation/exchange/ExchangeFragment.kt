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
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.fulldive.wallet.extensions.getDrawableCompat
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

    private val experienceTextWatcher = object : TextWatcher {
        override fun afterTextChanged(editable: Editable) =
            presenter.validateExperience(editable.toString().trim())

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    }

    @ProvidePresenter
    fun providePresenter(): ExchangePresenter = appInjector.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.toolbar?.title = getString(R.string.str_exchange_title)
        binding {
            enterExperienceTextEditText.addTextChangedListener(experienceTextWatcher)
            exchangeButton.setOnClickListener {
                presenter.exchangeExperience(
                    "", "" //todo server required
                    // enterExperienceTextEditText.text.toString()
                )
            }
        }
    }

    override fun onDestroyView() {
        binding {
            enterExperienceTextEditText.removeTextChangedListener(experienceTextWatcher)
        }
        super.onDestroyView()
    }

    override fun showUserExperience(
        experience: Int,
        minimumExchangeExperience: Int,
        coins: Double
    ) {
        binding {
            descriptionTextView.text = String.format(
                getString(R.string.str_exchange_description), minimumExchangeExperience
            )
            enterExperienceTextEditText.setText(experience.toString())
            coinsTextView.text = coins.toString()
        }
    }

    override fun showAvailableFulldiveCoins(coins: Double) {
        binding?.coinsTextView?.text = coins.toString()
    }

    override fun experienceIsValid(isValid: Boolean) {
        binding {
            exchangeButton.isEnabled = isValid
            enterExperienceTextLayout.background = context?.getDrawableCompat(
                if (isValid || enterExperienceTextEditText.editableText.isEmpty()) R.drawable.background_rounded_border_normal else R.drawable.background_rounded_border_error
            )
        }
    }
}
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

package com.fulldive.wallet.presentation.main

import com.fulldive.wallet.extensions.toast
import com.fulldive.wallet.presentation.base.BaseMvpDialogFragment
import com.joom.lightsaber.getInstance
import moxy.ktx.moxyPresenter
import org.adshield.databinding.FragmentCryptoMainBinding

class MainDialogFragment : BaseMvpDialogFragment<FragmentCryptoMainBinding>(), MainMoxyView {

    private val presenter by moxyPresenter {
        getInjector().getInstance<MainPresenter>()
    }

    override fun getViewBinding(): FragmentCryptoMainBinding {
        return FragmentCryptoMainBinding.inflate(layoutInflater)
    }

    override fun doSomething() {
        requireContext().toast("Hey! Ho! Let's Go")
    }
}
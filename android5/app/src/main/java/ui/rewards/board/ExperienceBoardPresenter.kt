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

package ui.rewards.board

import android.content.Context
import com.fulldive.wallet.di.modules.DefaultModule
import com.fulldive.wallet.interactors.ExperienceExchangeInterator
import com.fulldive.wallet.interactors.WalletInteractor
import com.fulldive.wallet.rx.ISchedulersProvider
import com.joom.lightsaber.ProvidedBy
import moxy.InjectViewState
import ui.rewards.board.base.BaseExperiencePresenter
import javax.inject.Inject

@InjectViewState
@ProvidedBy(DefaultModule::class)
class ExperienceBoardPresenter @Inject constructor(
    context: Context,
    experienceExchangeInterator: ExperienceExchangeInterator,
    walletInteractor: WalletInteractor,
    schedulers: ISchedulersProvider
) : BaseExperiencePresenter<ExperienceBoardView>(
    context,
    experienceExchangeInterator,
    walletInteractor,
    schedulers
)
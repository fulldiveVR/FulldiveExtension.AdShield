package ui.rewards.board

import com.fulldive.wallet.di.modules.DefaultModule
import com.fulldive.wallet.rx.ISchedulersProvider
import com.joom.lightsaber.ProvidedBy
import moxy.InjectViewState
import ui.rewards.board.base.BaseExperiencePresenter
import javax.inject.Inject

@InjectViewState
@ProvidedBy(DefaultModule::class)
class ExperienceBoardPresenter @Inject constructor(
    schedulers: ISchedulersProvider
) : BaseExperiencePresenter<ExperienceBoardView>(schedulers)
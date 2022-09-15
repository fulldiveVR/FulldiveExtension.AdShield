package com.fulldive.wallet.presentation.base.subscription

import analytics.TrackerConstants
import android.app.Activity
import appextension.StatisticHelper
import com.fulldive.wallet.di.modules.DefaultModule
import com.fulldive.wallet.presentation.base.BaseMoxyPresenter
import com.joom.lightsaber.ProvidedBy
import kotlinx.coroutines.launch
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
@ProvidedBy(DefaultModule::class)
open class SubscriptionTutorialPresenter @Inject constructor(

) : BaseMoxyPresenter<SubscriptionTutorialView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        StatisticHelper.logAction(TrackerConstants.EVENT_PRO_TUTORIAL_OPENED)
        launch {
            SubscriptionService.isConnectedState.collect { isConnected ->
                if (isConnected) {
                    viewState.showSubscriptionInfo(SubscriptionService.getProSubscriptionInfo())
                }
            }
        }
    }

    fun onAddProSubscriptionClicked(activity: Activity) {
        SubscriptionService.subscribe(activity)
        StatisticHelper.logAction(TrackerConstants.EVENT_BUY_PRO_CLICKED)
        viewState.onDismiss()
    }
}
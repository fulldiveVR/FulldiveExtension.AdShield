package com.fulldive.wallet.presentation.base.subscription

import com.fulldive.wallet.presentation.base.BaseMoxyView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface SubscriptionTutorialView : BaseMoxyView {
    fun showSubscriptionInfo(
        proSubscriptionInfo: ProSubscriptionInfo
    )

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onDismiss()
}
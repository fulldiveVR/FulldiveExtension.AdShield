package com.fulldive.wallet.presentation.base.subscription

data class ProSubscriptionInfo(
    val price: String,
    val salePrice: String,
    val currency: String
)

val EMPTY_SUBSCRIPTION_INFO = ProSubscriptionInfo(
    "",
    "",
    ""
)
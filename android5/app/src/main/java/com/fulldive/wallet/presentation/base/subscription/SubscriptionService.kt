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

package com.fulldive.wallet.presentation.base.subscription

import analytics.TrackerConstants
import android.app.Activity
import android.content.Context
import appextension.StatisticHelper
import com.fulldive.iap.DataWrappers
import com.fulldive.iap.IapConnector
import com.fulldive.iap.PurchaseServiceListener
import com.fulldive.iap.SubscriptionServiceListener
import com.fulldive.wallet.extensions.orEmptyString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import service.AppSettingsService

object SubscriptionService {

    const val proSku = "adshield_pro_subscription"
    const val proSkuDiscount = "adshield_pro_subscription_discount"

    const val STATE_PURCHASED = 1
    const val STATE_PENDING = 2
    const val STATE_UNDEFINED = 0
    private val repeatPopupCounts = listOf(2, 5)

    val isConnectedState = MutableStateFlow(false)
    val isProStatusPurchasedState = MutableStateFlow<Boolean>(false)
    val isPopupShowState = MutableStateFlow<Boolean>(false)
    private val subscriptionPrices = mutableMapOf<String, DataWrappers.ProductDetails>()
    private var isFirstLaunched = false

    private var iapConnector: IapConnector? = null

    suspend fun init(context: Context) {
        iapConnector = IapConnector(
            context = context, // activity / context
            nonConsumableKeys = emptyList(), // pass the list of non-consumables
            consumableKeys = emptyList(), // pass the list of consumables
            subscriptionKeys = listOf(proSku, proSkuDiscount), // pass the list of subscriptions
            enableLogging = true // to enable / disable logging
        )
        isConnectedState.emit(true)
        isProStatusPurchasedState.value = false

        iapConnector?.addPurchaseListener(object : PurchaseServiceListener {
            override fun onPricesUpdated(iapKeyPrices: Map<String, DataWrappers.ProductDetails>) {
                subscriptionPrices.putAll(iapKeyPrices)
            }

            override fun onProductPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
                when (purchaseInfo.sku) {
                    proSku, proSkuDiscount -> {
                        CoroutineScope(coroutineContext).launch {
                            StatisticHelper.logAction(TrackerConstants.EVENT_BUY_PRO_SUCCESS)
                            isProStatusPurchasedState.value =
                                purchaseInfo.purchaseState == STATE_PURCHASED
                        }
                    }
                }
            }

            override fun onProductRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
                // will be triggered fetching owned products using IapConnector;
            }
        })

        iapConnector?.addSubscriptionListener(object : SubscriptionServiceListener {
            override fun onSubscriptionRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
                // will be triggered upon fetching owned subscription upon initialization
                when (purchaseInfo.sku) {
                    proSku, proSkuDiscount -> {
                        CoroutineScope(coroutineContext).launch {
                            isProStatusPurchasedState.value =
                                (purchaseInfo.purchaseState == STATE_PURCHASED)
                        }
                    }
                }
            }

            override fun onSubscriptionPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
                // will be triggered whenever subscription succeeded
                when (purchaseInfo.sku) {
                    proSku, proSkuDiscount -> {
                        CoroutineScope(coroutineContext).launch {
                            StatisticHelper.logAction(TrackerConstants.EVENT_BUY_PRO_SUCCESS)
                            isProStatusPurchasedState.value =
                                (purchaseInfo.purchaseState == STATE_PURCHASED)
                        }
                    }
                }
            }

            override fun onPricesUpdated(iapKeyPrices: Map<String, DataWrappers.ProductDetails>) {
                subscriptionPrices.putAll(iapKeyPrices)
            }
        })
        handlePromoPopupState()
    }

    fun onDestroy() {
        iapConnector?.destroy()
    }

    fun subscribe(activity: Activity) {
        when {
            subscriptionPrices[proSkuDiscount] != null -> {
                iapConnector?.subscribe(activity, proSkuDiscount)
            }
            subscriptionPrices[proSku] != null -> {
                iapConnector?.subscribe(activity, proSku)
            }
        }
    }

    fun getProSubscriptionInfo(): ProSubscriptionInfo {
        val (fullPrice, currency) = getSkuPrice(proSku)
        val (discountPrice, _) = getSkuPrice(proSkuDiscount)
        return ProSubscriptionInfo(
            price = fullPrice,
            salePrice = discountPrice,
            currency = currency
        )
    }

    fun setClosePopup(isClose: Boolean) {
        isPopupShowState.value = !isClose
        if (isClose && !AppSettingsService.getIsPromoPopupClosed()) {
            AppSettingsService.setIsPromoPopupClosed(true)
        }
    }

    fun setIsFirstLaunched(isLaunched: Boolean) {
        if (isFirstLaunched != isLaunched) {
            isFirstLaunched = isLaunched
            handlePromoPopupState()
        }
    }

    private fun getSkuPrice(sku: String): Pair<String, String> {
        return subscriptionPrices[sku]?.let {
            Pair(it.priceAmount.toString(), it.priceCurrencyCode.orEmptyString())
        } ?: Pair("", "")
    }

    private fun handlePromoPopupState() {
        val isClosed = AppSettingsService.getIsPromoPopupClosed()
        isPopupShowState.value = when {
            !isFirstLaunched -> {
                false
            }
            isClosed -> {
                val closeCount = AppSettingsService.getPromoCloseStartCounter()
                val startCount = AppSettingsService.getCurrentStartCounter()
                val diff = startCount - closeCount
                repeatPopupCounts.any { it == diff }
            }
            else -> true
        }

    }
}
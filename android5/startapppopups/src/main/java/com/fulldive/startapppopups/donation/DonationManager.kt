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

package com.fulldive.startapppopups.donation

import android.app.Activity
import android.content.Context
import com.fulldive.iap.BillingClientConnectionListener
import com.fulldive.iap.DataWrappers
import com.fulldive.iap.IapConnector
import com.fulldive.iap.PurchaseServiceListener


object DonationManager {

    private const val PURCHASE_ID = "support_fulldive_team"

    private var iapConnector: IapConnector? = null

    var isConnected = false

    fun init(context: Context, onConnected: () -> Unit, onPurchased: () -> Unit) {
        iapConnector = IapConnector(
            context = context, // activity / context
            nonConsumableKeys = emptyList(), // pass the list of non-consumables
            consumableKeys = listOf(PURCHASE_ID), // pass the list of consumables
            subscriptionKeys = emptyList(), // pass the list of subscriptions
            enableLogging = true // to enable / disable logging
        )
        iapConnector?.addBillingClientConnectionListener(object : BillingClientConnectionListener {
            override fun onConnected(status: Boolean, billingResponseCode: Int) {
                if (status) {
                    onConnected.invoke()
                }
                isConnected = status
            }
        })
        iapConnector?.addPurchaseListener(object : PurchaseServiceListener {
            override fun onPricesUpdated(iapKeyPrices: Map<String, DataWrappers.ProductDetails>) {
                val a = 1
            }

            override fun onProductPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
                when (purchaseInfo.sku) {
                    PURCHASE_ID -> {
                        onPurchased.invoke()
                    }
                }
            }

            override fun onProductRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
                val a = 1
            }
        })
    }

    fun destroy() {
        iapConnector?.destroy()
        iapConnector = null
    }

    fun purchase(activity: Activity) {
        iapConnector?.purchase(activity, PURCHASE_ID)
    }

    fun purchaseFromSettings(
        activity: Activity,
        onConnected: () -> Unit = {},
        onPurchased: () -> Unit = {}
    ) {
        if (!isConnected) {
            init(
                activity,
                onConnected = {
                    onConnected.invoke()
                    purchase(activity)
                },
                onPurchased
            )
        } else {
            purchase(activity)
        }
    }
}
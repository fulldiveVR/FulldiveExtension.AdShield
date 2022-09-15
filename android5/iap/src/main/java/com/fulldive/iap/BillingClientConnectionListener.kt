package com.fulldive.iap

interface BillingClientConnectionListener {
    fun onConnected(status: Boolean, billingResponseCode: Int)
}
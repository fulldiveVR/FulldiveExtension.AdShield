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

package com.fulldive.startapppopups

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.ContextCompat
import com.fulldive.startapppopups.donation.DonationAction
import com.fulldive.startapppopups.donation.DonationManager
import com.fulldive.startapppopups.donation.DonationSnackbar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class PopupManager {

    private val client = OkHttpClient()
    private val popupsFlow = listOf(
        StartAppDialog.InstallBrowser,
        StartAppDialog.RateUs,
        StartAppDialog.InstallBrowser,
        StartAppDialog.RateUs,
        StartAppDialog.InstallBrowser,
        StartAppDialog.RateUs,
        StartAppDialog.InstallBrowser,
        StartAppDialog.RateUs,
        StartAppDialog.Empty,
        StartAppDialog.Empty
    )

    fun onAppStarted(
        activity: Activity,
        appId: String,
        isShowInstallBrowserPopup: Boolean = true,
        isShowRateUsPopup: Boolean = true,
        isShowDonationPopup: Boolean = true,
        donationPopupBottomMarginInPixels: Int = 0,
        donationActionListener: (DonationAction) -> Unit = {}
    ) {
        val sharedPreferences = activity.getPrivateSharedPreferences()
        val startCounter = sharedPreferences.getProperty(KEY_START_APP_COUNTER, 0)
        val isPromoPopupClosed = sharedPreferences.getProperty(KEY_IS_PROMO_POPUP_CLOSED, false)
        val isPromoPopupCloseCounter = sharedPreferences
            .getProperty(KEY_IS_PROMO_POPUP_CLOSED_START_COUNTER, 0)

        val diff = startCounter - isPromoPopupCloseCounter

        sharedPreferences.setProperty(KEY_START_APP_COUNTER, startCounter + 1)

        val rateUsDone = sharedPreferences.getProperty(KEY_RATE_US_DONE, false)
        val installBrowserDone = sharedPreferences.getProperty(KEY_INSTALL_BROWSER_DONE, false)

        if (!rateUsDone || !installBrowserDone) {
            when (getShowingPopup(startCounter)) {
                StartAppDialog.RateUs -> {
                    if (isShowRateUsPopup && !rateUsDone) {
                        showRateUsDialog(activity) {
                            onRateUsPositiveClicked(
                                activity,
                                sharedPreferences,
                                it,
                                appId
                            )
                        }
                    }
                }
                StartAppDialog.InstallBrowser -> {
                    if (isShowInstallBrowserPopup && (!installBrowserDone) && !isBrowserInstalled(
                            activity
                        )
                    ) {
                        showInstallBrowserDialog(activity) {
                            onInstallAppPositiveClicked(activity, sharedPreferences)
                        }
                    }
                }
                else -> {
                }
            }
        }

        if (isShowDonationPopup && (!isPromoPopupClosed || repeatPopupCounts.any { it == diff })) {
            val snackbar = DonationSnackbar()
            snackbar.showSnackBar(
                activity.findViewById(android.R.id.content),
                onDonateClicked = {
                    if (DonationManager.isConnected) {
                        DonationManager.purchase(activity)
                    } else {
                        DonationManager.init(
                            activity,
                            onConnected = {
                                DonationManager.purchase(activity)
                            },
                            onPurchased = {
                                donationActionListener.invoke(DonationAction.DonationSuccess)
                                showDonationSuccess(activity)
                            }
                        )
                    }
                    donationActionListener.invoke(DonationAction.OpenedFromPopup)
                    snackbar.dismiss()
                    onCloseDonationClicked(sharedPreferences)
                },
                onCloseClicked = {
                    donationActionListener.invoke(DonationAction.PopupClosed)
                    onCloseDonationClicked(sharedPreferences)
                },
                bottomMargin = if (donationPopupBottomMarginInPixels == 0) {
                    activity.resources.getDimensionPixelSize(R.dimen.size_48dp)
                } else {
                    donationPopupBottomMarginInPixels
                }
            )
            donationActionListener.invoke(DonationAction.PopupShown)
        }
    }

    private fun onCloseDonationClicked(sharedPreferences: SharedPreferences) {
        sharedPreferences.setProperty(KEY_IS_PROMO_POPUP_CLOSED, true)
        sharedPreferences.setProperty(
            KEY_IS_PROMO_POPUP_CLOSED_START_COUNTER,
            sharedPreferences.getProperty(KEY_START_APP_COUNTER, 0)
        )
    }

    private fun isBrowserInstalled(context: Context): Boolean {
        val app = try {
            context.packageManager.getApplicationInfo(BROWSER_PACKAGE_NAME, 0)
        } catch (e: Exception) {
            null
        }
        return app?.enabled ?: false
    }

    private fun onRateUsPositiveClicked(
        context: Context,
        sharedPreferences: SharedPreferences,
        rating: Int,
        appId: String
    ) {
        if (rating < SUCCESS_RATING_VALUE) {
            showRateReportDialog(context) { message ->
                sendMessage(message)
                sharedPreferences.setProperty(KEY_RATE_US_DONE, true)
            }
        } else {
            context.openAppInGooglePlay(appId)
            sharedPreferences.setProperty(KEY_RATE_US_DONE, true)
        }
    }

    private fun onInstallAppPositiveClicked(
        context: Context,
        sharedPreferences: SharedPreferences
    ) {
        context.openAppInGooglePlay(BROWSER_PACKAGE_NAME)
        sharedPreferences.setProperty(KEY_INSTALL_BROWSER_DONE, true)
    }

    private fun sendMessage(message: String) {
        Thread {
            try {
                val res = post(INBOX_URL, getJSON(message))
                Log.d("sendMessageTest", res)
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                Log.d("sendMessageTest", "$ex.message")
            }
        }
            .start()
    }

    private fun getJSON(message: String): String {
        return "{\"payload\":{\"message\":\"$message\"},\"type\":\"report-message\"}"
    }

    private fun getShowingPopup(startCounter: Int): StartAppDialog {
        return if (popupsFlow.lastIndex >= startCounter) {
            popupsFlow[startCounter]
        } else {
            popupsFlow[startCounter % popupsFlow.size]
        }
    }

    private fun showRateUsDialog(
        context: Context,
        positiveClickListener: (value: Int) -> Unit
    ) {
        RateUsDialogBuilder.show(context) { value ->
            positiveClickListener.invoke(value)
        }
    }

    private fun showRateReportDialog(
        context: Context,
        positiveClickListener: (message: String) -> Unit
    ) {
        RateReportDialogBuilder.show(context) { message ->
            positiveClickListener.invoke(message)
        }
    }

    private fun showInstallBrowserDialog(
        context: Context,
        positiveClickListener: () -> Unit
    ) {
        InstallBrowserDialogBuilder.show(context) {
            positiveClickListener.invoke()
        }
    }

    @Throws(IOException::class)
    private fun post(url: String, json: String): String {
        val body: RequestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        var result = ""
        client.newCall(request).execute().use { response ->
            result = response.body.toString()
        }
        return result
    }

    fun showDonationSuccess(context: Context) {
        val dialog = AlertDialog
            .Builder(context)
            .setTitle(R.string.donation_title)
            .setMessage(R.string.donation_message)
            .setPositiveButton(R.string.donation_done) { _, _ -> }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(ContextCompat.getColor(context, R.color.textColorAccent))
        }
        dialog.show()
    }

    companion object {
        private val repeatPopupCounts = listOf(2, 5)
        private const val INBOX_URL = "https://api.fdvr.co/v2/inbox"
        private const val KEY_START_APP_COUNTER = "KEY_START_APP_COUNTER"
        private const val KEY_RATE_US_DONE = "KEY_RATE_US_DONE"
        private const val KEY_INSTALL_BROWSER_DONE = "KEY_INSTALL_BROWSER_DONE"
        private const val KEY_IS_PROMO_POPUP_CLOSED = "KEY_IS_PROMO_POPUP_CLOSED"
        private const val KEY_IS_PROMO_POPUP_CLOSED_START_COUNTER =
            "KEY_IS_PROMO_POPUP_CLOSED_START_COUNTER"
        private const val BROWSER_PACKAGE_NAME = "com.fulldive.mobile"
        private const val SUCCESS_RATING_VALUE = 4
    }
}

sealed class StartAppDialog(val id: String) {
    object RateUs : StartAppDialog("RateUs")
    object InstallBrowser : StartAppDialog("InstallBrowser")
    object Empty : StartAppDialog("Empty")
}
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

package appextension.dialogs

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.navigation.findNavController
import appextension.openAppInGooglePlay
import com.fulldive.startapppopups.FIN_WIZE_APP
import com.fulldive.startapppopups.FinWizeSnackbar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.adshield.BuildConfig
import org.adshield.R
import service.AppSettingsService
import service.EnvironmentService
import ui.MainActivity
import ui.advanced.AdvancedFragmentDirections
import utils.Links
import utils.Logger
import java.io.IOException

object PopupManager {

    private const val INBOX_URL = "https://api.fdvr.co/v2/inbox"
    private const val BROWSER_PACKAGE_NAME = "com.fulldive.mobile"
    private const val SUCCESS_RATING_VALUE = 4

    private val client = OkHttpClient()
    private val popupsFlow = listOf(
        StartAppDialog.FinWize,
        StartAppDialog.InstallBrowser,
        StartAppDialog.RateUs,
        StartAppDialog.FinWize,
        StartAppDialog.InstallBrowser,
        StartAppDialog.RateUs,
        StartAppDialog.FinWize,
        StartAppDialog.InstallBrowser,
        StartAppDialog.RateUs,
        StartAppDialog.FinWize,
        StartAppDialog.InstallBrowser,
        StartAppDialog.RateUs,
        StartAppDialog.Empty,
        StartAppDialog.Empty
    )

    private val log = Logger("PopupManager")

    fun onAppStarted(context: MainActivity) {
        val startCounter = AppSettingsService.updateAndGetCurrentStartUpCount()

        val rateUsDone = AppSettingsService.isRateUsDone()
        val isFinWizeClicked = AppSettingsService.isFinWizeClicked()
        val installBrowserDone = AppSettingsService.isInstallBrowserDone()
        val adBlockDone = AppSettingsService.isAdBlockDone()

        if (!rateUsDone || !installBrowserDone) {
            when (getShowingPopup(startCounter)) {
                StartAppDialog.RateUs -> {
                    if (!rateUsDone) {
                        showRateUsDialog(context) {
                            onRateUsPositiveClicked(
                                context,
                                it
                            )
                        }
                    }
                }

                StartAppDialog.FinWize -> {
                    if (!isFinWizeClicked) {
                        val snackbar = FinWizeSnackbar()
                        snackbar.showSnackBar(
                            context.findViewById(android.R.id.content),
                            onOpenFinWizeClicked = {
                                AppSettingsService.setFinWizeClicked()
                                context.openAppInGooglePlay(FIN_WIZE_APP)
                                snackbar.dismiss()
                            },
                            onCloseClicked = {
                                snackbar.dismiss()
                            },
                            bottomMargin = context.resources.getDimensionPixelSize(com.fulldive.startapppopups.R.dimen.size_48dp)
                        )
                    }
                }

                StartAppDialog.InstallBrowser -> {
                    if ((!installBrowserDone) && !isBrowserInstalled(context)) {
                        showInstallBrowserDialog(context) {
                            onInstallAppPositiveClicked(context)
                        }
                    }
                }

                StartAppDialog.BlockAds -> {
                    if (!adBlockDone && !EnvironmentService.isSlim()) {
                        showAdBlockDialog(context,
                            positiveClickListener = {
                                context.findNavController(R.id.nav_host_fragment).navigate(
                                    AdvancedFragmentDirections.actionActivityToWebFragment(
                                        Links.dnsSettings,
                                        context.getString(R.string.str_dns_settings)
                                    )
                                )
                                AppSettingsService.setAdBlockDone()
                            },
                            neutralClickListener = {
                                Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse(Links.kb)
                                    context.startActivity(this)
                                }
                                AppSettingsService.setAdBlockDone()
                            }
                        )
                    }
                }

                else -> {
                }
            }
        }
    }

    fun showContactSupportDialog(
        context: Context,
        positiveClickListener: () -> Unit
    ) {
        ContactSupportDialogBuilder
            .show(context) {
                positiveClickListener.invoke()
            }
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
        rating: Int
    ) {
        if (rating < SUCCESS_RATING_VALUE) {
            showRateReportDialog(context) { message ->
                sendMessage(message)
                AppSettingsService.setRateUsDone()
            }
        } else {
            context.openAppInGooglePlay(BuildConfig.APPLICATION_ID)
            AppSettingsService.setRateUsDone()
        }
    }

    fun showAppSettingsPermissionDialog(
        context: Context,
        onPermissionGranted: (Boolean) -> Unit
    ) {
        when {
            !AppSettingsService.isAppSettingsPermissionGranted() -> {
                AppSettingsPermissionDialog
                    .show(context) { isGranted ->
                        if (isGranted) {
                            AppSettingsService.setAppSettingsPermissionGranted()
                        }
                        onPermissionGranted.invoke(isGranted)
                    }
            }

            else -> onPermissionGranted.invoke(true)
        }
    }

    fun showUpdateDialog(context: Context) {
        if (AppSettingsService.compareVersions() < 0) {
            UpdateAppDialog.show(context)
        }
    }

    private fun onInstallAppPositiveClicked(
        context: Context
    ) {
        context.openAppInGooglePlay(BROWSER_PACKAGE_NAME)
        AppSettingsService.setInstallBrowserDone()
    }

    private fun sendMessage(message: String) {
        Thread {
            try {
                val res = post(INBOX_URL, getJSON(message))
                log.v("sendMessageTest $res")
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                log.e("sendMessageTest ${ex.message}")
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
        RateUsDialogBuilder
            .show(context) { value ->
                positiveClickListener.invoke(value)
            }
    }

    private fun showRateReportDialog(
        context: Context,
        positiveClickListener: (message: String) -> Unit
    ) {
        RateReportDialogBuilder
            .show(context) { message ->
                positiveClickListener.invoke(message)
            }
    }

    private fun showInstallBrowserDialog(
        context: Context,
        positiveClickListener: () -> Unit
    ) {
        InstallBrowserDialogBuilder
            .show(context) {
                positiveClickListener.invoke()
            }
    }

    private fun showAdBlockDialog(
        context: Context,
        positiveClickListener: () -> Unit,
        neutralClickListener: () -> Unit
    ) {
        AdBlockDialogBuilder.show(context, positiveClickListener, neutralClickListener)
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
}

sealed class StartAppDialog(val id: String) {
    data object RateUs : StartAppDialog("RateUs")
    data object FinWize : StartAppDialog("FinWize")
    data object InstallBrowser : StartAppDialog("InstallBrowser")
    data object BlockAds : StartAppDialog("BlockAds")
    data object Empty : StartAppDialog("Empty")
}
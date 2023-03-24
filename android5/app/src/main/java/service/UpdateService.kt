/*
 * This file is part of Blokada.
 *
 * Blokada is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Blokada is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Blokada.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright © 2020 Blocka AB. All rights reserved.
 *
 * @author Karol Gusak (karol@blocka.net)
 */

package service

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.*
import org.blokada.R
import ui.Command
import ui.executeCommand
import ui.utils.cause
import ui.utils.openInBrowser
import utils.Logger
import utils.UpdateNotification

object UpdateService {

    private val log = Logger("Update")
    private val alert = AlertDialogService
    private val env = EnvironmentService
    private val notification = NotificationService
    private val context = ContextService
    private val persistence = PersistenceService
    private val scope = GlobalScope

    private var updateInfo: BlockaRepoUpdate? = null

    fun checkForUpdate(config: BlockaRepoConfig): Boolean {
        if (config.update == null) return false
        val updateInfo = config.update
        val versionCode = versionToVersionCode(updateInfo.newest)
        log.v("Repo newest version code is: $versionCode")
        return if (versionCode > env.getVersionCode()) {
            if (hasUserSeenThisUpdate(updateInfo)) {
                log.w("Ignoring this update, user has already seen it")
                false
            } else {
                log.w("New version is available")
                this.updateInfo = updateInfo
                true
            }
        } else false
    }

    fun handleUpdateFlow(
        onOpenDonate: () -> Unit,
        onOpenMore: () -> Unit
    ) {
        val appVersion = EnvironmentService.getVersionCode()
        if (!hasUserSeenAfterUpdateDialog(appVersion)) {
            markUserSeenAfterUpdateDialog(appVersion)
            showThankYouAlert(onOpenDonate, onOpenMore)
        } else {
            // This is in else branch to make sure only one dialog can show at once
            showUpdateAlertIfNecessary()
        }
    }

    fun showUpdateNotificationIfNecessary() {
        updateInfo?.let {
            notification.show(UpdateNotification(it.newest))
        }
    }

    fun showUpdateAlertIfNecessary(libreMode: Boolean = false) {
        updateInfo?.let {
            val ctx = context.requireContext()
            alert.showAlert(
                message = ctx.getString(R.string.alert_update_body, it.newest),
                title = ctx.getString(R.string.notification_update_header),
                positiveAction = ctx.getString(R.string.universal_action_download) to {
                    showUpdatingAlert(it.infoUrl)
                    scope.launch {
                        if (libreMode) deactivateBeforeDownload()
                        UpdateDownloaderService.installUpdate(it.mirrors) {
                            alert.dismiss()
                        }
                    }
                },
                onDismiss = {
                    markUpdateAsSeen(it)
                    notification.cancel(UpdateNotification(it.newest))
                    updateInfo = null
                }
            )
        }
    }

    private suspend fun deactivateBeforeDownload() {
        log.v("Deactivating before downloading the update")
        executeCommand(Command.OFF)
        delay(2000)
    }

    private fun showUpdatingAlert(url: Uri) {
        val ctx = context.requireContext()
        alert.showAlert(
            message = ctx.getString(R.string.update_downloading_description),
            title = ctx.getString(R.string.universal_status_processing),
            positiveAction = ctx.getString(R.string.universal_action_cancel) to {
                UpdateDownloaderService.cancelUpdate()
            },
            additionalAction = ctx.getString(R.string.universal_action_open_in_browser) to {
                UpdateDownloaderService.cancelUpdate()
                openInBrowser(url)
            },
            onDismiss = {
                UpdateDownloaderService.cancelUpdate()
            }
        )
    }

    private fun showThankYouAlert(
        onOpenDonate: () -> Unit,
        onOpenMore: () -> Unit
    ) {
        val ctx = context.requireContext()
        alert.showAlert(
            message = ctx.getString(R.string.update_desc_updated),
            title = ctx.getString(R.string.update_label_updated),
            positiveAction =
                if (EnvironmentService.isSlim()) ctx.getString(R.string.universal_action_close) to {}
                else ctx.getString(R.string.universal_action_donate) to onOpenDonate,
            additionalAction = ctx.getString(R.string.universal_action_learn_more) to onOpenMore
        )
    }

    private fun hasUserSeenThisUpdate(update: BlockaRepoUpdate): Boolean {
        val seen = persistence.load(BlockaRepoUpdate::class)
        return seen.newest == update.newest
    }

    private fun markUpdateAsSeen(update: BlockaRepoUpdate) {
        log.v("Marking update ${update.newest} as seen")
        persistence.save(update)
    }

    fun resetSeenUpdate() {
        log.v("Resetting seen update mark")
        persistence.save(Defaults.noSeenUpdate())
    }

    private fun hasUserSeenAfterUpdateDialog(appVersion: Int): Boolean {
        val seen = persistence.load(BlockaAfterUpdate::class)
        if (seen.dialogShownForVersion == null) {
            // Null is used to not show the dialog right on first install
            markUserSeenAfterUpdateDialog(appVersion)
            return true
        }
        return seen.dialogShownForVersion  == appVersion
    }

    private fun markUserSeenAfterUpdateDialog(appVersion: Int) {
        log.v("Marking user seen after update dialog for version: $appVersion")
        persistence.save(BlockaAfterUpdate(dialogShownForVersion = appVersion))
    }

    private fun versionToVersionCode(version: String): Int {
        val parts = version
            .replaceAfter("-", "")
            .replaceAfter("_", "")
            .split(".")

        return try {
            when(parts.size) {
                3 -> {
                    val major = parts[0].toInt()
                    val minor = parts[1].toInt()
                    val patch = parts[2].toInt()
                    "%d%02d%06d".format(major, minor, patch).toInt()
                }
                2 -> {
                    val major = parts[0].toInt()
                    val minor = parts[1].toInt()
                    "%d%02d000000".format(major, minor).toInt()
                }
                1 -> parts[0].toInt()
                else -> throw BlokadaException("Unknown version format")
            }
        } catch (ex: Exception) {
            log.w("Could not parse version: $version".cause(ex))
            0
        }
    }

}
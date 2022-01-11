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

package appextension

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.net.VpnService
import android.os.Bundle
import androidx.core.os.bundleOf
import appextension.LaunchHelper.isChangingState
import ui.MainActivity
import java.util.*

class ExtensionContentProvider : ContentProvider() {

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        return when (method.toLowerCase(Locale.ENGLISH)) {
            AppExtensionWorkType.START.id -> {

                if (!isChangingState()) {
                    context?.let { context ->
                        LaunchHelper.start(context)
                    }
                }
                null
            }
            AppExtensionWorkType.STOP.id -> {
                if (!isChangingState()) {
                    context?.let { context ->
//                        LaunchHelper.stopVPN(context)
                        LaunchHelper.stop(context)
                    }
                }
                null
            }
            AppExtensionWorkType.OPEN.id -> {
                context?.let { context ->
                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra(PREPARE_VPN, true)
                    intent.action = AppExtensionWorkType.OPEN.id
                    context.startActivity(intent)
                }
                null
            }
            AppExtensionWorkType.GetPermissionsRequired.id -> {
                bundleOf(
                    KEY_RESULT to context
                        ?.let { context ->
                            VpnService.prepare(context) != null
                        }
                        ?.or(false)
                )
            }
            AppExtensionWorkType.GetStatus.id -> {
                bundleOf(KEY_WORK_STATUS to LaunchHelper.getCurrentState())
            }
            else -> {
                super.call(method, arg, extras)
            }
        }
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        context?.contentResolver?.notifyChange(uri, null)
        return uri
    }

    override fun getType(uri: Uri): String? = null

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    companion object {
        private const val PREFERENCE_AUTHORITY =
            "com.fulldive.extension.adshield.dnschanger.FulldiveContentProvider"
        const val BASE_URL = "content://$PREFERENCE_AUTHORITY"
        const val KEY_WORK_STATUS = "work_status"
        const val KEY_RESULT = "result"
        const val PREPARE_VPN = "prepare_vpn"
    }
}

fun getContentUri(value: String): Uri {
    return Uri
        .parse(ExtensionContentProvider.BASE_URL)
        .buildUpon().appendPath(ExtensionContentProvider.KEY_WORK_STATUS)
        .appendPath(value)
        .build()
}
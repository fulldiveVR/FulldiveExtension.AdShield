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

package service

import appextension.AppExtensionState
import appextension.LaunchHelper
import org.adshield.R
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

object CheckAdblockWorkService {

    fun isAdblockWork(): Boolean {
        var isAdblockWork = true
        var urlConnection: HttpURLConnection? = null

        if (LaunchHelper.getCurrentState() == AppExtensionState.PROGRESS.id &&
            RemoteConfigService.getAdblockWorkCheckDomain().isNotEmpty()
        ) {
            try {
                isAdblockWork = false
                val url = URL(RemoteConfigService.getAdblockWorkCheckUrl())
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.doOutput = true
                //to catch in UnknownHostException
                urlConnection.responseCode
                urlConnection.responseMessage
            } catch (exception: Exception) {
                if (exception is UnknownHostException) {
                    isAdblockWork = true
                }
            } finally {
                (urlConnection as HttpURLConnection).disconnect()
            }
        }
        MonitorService.setInfo(if (!isAdblockWork) R.string.str_stop_working_push_info else 0)
        return isAdblockWork
    }
}
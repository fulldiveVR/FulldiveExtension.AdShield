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

package remoteconfig

import android.annotation.SuppressLint
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import org.json.JSONException
import org.json.JSONObject
import service.ContextService
import service.FileService
import utils.Logger
import utils.cause
import java.lang.reflect.Type
import java.util.*

object FulldiveRemoteConfigFetcher {

    @SuppressLint("StaticFieldLeak")
    val context = ContextService.requireContext()

    private const val FULLDIVE_CONFIGS_PATH =
        "https://raw.githubusercontent.com/fulldiveVR/FulldiveExtension.AdShield/5/android5/configs/configs.json"

    private const val ASSETS_CONFIGS_PATH = "configs/configs.json"

    private const val KEY_APP_VERSION = "adshield_current_version"

    private const val LOCALE_ALL = "all"

    private val log = Logger("FulldiveRemoteConfigFetcher")

    fun getCurrentAppVersion(): Int {
        return (requestFulldiveConfigs()[KEY_APP_VERSION] as? Double)?.toInt() ?: 0
    }

    private fun requestFulldiveConfigs(): Map<String, Any> {
        var configs = emptyMap<String, Any>()
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(FULLDIVE_CONFIGS_PATH)
                .header("User-Agent", "OkHttp Headers.java")
                .addHeader("Accept", "application/json; q=0.5")
                .addHeader("Accept", "application/vnd.github.v3+json")
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body().string()
                configs = parseConfigs(responseBody)
            }
        } catch (exception: Exception) {
            log.e("Error while request fulldive configss, ignoring".cause(exception))
        }
        return configs
    }

    private fun readDefaultConfigs(): Map<String, Any> {
        var configs = emptyMap<String, Any>()
        try {
            val file = FileService
            val manager = context.assets
            val stream = manager.open(ASSETS_CONFIGS_PATH)
            val configsData = file.load(stream)
            configs = parseConfigs(configsData)
        } catch (ex: Exception) {
        }
        return configs
    }

    private fun parseConfigs(configsData: String): Map<String, Any> {
        val locale = Locale.getDefault().country.lowercase()

        val responseJson = JSONObject(configsData)
        val configsJson = try {
            responseJson.get(locale).toString()
        } catch (e: JSONException) {
            responseJson.get(LOCALE_ALL).toString()
        }

        val gson = Gson()
        val type: Type = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson<Map<String, Any>>(configsJson, type)
    }
}
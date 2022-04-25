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

import android.content.Context
import android.content.SharedPreferences

fun Context.getPrivateSharedPreferences(key: String = "preference"): SharedPreferences {
    return this.getSharedPreferences(packageName + "_$key", Context.MODE_PRIVATE)
}

fun SharedPreferences.setProperty(tag: String, value: String) {
    try {
        val spe = edit()
        spe.putString(tag, value).apply()
    } catch (ex: Exception) {
    }
}

fun SharedPreferences.setProperty(tag: String, value: Int) {
    try {
        val spe = edit()
        spe.putInt(tag, value).apply()
    } catch (ex: Exception) {
    }
}

fun SharedPreferences.setProperty(tag: String, value: Boolean) {
    try {
        val spe = edit()
        spe.putBoolean(tag, value).apply()
    } catch (ex: Exception) {
    }
}

fun SharedPreferences.getProperty(tag: String, default_value: String): String {
    var result = default_value
    try {
        result = getString(tag, default_value) ?: default_value
    } catch (ex: Exception) {
    }
    return result
}

fun SharedPreferences.getProperty(tag: String, default_value: Int): Int {
    var result = default_value
    try {
        result = getInt(tag, default_value)
    } catch (ex: Exception) {
    }
    return result
}

fun SharedPreferences.getProperty(tag: String, default_value: Boolean): Boolean {
    var result = default_value
    try {
        result = getBoolean(tag, default_value)
    } catch (ex: Exception) {
    }
    return result
}
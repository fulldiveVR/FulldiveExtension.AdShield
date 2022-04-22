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
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

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

fun SharedPreferences.setProperty(tag: String, value: Long) {
    try {
        val spe = edit()
        spe.putLong(tag, value).apply()
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

fun SharedPreferences.getProperty(tag: String, default_value: Long): Long {
    var result = default_value
    try {
        result = getLong(tag, default_value)
    } catch (ex: Exception) {
    }
    return result
}

fun SharedPreferences.observeSettingsString(
    preferenceKey: String,
    defaultValue: String = "",
    hotStart: Boolean = true
): Observable<String> = BehaviorSubject.create { emitter ->
    val sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == preferenceKey) {
                emitter.onNext(sharedPreferences.getProperty(preferenceKey, ""))
            }
        }

    emitter.setCancellable {
        unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }
    if (hotStart) {
        emitter.onNext(getProperty(preferenceKey, defaultValue))
    }
    registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
}


fun SharedPreferences.observeSettingsInt(
    preferenceKey: String,
    defaultValue: Int = -1,
    hotStart: Boolean = true
): Observable<Int> = BehaviorSubject.create { emitter ->
    val sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == preferenceKey) {
                emitter.onNext(sharedPreferences.getInt(preferenceKey, defaultValue))
            }
        }
    emitter.setCancellable {
        unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }
    if (hotStart) {
        emitter.onNext(getProperty(preferenceKey, defaultValue))
    }
    registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
}

fun SharedPreferences.observeSettingsLong(
    preferenceKey: String,
    defaultValue: Long = -1L,
    hotStart: Boolean = true
): Observable<Long> = BehaviorSubject.create { emitter ->
    val sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == preferenceKey) {
                emitter.onNext(sharedPreferences.getLong(preferenceKey, defaultValue))
            }
        }
    emitter.setCancellable {
        unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }
    if (hotStart) {
        emitter.onNext(getProperty(preferenceKey, defaultValue))
    }
    registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
}
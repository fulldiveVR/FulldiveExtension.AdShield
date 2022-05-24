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

package ui.web

import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.*
import android.util.AttributeSet
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate.*

class ThemedWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    init {
        fixTheme()
    }

    private fun fixTheme() {
        val configuration = context.resources.configuration
        val configurationNighMode = configuration.uiMode and UI_MODE_NIGHT_MASK
        val appCompatNightMode = getDefaultNightMode()

        val newUiModeConfiguration = when {
            configurationNighMode == UI_MODE_NIGHT_NO && appCompatNightMode == MODE_NIGHT_YES -> {
                UI_MODE_NIGHT_YES or (configuration.uiMode and UI_MODE_NIGHT_MASK.inv())
            }
            configurationNighMode == UI_MODE_NIGHT_YES && appCompatNightMode == MODE_NIGHT_NO -> {
                UI_MODE_NIGHT_NO or (configuration.uiMode and UI_MODE_NIGHT_MASK.inv())
            }
            else -> null
        }

        if (newUiModeConfiguration != null) {
            val fixedConfiguration = Configuration().apply {
                uiMode = newUiModeConfiguration
            }
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(
                fixedConfiguration,
                context.resources.displayMetrics
            )
        }
    }
}
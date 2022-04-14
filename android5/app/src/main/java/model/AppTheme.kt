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

package model

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import org.adshield.R

sealed class AppTheme(
    val type: String,
    val mode: Int,
    val titleRes: Int
) {

    object LightAppTheme : AppTheme(
        LIGHT_THEME,
        AppCompatDelegate.MODE_NIGHT_NO,
        R.string.theme_light
    )

    object DarkAppTheme : AppTheme(
        DARK_THEME,
        AppCompatDelegate.MODE_NIGHT_YES,
        R.string.theme_dark
    )

    object AutoAppTheme : AppTheme(
        AUTO_THEME,
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        R.string.theme_auto
    )

    companion object {
        const val LIGHT_THEME = "LIGHT_THEME"
        const val DARK_THEME = "DARK_THEME"
        const val AUTO_THEME = "AUTO_THEME"

        fun getThemeByType(type: String): AppTheme {
            return when (type) {
                LightAppTheme.type -> LightAppTheme
                DarkAppTheme.type -> DarkAppTheme
                else -> AutoAppTheme
            }
        }

        fun getThemeByTitle(context: Context, title: String): AppTheme {
            return when (title) {
                context.getString(LightAppTheme.titleRes) -> LightAppTheme
                context.getString(DarkAppTheme.titleRes) -> DarkAppTheme
                else -> AutoAppTheme
            }
        }
    }
}
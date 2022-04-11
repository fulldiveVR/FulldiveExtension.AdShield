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
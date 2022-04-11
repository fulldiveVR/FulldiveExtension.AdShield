package model

import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {

    fun generateAppThemes(): List<AppTheme> {
        return listOf(
            AppTheme.LightAppTheme,
            AppTheme.AutoAppTheme,
            AppTheme.DarkAppTheme
        )
    }

    fun setCurrentAppTheme(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
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

package ui.advanced.presets

import androidx.annotation.StringRes
import org.adshield.R
import ui.advanced.packs.PacksViewModel

sealed class PacksPreset(val id: String, @StringRes val titleRes: Int, @StringRes  val descriptionRes: Int) {
    object Default : PacksPreset("default", R.string.str_default_preset_title, R.string.str_default_preset_description) {

        private const val packsLimit = 2
        private const val configsLimit = 1

        override fun updateConfig(config: List<PacksViewModel.PackEntity>): List<PacksViewModel.PackEntity> {
            var packsCounter = 0
            var configsCounter = 0
            return config.map { entity ->
                configsCounter = 0
                val newConfigs = entity.configs.map { packConfig ->
                    val newConfig = packConfig
                        .copy(isActive = packsCounter < packsLimit && configsCounter < configsLimit)
                    configsCounter++
                    newConfig
                }
                packsCounter++
                entity.copy(configs = newConfigs)
            }
        }
    }

    object Advanced : PacksPreset("advanced", R.string.str_balance_preset_title, R.string.str_balance_preset_description) {
        private const val configsLimit = 1

        override fun updateConfig(config: List<PacksViewModel.PackEntity>): List<PacksViewModel.PackEntity> {
            var configsCounter: Int
            return config.map { entity ->
                configsCounter = 0
                val newConfigs = entity.configs.map { packConfig ->
                    val newConfig = packConfig
                        .copy(isActive = configsCounter < configsLimit)
                    configsCounter++
                    newConfig
                }
                entity.copy(configs = newConfigs)
            }
        }
    }

    object Hard : PacksPreset("hard", R.string.str_shield_preset_title, R.string.str_shield_preset_description) {
        override fun updateConfig(config: List<PacksViewModel.PackEntity>): List<PacksViewModel.PackEntity> {
            return config.map { entity ->
                entity.copy(configs = entity.configs.map { packConfig ->
                    packConfig.copy(isActive = true)
                })
            }
        }
    }

    abstract fun updateConfig(config: List<PacksViewModel.PackEntity>): List<PacksViewModel.PackEntity>

    companion object {
        fun getPresets() = listOf(
            Default, Advanced, Hard
        )
    }
}

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

import androidx.annotation.StringRes
import org.adshield.R
import service.RemoteConfigService
import utils.Links

sealed class WebSettings(val id: Int, @StringRes val titleRes: Int, val url: String) {
    object BlockListsSettings : WebSettings(
        0, R.string.dns_forwarding_lists_description, Links.dnsSettings
    )

    object AppsListsSettings : WebSettings(
        1, R.string.advanced_section_slugline_apps, Links.appsSettings
    )

    object CustomBlockListsSettings : WebSettings(
        2, R.string.str_custom_forwarding_lists_description, Links.customSettings
    )

    object Empty : WebSettings(999, 0, "")

    companion object {
        fun getWebSettings() = listOfNotNull(
            BlockListsSettings,
            AppsListsSettings.takeIf { !RemoteConfigService.getIsWebAppsSettingsLimited() },
            CustomBlockListsSettings.takeIf { !RemoteConfigService.getIsWebCustomSettingsEnabled() }
        )
    }
}

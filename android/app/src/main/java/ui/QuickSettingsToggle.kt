/*
 * This file is part of Blokada.
 *
 * Blokada is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Blokada is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Blokada.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright © 2020 Blocka AB. All rights reserved.
 *
 * @author Karol Gusak (karol@blocka.net)
 */

package ui

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.lifecycle.ViewModelProvider
import model.TunnelStatus
import org.blokada.R
import utils.Logger

class QuickSettingsToggle : TileService() {

    private val log = Logger("QSTile")

    private val vm by lazy {
        val vm = ViewModelProvider(app()).get(TunnelViewModel::class.java)
        vm.tunnelStatus.observeForever {
            if (tileActive) {
                syncStatus(it)
            }
        }
        vm
    }

    private var tileActive = false

    override fun onStartListening() {
        tileActive = true
        syncStatus()
    }

    override fun onStopListening() {
        tileActive = false
    }

    override fun onTileAdded() {
        syncStatus()
    }

    override fun onClick() {
        syncStatus()?.let { isActive ->
            if (isActive) {
                log.v("Turning off from QuickSettings")
                vm.turnOff()
            } else {
                log.v("Turning on from QuickSettings")
                vm.turnOn()
            }
        }
    }

    private fun syncStatus(status: TunnelStatus? = vm.tunnelStatus.value): IsActive? {
        return when {
            qsTile == null -> null
            status == null -> {
                showOff()
                false
            }
            status.inProgress -> {
                showActivating()
                null
            }
            status.active -> {
                showOn()
                true
            }
            else -> {
                showOff()
                false
            }
        }
    }

    private fun showActivating() {
        qsTile.state = Tile.STATE_ACTIVE
        qsTile.label = "..."
        qsTile.updateTile()
    }

    private fun showOff() {
        qsTile.state = Tile.STATE_INACTIVE
        qsTile.label = getString(R.string.home_status_deactivated)
        qsTile.updateTile()
    }

    private fun showOn() {
        qsTile.state = Tile.STATE_ACTIVE
        qsTile.label = getString(R.string.home_status_active)
        qsTile.updateTile()
    }

}

private typealias IsActive = Boolean
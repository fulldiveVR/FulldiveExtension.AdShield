/*
 * This file is part of Blokada.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright © 2022 Blocka AB. All rights reserved.
 *
 * @author Karol Gusak (karol@blocka.net)
 */

package service

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import engine.Host
import kotlinx.coroutines.CompletableDeferred
import model.Stats
import model.TunnelStatus
import utils.Logger
import utils.MonitorNotification

object MonitorService {

    private var strategy: MonitorServiceStrategy = SimpleMonitorServiceStrategy()

    fun setup(useForeground: Boolean) {
        strategy.setup()
    }

    fun setCounter(counter: Long) = strategy.setCounter(counter)
    fun setStats(stats: Stats) = strategy.setStats(stats)
    fun setTunnelStatus(tunnelStatus: TunnelStatus) = strategy.setTunnelStatus(tunnelStatus)

}

private interface MonitorServiceStrategy {
    fun setup()
    fun setCounter(counter: Long)
    fun setStats(stats: Stats)
    fun setTunnelStatus(tunnelStatus: TunnelStatus)
}

// This strategy just shows the notification
private class SimpleMonitorServiceStrategy : MonitorServiceStrategy {

    private val notification = NotificationService

    private var counter: Long = 0
    private var lastDenied: List<Host> = emptyList()
    private var tunnelStatus: TunnelStatus = TunnelStatus.off()

    override fun setup() {}

    override fun setCounter(counter: Long) {
        this.counter = counter
        updateNotification()
    }

    override fun setStats(stats: Stats) {
        lastDenied = stats.entries.sortedByDescending { it.time }.take(3).map { it.name }
        updateNotification()
    }

    override fun setTunnelStatus(tunnelStatus: TunnelStatus) {
        this.tunnelStatus = tunnelStatus
        updateNotification()
    }

    private fun updateNotification() {
        if (tunnelStatus.active) {
            val prototype = MonitorNotification(tunnelStatus, counter, lastDenied)
            notification.show(prototype)
        }
    }

}

class ForegroundService : Service() {

    private val notification = NotificationService
    private var binder: ForegroundBinder? = null

    private var counter: Long = 0
    private var lastDenied: List<Host> = emptyList()
    private var tunnelStatus: TunnelStatus = TunnelStatus.off()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ContextService.setContext(this)
        updateNotification()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        if (FOREGROUND_BINDER_ACTION == intent?.action) {
            ContextService.setContext(this)
            binder = ForegroundBinder { counter, lastDenied, tunnelStatus, dnsLabel ->
                this.counter = counter ?: this.counter
                this.lastDenied = lastDenied ?: this.lastDenied
                this.tunnelStatus = tunnelStatus ?: this.tunnelStatus
                updateNotification()
            }
            return binder
        }
        return null
    }

    private fun updateNotification() {
        val prototype = MonitorNotification(tunnelStatus, counter, lastDenied)
        val n = notification.build(prototype)
        startForeground(prototype.id, n)
    }

}

class ForegroundBinder(
    val onNewStats: (counter: Long?, lastDenied: List<Host>?, tunnelStatus: TunnelStatus?, dnsLabel: String?) -> Unit
) : Binder()

const val FOREGROUND_BINDER_ACTION = "ForegroundBinder"

private class ForegroundConnection(
    private val deferred: ConnectDeferred,
    val onConnectionClosed: () -> Unit
) : ServiceConnection {

    private val log = Logger("ForegroundConnection")

    lateinit var binder: ForegroundBinder

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        this.binder = binder as ForegroundBinder
        deferred.complete(this.binder)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        log.w("onServiceDisconnected")
        onConnectionClosed()
    }

}

private typealias ConnectDeferred = CompletableDeferred<ForegroundBinder>

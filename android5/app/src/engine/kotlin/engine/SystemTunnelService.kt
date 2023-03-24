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

package engine

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.IBinder
import kotlinx.coroutines.CompletableDeferred
import model.BlokadaException
import model.TunnelStatus
import service.ContextService
import ui.utils.cause
import utils.Logger
import java.net.DatagramSocket
import java.net.Socket

object SystemTunnelService {

    private val log = Logger("SystemTunnel")
    private val context = ContextService

    private var connection: SystemTunnelConnection? = null
        @Synchronized get
        @Synchronized set

    var onConfigureTunnel: (vpn: VpnService.Builder) -> Unit = {}
    var onTunnelClosed = { ex: BlokadaException? -> }

    private var tunnel: SystemTunnel? = null

    fun setup() {
        log.v("Starting SystemTunnel service")
        val ctx = context.requireAppContext()
        val intent = Intent(ctx, SystemTunnel::class.java)
        ctx.startService(intent)
    }

    suspend fun getStatus(): TunnelStatus {
        return try {
            val hasFileDescriptor = getConnection().binder.tunnel.queryConfig() != null
            if (hasFileDescriptor) TunnelStatus.filteringOnly()
            else TunnelStatus.off()
        } catch (ex: Exception) {
            log.e("Could not get tunnel status".cause(ex))
            TunnelStatus.error(BlokadaException(ex.message ?: "Unknown reason"))
        }
    }

    suspend fun open(): SystemTunnelConfig {
        log.v("Received a request to open tunnel")
        try {
            return getConnection().binder.tunnel.turnOn()
        } catch (ex: Exception) {
            log.w("Could not turn on, unbinding to rebind on next attempt: ${ex.message}")
            unbind()
            throw ex
        }
    }

    suspend fun close() {
        log.v("Received a request to close tunnel")
        getConnection().binder.tunnel.turnOff()
    }

    suspend fun getTunnelConfig(): SystemTunnelConfig {
        return getConnection().binder.tunnel.queryConfig()
            ?: throw BlokadaException("No system tunnel started")
    }

    fun protectSocket(socket: DatagramSocket) {
        tunnel?.protect(socket) ?: log.e("No tunnel reference while called protectSocket()")
    }

    fun protectSocket(socket: Socket) {
        tunnel?.protect(socket) ?: log.e("No tunnel reference while called protectSocket()")
    }

    private suspend fun getConnection(): SystemTunnelConnection {
        return connection ?: run {
            val deferred = CompletableDeferred<SystemTunnelBinder>()
            val connection = bind(deferred)
            deferred.await()
            log.v("Bound SystemTunnel")
            this.connection = connection
            this.tunnel = connection.binder.tunnel
            connection
        }
    }

    private suspend fun bind(deferred: ConnectDeferred): SystemTunnelConnection {
        log.v("Binding SystemTunnel")
        val ctx = context.requireAppContext()
        val intent = Intent(ctx, SystemTunnel::class.java).apply {
            action = SYSTEM_TUNNEL_BINDER_ACTION
        }

        val connection = SystemTunnelConnection(deferred, { onConfigureTunnel(it) },
            onTunnelClosed = {
                log.w("Tunnel got closed, unbinding (if bound)")
                unbind()
                this.onTunnelClosed(it)
            },
            onConnectionClosed = {
                this.connection = null
            })
        if (!ctx.bindService(intent, connection,
                Context.BIND_AUTO_CREATE or Context.BIND_ABOVE_CLIENT or Context.BIND_IMPORTANT
        )) {
            deferred.completeExceptionally(BlokadaException("Could not bindService()"))
        } else {
//            delay(3000)
//            if (!deferred.isCompleted) deferred.completeExceptionally(
//                BlokadaException("Timeout waiting for bindService()")
//            )
        }
        return connection
    }

    fun unbind() {
        connection?.let {
            log.v("Unbinding SystemTunnel")
            try {
                val ctx = context.requireAppContext()
                ctx.unbindService(it)
                log.v("unbindService called")
            } catch (ex: Exception) {
                log.w("unbindService failed: ${ex.message}")
            }
        }
        connection = null
    }

}

private class SystemTunnelConnection(
    private val deferred: ConnectDeferred,
    var onConfigureTunnel: (vpn: VpnService.Builder) -> Unit,
    var onTunnelClosed: (exception: BlokadaException?) -> Unit,
    val onConnectionClosed: () -> Unit
): ServiceConnection {

    private val log = Logger("SystemTunnel")

    lateinit var binder: SystemTunnelBinder

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        this.binder = binder as SystemTunnelBinder
        binder.onConfigureTunnel = onConfigureTunnel
        binder.onTunnelClosed = onTunnelClosed
        deferred.complete(this.binder)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        log.w("onServiceDisconnected")
        onConnectionClosed()
    }

}

private typealias ConnectDeferred = CompletableDeferred<SystemTunnelBinder>
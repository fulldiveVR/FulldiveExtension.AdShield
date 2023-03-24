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

import android.net.VpnService
import android.os.Build
import model.*
import repository.AppRepository
import repository.DnsDataSource
import ui.utils.cause
import utils.Logger
import java.net.Inet4Address
import java.net.Inet6Address

object SystemTunnelConfigurator {

    private val log = Logger("STConfigurator")
    private val apps = AppRepository

    fun forPlus(tun: VpnService.Builder, ipv6: Boolean, dns: Dns, lease: Lease) {
        log.v("Configuring VPN for Plus mode")

        if (ipv6) {
            log.v("Using IP: ${lease.vip4}, ${lease.vip6}")
            tun.addAddress(lease.vip4, 32)
            tun.addAddress(lease.vip6, 128)
        } else {
            log.v("Using IP: ${lease.vip4}")
            tun.addAddress(lease.vip4, 32)
        }

        var index = 1
        for (address in dns.ips.includeIpv6(false)) {
            try {
                log.v("Adding DNS server: $address")
                tun.addMappedDnsServer(address, index++)
            } catch (ex: Exception) {
                log.e("Failed adding DNS server".cause(ex))
            }
        }

        log.v("Setting only public networks as routes for IPv4")
        IPV4_PUBLIC_NETWORKS.forEach {
            val (ip, mask) = it.split("/")
            tun.addRoute(ip, mask.toInt())
        }

        if (dns == DnsDataSource.blocka) {
            log.v("Adding route for Blocka DNS")
            tun.addRoute("10.143.0.0", 24)
        }

        if (ipv6) {
            log.v("Setting all networks as routes for IPv6")
            tun.addRoute("::", 0)
        }

        log.v("Setting MTU: $MTU")
        tun.setMtu(MTU)
        tun.setBlocking(true)

        // To not show our VPN as a metered connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tun.setMetered(false)
        }

        val bypassed = apps.getPackageNamesOfAppsToBypass(forRealTunnel = true)
        log.v("Setting bypass for ${bypassed.count()} apps")
        bypassed.forEach {
            tun.addDisallowedApplication(it)
        }
    }

    fun forLibre(tun: VpnService.Builder, dns: Dns, ipv6: Boolean) {
        if (dns == DnsDataSource.blocka) {
            throw BlockaDnsInFilteringMode()
        }

        log.v("Configuring VPN for Libre mode")

        // TEST-NET IP range from RFC5735
        var ip = "203.0.113.69"
        try {
            tun.addAddress(ip, 24)
        } catch (e: IllegalArgumentException) {
            ip = "192.168.50.1"
            tun.addAddress(ip, 24)
        }

        log.v("Using IP: $ip")

        if (ipv6 && dns.ips.ipv6().isNotEmpty()) {
            // Also a special subnet (2001:DB8::/32), from RFC3849. Meant for documentation use.
            val ipv6 = "2001:db8:0:0:0:0:0:0"

            try {
                val address = Inet6Address.getByName(ipv6)
                tun.addAddress(address, 120)
                log.v("Using IPv6: $ipv6")
            } catch (ex: Exception) {
                log.e("Failed adding IPv6 address".cause(ex))
            }
        }

        var index = 1
        for (address in dns.ips.includeIpv6(false)) {
            try {
                log.v("Adding DNS server: $address")
                tun.addMappedDnsServer(address, index++, addRoute = true)
            } catch (ex: Exception) {
                log.e("Failed adding DNS server".cause(ex))
            }
        }

        log.v("Setting MTU: $MTU")
        tun.setMtu(MTU)
        tun.setBlocking(true)

        // To not show our VPN as a metered connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tun.setMetered(false)
        }

        /**
         * This may fix the problematic apps that have been reported to not work under Blokada.
         * In Libre mode, we allow for them to just bypass us. Better to let the ads go through,
         * than to break the app. We do not do that for the Plus mode.
          */
        log.w("Allowing bypass (experimental)")
        tun.allowBypass()

        val bypassed = apps.getPackageNamesOfAppsToBypass()
        log.v("Setting bypass for ${bypassed.count()} apps")
        bypassed.forEach {
            tun.addDisallowedApplication(it)
        }
    }

    fun forSlim(tun: VpnService.Builder, doh: Boolean, dns: Dns, ipv6: Boolean) {
        if (dns.id == "blocka") {
            throw BlockaDnsInFilteringMode()
        }

        log.v("Configuring VPN for Slim mode")

        // TEST-NET IP range from RFC5735
        var ip = "203.0.113.69"
        try {
            tun.addAddress(ip, 24)
        } catch (e: IllegalArgumentException) {
            ip = "192.168.50.1"
            tun.addAddress(ip, 24)
        }

        log.v("Using IP: $ip")

        if (ipv6 && dns.ips.ipv6().isNotEmpty()) {
            // Also a special subnet (2001:DB8::/32), from RFC3849. Meant for documentation use.
            val ipv6 = "2001:db8:0:0:0:0:0:0"

            try {
                val address = Inet6Address.getByName(ipv6)
                tun.addAddress(address, 120)
                log.v("Using IPv6: $ipv6")
            } catch (ex: Exception) {
                log.e("Failed adding IPv6 address".cause(ex))
            }
        }

        if (doh && dns.isDnsOverHttps()) {
            try {
                log.v("Adding DNS server proxy for DoH")
                tun.addDnsServer(DnsMapperService.proxyDnsIp)
            } catch (ex: Exception) {
                log.e("Failed adding DNS server".cause(ex))
            }
        } else {
            for (address in dns.ips.includeIpv6(false)) {
                try {
                    log.v("Adding DNS server: $address")
                    tun.addDnsServer(address)
                } catch (ex: Exception) {
                    log.e("Failed adding DNS server".cause(ex))
                }
            }
        }

        tun.setBlocking(true)

        // To not show our VPN as a metered connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tun.setMetered(false)
        }

        /**
         * This may fix the problematic apps that have been reported to not work under Blokada.
         * In Slim mode, we allow for them to just bypass us. Better to let the ads go through,
         * than to break the app. We do not do that for the Plus mode.
         */
        log.w("Allowing bypass (experimental)")
        tun.allowBypass()

        val bypassed = apps.getPackageNamesOfAppsToBypass()
        log.v("Setting bypass for ${bypassed.count()} apps")
        bypassed.forEach {
            tun.addDisallowedApplication(it)
        }
    }

    private fun VpnService.Builder.addMappedDnsServer(address: DnsIp, index: Int, addRoute: Boolean = false) {
        log.v("Adding mapped DNS server for IPv4")
        val template = dnsProxyDst4.copyOf()
        template[template.size - 1] = (index).toByte()
        val add = Inet4Address.getByAddress(template)
        this.addDnsServer(add)
        if (addRoute) this.addRoute(add, 32)
    }

}

internal val MTU = 1280

private val IPV4_PUBLIC_NETWORKS = listOf(
    "0.0.0.0/5", "8.0.0.0/7", "11.0.0.0/8", "12.0.0.0/6", "16.0.0.0/4", "32.0.0.0/3",
    "64.0.0.0/2", "128.0.0.0/3", "160.0.0.0/5", "168.0.0.0/6", "172.0.0.0/12",
    "172.32.0.0/11", "172.64.0.0/10", "172.128.0.0/9", "173.0.0.0/8", "174.0.0.0/7",
    "176.0.0.0/4", "192.0.0.0/9", "192.128.0.0/11", "192.160.0.0/13", "192.169.0.0/16",
    "192.170.0.0/15", "192.172.0.0/14", "192.176.0.0/12", "192.192.0.0/10",
    "193.0.0.0/8", "194.0.0.0/7", "196.0.0.0/6", "200.0.0.0/5", "208.0.0.0/4"
)

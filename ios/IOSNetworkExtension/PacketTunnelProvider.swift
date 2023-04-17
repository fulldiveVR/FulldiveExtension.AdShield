//
//  This file is part of Blokada.
//
//  Blokada is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  Blokada is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with Blokada.  If not, see <https://www.gnu.org/licenses/>.
//
//  Copyright © 2020 Blocka AB. All rights reserved.
//
//  @author Karol Gusak
//

import Network
import NetworkExtension
import os.log

struct TunnelConfig {
    var privateKey: String
    var gatewayId: String
    var gatewayIpv4: String
    var gatewayIpv6: String
    var gatewayPort: String
    var vip4: String
    var vip6: String
}

enum PacketTunnelProviderError: String, Error {
    case couldNotStartBackend
    case couldNotDetermineFileDescriptor
    case couldNotSetNetworkSettings
}

enum IPStack {
    case IPv4
    case IPv6
}

class PacketTunnelProvider: NEPacketTunnelProvider, TunnelSessionDelegate {
    var tunnelStarted = false
    private var dnsHandle: OpaquePointer?
    private var apiHandle: OpaquePointer?
    private var device = PacketLoop()
    private var deviceStack: IPStack = .IPv4
    private var tunnelConfig: TunnelConfig?
    private var previousConfig: TunnelConfig?
    private var networkMonitor: NWPathMonitor?
    private var currentGatewayStack: IPStack?
    private var pauseTimer: Timer?

    deinit {
        networkMonitor?.cancel()
    }

    override func startTunnel(options: [String : NSObject]?, completionHandler startTunnelCompletionHandler: @escaping (Error?) -> Void) {
        self.persistedStats = Stats.load()

        engine_logger("info")
        freopen(LoggerSaver.logFile?.path.cString(using: .ascii)!, "a+", stderr)
        freopen(LoggerSaver.logFile?.path.cString(using: .ascii)!, "a+", stdout)

        panic_hook { (msg) in
            let str = String(cString: msg!)
            NELogger.e("PacketTunnelProvider: Blocka Engine: \(str)")
        }

        NELogger.v("PacketTunnelProvider: startTunnel")
        if (tunnelStarted) {
            NELogger.v("PacketTunnelProvider: tunnel already started, ignoring")
            return startTunnelCompletionHandler(nil)
        }
        tunnelStarted = true

        guard let config = (protocolConfiguration as? NETunnelProviderProtocol)?.providerConfiguration else {
            return startTunnelCompletionHandler("No protocolConfiguration specified")
        }

        self.apiHandle = api_new(10, config["userAgent"] as! String)

        self.setupDNS(dnsName: config["dnsName"] as! String, dnsIps: config["dnsIps"] as! String, dnsPath: config["dnsPath"] as! String)

        if config["mode"] as? String == "plus" {
            tunnelConfig = TunnelConfig(
                privateKey: config["privateKey"] as! String,
                gatewayId: config["gatewayId"] as! String,
                gatewayIpv4: config["ipv4"] as! String,
                gatewayIpv6: config["ipv6"] as! String,
                gatewayPort: config["port"] as! String,
                vip4: config["vip4"] as! String,
                vip6: config["vip6"] as! String
            )
            return self.connectVPN(completionHandler: startTunnelCompletionHandler)
        }

        NELogger.v("PacketTunnelProvider: startTunnel: using black hole configuration")
        self.filteringOnly(completionHandler: startTunnelCompletionHandler)
    }

    private func connectVPN(completionHandler: @escaping ((Error?)) -> Void) {
        NELogger.v("PacketTunnelProvider: connectVPN")
        let settings = createTunnelSettings(gatewayIp: tunnelConfig!.gatewayIpv4, vip4: tunnelConfig!.vip4, vip6: tunnelConfig!.vip6, defaultRoute: true)

        setTunnelNetworkSettings(settings) { error in
            if let error = error {
                NELogger.e("PacketTunnelProvider: failed connectVPN with tunnel network settings".cause(error))
                completionHandler(PacketTunnelProviderError.couldNotSetNetworkSettings)
                return
            }
            guard let config = self.tunnelConfig else { fatalError("connectVPN: no config") }

            NELogger.v("PacketTunnelProvider: connecting to: \(self.tunnelConfig!.gatewayId)")
            self.device.stop()
            self.networkMonitor = NWPathMonitor()
            self.networkMonitor?.start(queue: .global())
            self.device.start(privateKey: config.privateKey, gatewayKey: config.gatewayId, delegate: self)
            self.dnsVia(mode: TunneledInterface)
            NELogger.v("PacketTunnelProvider: real VPN established")
            completionHandler(nil)
        }
    }

    override func sleep(completionHandler: @escaping () -> Void) {
        self.device.sleep()
        completionHandler()
    }

    override func wake() {
        self.device.wake()
    }

    func createSession() -> NWUDPSession {
        guard let config = self.tunnelConfig else { fatalError("connectVPN: no config") }

        // Try to cycle the stacks on each session or fallback to ipv4
        switch deviceStack {
        case .IPv4:
            if let nw = networkMonitor, nw.currentPath.supportsIPv6 {
                deviceStack = .IPv6
                return createUDPSession(to: NWHostEndpoint(hostname: config.gatewayIpv6, port: config.gatewayPort), from: nil)
            }
        case .IPv6:
            if let nw = networkMonitor, nw.currentPath.supportsIPv4 {
                deviceStack = .IPv4
                return createUDPSession(to: NWHostEndpoint(hostname: config.gatewayIpv4, port: config.gatewayPort), from: nil)
            }
        }
        deviceStack = .IPv4
        return createUDPSession(to: NWHostEndpoint(hostname: config.gatewayIpv4, port: config.gatewayPort), from: nil)
    }

    func hasBetterEndpoint(_ session: NWUDPSession) -> Bool {
        guard let currentPath = networkMonitor?.currentPath else { return false }
        guard let sessionPath = session.currentPath else { return false }

        if currentPath.isExpensive { return false }
        if !sessionPath.isExpensive { return false }

        switch deviceStack {
        case .IPv6:
            return currentPath.supportsIPv4
        case .IPv4:
            return currentPath.supportsIPv6
        }
    }

    func writePackets(_ packets: [Data], withProtocols protocols: [NSNumber]) {
        self.packetFlow.writePackets(packets, withProtocols: protocols)
    }

    func readPacketObjects(completionHandler: @escaping ([NEPacket]) -> Void) {
        self.packetFlow.readPacketObjects(completionHandler: completionHandler)
    }

    private func filteringOnly(completionHandler: @escaping ((Error?)) -> Void) {
        NELogger.v("PacketTunnelProvider: filteringOnly")
        tunnelConfig = nil
        disconnectVPN()

        setTunnelNetworkSettings(createFilteringOnlySettings()) { error in
            if let error = error {
                NELogger.e("PacketTunnelProvider: failed filteringOnly with tunnel network settings".cause(error))
                completionHandler(PacketTunnelProviderError.couldNotSetNetworkSettings)
            } else {
                completionHandler(nil)
            }
        }
    }

    private func dnsVia(mode: TunnelMode) {
        guard let dnsHandle = dnsHandle else { return }
        dns_via(dnsHandle, mode)
    }

    private func setupDNS(dnsName: String, dnsIps: String, dnsPath: String) {
        self.dnsHandle = new_dns(
            "127.0.0.1:53", self.blocklist(), self.allowlist(),
            dnsIps, dnsName, dnsPath
        )
        if self.dnsHandle == nil {
            fatalError("could not start DNS")
        }
    }

    private func blocklist() -> String {
        if let packs = FileManager.default.containerURL(
            forSecurityApplicationGroupIdentifier: "group.net.blocka.app")?.appendingPathComponent("hosts"),
            FileManager.default.fileExists(atPath: packs.path)
        {
            NELogger.v("Using blocklist path: \(packs.path)")
            return packs.path
        }

        // static default list based on energized
        NELogger.w("Using hardcoded hosts list")
        return Bundle(for: type(of : self)).path(forResource: "hosts", ofType: "txt")!
    }

    private func allowlist() -> String? {
        if let allow = FileManager.default.containerURL(
            forSecurityApplicationGroupIdentifier: "group.net.blocka.app")?.appendingPathComponent("allow"),
            FileManager.default.fileExists(atPath: allow.path)
        {
            NELogger.v("Using allowlist path: \(allow.path)")
            return allow.path
        }

        return nil
    }

    private func tearDownDNS() {
        guard let dnsHandle = dnsHandle else { return }
        dns_close(dnsHandle)
        self.dnsHandle = nil
        NELogger.v("PacketTunnelProvider: TearDownDNS done")
    }

    private func disconnectVPN() {
        dnsVia(mode: DefaultInterface)
        self.device.stop()
        self.networkMonitor?.cancel()
        self.networkMonitor = nil
        NELogger.v("PacketTunnelProvider: disconnectVPN done")
    }

    private func performProtectedHttpRequest(url: String, method: String, body: String, completionHandler: @escaping ((Error?, String?) -> Void)) {
        let res = api_request(self.apiHandle, method, url, body)
        defer { api_response_free(res) }

        if res!.pointee.error != nil {
            let err = String(cString: res!.pointee.error!)
            return completionHandler(err, nil)
        } else if res!.pointee.code != 200 {
            return completionHandler("code: \(res!.pointee.code)", nil)
        }
        let responseBody = String(cString: res!.pointee.body)
        completionHandler(nil, responseBody)
    }

    private func createTunnelSettings(gatewayIp: String, vip4: String, vip6: String, defaultRoute: Bool) -> NEPacketTunnelNetworkSettings {
        let dns = NEDNSSettings(servers: ["127.0.0.1"])
        dns.matchDomains = [""]

        let newSettings = NEPacketTunnelNetworkSettings(tunnelRemoteAddress: gatewayIp)
        newSettings.dnsSettings = dns
        newSettings.mtu = 1280

        if defaultRoute {
            let ipv4 = NEIPv4Settings(addresses: [vip4], subnetMasks: ["255.255.255.255"])
            let ipv6 = NEIPv6Settings(addresses: [vip6], networkPrefixLengths: [64])
            ipv4.includedRoutes = [NEIPv4Route.default()]
            ipv6.includedRoutes = [NEIPv6Route.default()]
            newSettings.ipv4Settings = ipv4
            newSettings.ipv6Settings = ipv6
        }

        return newSettings
    }

    private func createFilteringOnlySettings() -> NEPacketTunnelNetworkSettings {
        return createTunnelSettings(gatewayIp: "127.0.0.1", vip4: "127.0.0.1", vip6: "::1", defaultRoute: false)
    }

    override func stopTunnel(with reason: NEProviderStopReason, completionHandler: @escaping () -> Void) {
        NELogger.v("PacketTunnelProvider: stopTunnel")
        self.stats()?.persist()
        // We implicitly stop the dns & device because the process will exit here
        NELogger.v("PacketTunnelProvider: stopTunnel: done")
        tunnelStarted = false
        completionHandler()
    }

    private func pause(seconds: Int) {
        guard let dnsHandle = self.dnsHandle else { return }

        if let timer = self.pauseTimer {
            timer.invalidate()
            self.pauseTimer = nil
        }
        if seconds == 0 {
            return self.unpause()
        }

        // Start pausing
        NELogger.v("PacketTunnelProvider: pausing for \(seconds)s")
        self.stats()?.persist()
        self.pauseTimer = Timer.scheduledTimer(
            timeInterval: TimeInterval(seconds),
            target: self, selector: #selector(unpause), userInfo: nil, repeats: false
        )
        dns_use_lists(dnsHandle, nil, nil)
    }

    @objc private func unpause() {
        guard tunnelStarted else { return }
        guard let dnsHandle = self.dnsHandle else { return }

        NELogger.v("PacketTunnelProvider: unpausing")
        self.persistedStats = Stats.load()
        dns_use_lists(dnsHandle, self.blocklist(), nil)
    }

    private func reload_lists() -> Error? {
        guard tunnelStarted else { return "not started" }
        guard let dnsHandle = self.dnsHandle else { return "no dns handle" }

        // persist stats since in-memory cache will reset after reload
        self.stats()?.persist()
        defer { self.persistedStats = Stats.load() }

        NELogger.v("PacketTunnelProvider: reload lists")
        if !dns_use_lists(dnsHandle, self.blocklist(), self.allowlist()) {
            return "could not reload lists"
        }
        return nil
    }

    private var persistedStats: Stats = Stats.empty()

    private func stats() -> Stats? {
        guard let dnsHandle = self.dnsHandle else { return nil }
        let history = dns_history(dnsHandle)
        defer { dns_history_free(history) }

        var all: [HistoryEntry] = []
        for entry in UnsafeBufferPointer(start: history.ptr, count: Int(history.len)) {
            all.append(HistoryEntry(
                name: String(String(cString: entry.name).dropLast(1)), // Remove the trailing dot
                type: HistoryEntryType.fromTypedef(value: entry.action),
                time: Date(timeIntervalSince1970: Double(entry.unix_time)),
                requests: entry.requests
            ))
        }

        return persistedStats.combine(with: Stats(
            allowed: history.allowed_requests,
            denied: history.denied_requests,
            entries: all
        ))
    }

    override func handleAppMessage(_ messageData: Data, completionHandler: ((Data?) -> Void)?) {
        let data = String.init(data: messageData, encoding: String.Encoding.utf8)!
        let params = data.components(separatedBy: " ")
        let command = params[0]
        NELogger.v("PacketTunnelProvider: command received: \(command)")

        switch command {
        case "connect":
            previousConfig = tunnelConfig
            tunnelConfig = TunnelConfig(privateKey: params[1], gatewayId: params[2], gatewayIpv4: params[3], gatewayIpv6: params[4], gatewayPort: params[5], vip4: params[6], vip6: params[7])
            if (tunnelStarted) {
                connectVPN { error in
                    self.respond(command: command, error: error, response: "", completionHandler: completionHandler)
                }
            } else {
                NELogger.v("PacketTunnelProvider: connect: tunnel not started, just saved configuration")
                self.respond(command: command, error: nil, response: "", completionHandler: completionHandler)
            }
        case "disconnect":
            filteringOnly { error in
                self.respond(command: command, error: error, response: "", completionHandler: completionHandler)
            }
        case "request":
            let url = params[1]
            let method = params[2]
            let body = data.components(separatedBy: ":body:")[1]
            NELogger.v("PacketTunnelProvider: request: \(method) \(url)")
            performProtectedHttpRequest(url: url, method: method, body: body, completionHandler: { error, response in
                self.respond(command: command, error: error, response: response ?? "", completionHandler: completionHandler)
            })
        case "report":
            // Quick hack to make sure we can output logs after UI has truncated it.
            freopen(LoggerSaver.logFile?.path.cString(using: .ascii)!, "a+", stderr)
            freopen(LoggerSaver.logFile?.path.cString(using: .ascii)!, "a+", stdout)

            var tunnelState = "off"
            if tunnelStarted {
                tunnelState = String(Int(pauseTimer?.fireDate.timeIntervalSinceNow ?? 0))
            }
            self.respond(command: command, error: nil, response: tunnelState, completionHandler: completionHandler)
        case "pause":
            let pauseSeconds = Int(params[1]) ?? 0
            self.pause(seconds: pauseSeconds)
            self.respond(command: command, error: nil, response: "", completionHandler: completionHandler)
        case "reload_lists":
            let err = self.reload_lists()
            self.respond(command: command, error: err, response: "", completionHandler: completionHandler)
        case "stats":
            var response = ""
            if let statsString = self.stats()?.toJson() {
                response = statsString
            } else {
                NELogger.w("Could not parse stats")
            }

            self.respond(command: command, error: nil, response: response, completionHandler: completionHandler)
        default:
            NELogger.v("PacketTunnelProvider: message ignored, responding OK")
            let data = "".data(using: String.Encoding.utf8)
            if let handler = completionHandler {
                handler(data)
            }
        }
    }

    private func respond(command: String, error: Error?, response: String, completionHandler: ((Data?) -> Void)?) {
        var data = response.data(using: String.Encoding.utf8)
        if (error != nil) {
            NELogger.e("PacketTunnelProvider: \(command) responded with error".cause(error))
            data = ("error: " + error!.localizedDescription).data(using: String.Encoding.utf8)
        } else {
            NELogger.v("PacketTunnelProvider: \(command) responded with OK")
        }

        if let handler = completionHandler {
            handler(data)
        }
    }
}


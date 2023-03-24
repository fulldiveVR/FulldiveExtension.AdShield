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

import Foundation

struct Dns: Codable {
    let ips: [String]
    let port: Int
    let name: String
    let path: String
    let label: String
}


extension Dns: Hashable {
    func hash(into hasher: inout Hasher) {
        hasher.combine(label)
    }
}

extension Dns: Equatable {
    static func == (lhs: Dns, rhs: Dns) -> Bool {
        lhs.label == rhs.label
    }
}

extension Dns {

    func persist() {
        if let dnsString = self.toJson() {
            UserDefaults.standard.set(dnsString, forKey: "dns")
        } else {
            Logger.w("Dns", "Could not convert dns to json")
        }
    }

    static func load() -> Dns {
        let result = UserDefaults.standard.string(forKey: "dns")
        guard let stringData = result else {
            return Dns.defaultDns()
        }

        let jsonData = stringData.data(using: .utf8)
        guard let json = jsonData else {
            Logger.e("Dns", "Failed getting dns json")
            return Dns.defaultDns()
        }

        do {
            return try decoder.decode(Dns.self, from: json)
        } catch {
            Logger.e("Dns", "Failed decoding dns json".cause(error))
            return Dns.defaultDns()
        }
    }

    static var hardcoded = [
        //Dns(ips: ["176.103.130.130", "176.103.130.131", "2a00:5a60::ad1:0ff", "2a00:5a60::ad2:0ff"], port: 443, name: "dns.adguard.com", path: "dns-query", label: "Adguard"),
        //Dns(ips: ["185.228.168.9", "185.228.169.9", "2a0d:2a00:1::2", "2a0d:2a00:2::2"], port: 443, name: "doh.cleanbrowsing.org", path: "doh/security-filter", label: "CleanBrowsing: Security filter"),
        //Dns(ips: ["185.228.168.10", "185.228.169.11", "2a0d:2a00:1::1", "2a0d:2a00:2::1"], port: 443, name: "doh.cleanbrowsing.org", path: "doh/adult-filter", label: "CleanBrowsing: Adult filter"),
        Dns(ips: ["1.1.1.1", "1.0.0.1", "2606:4700:4700::1111", "2606:4700:4700::1001"], port: 443, name: "cloudflare-dns.com", path: "dns-query", label: "Cloudflare"),
        Dns(ips: ["1.1.1.2", "1.0.0.2", "2606:4700:4700::1112", "2606:4700:4700::1002"], port: 443, name: "cloudflare-dns.com", path: "dns-query", label: "Cloudflare: malware blocking"),
        Dns(ips: ["1.1.1.3", "1.0.0.3", "2606:4700:4700::1113", "2606:4700:4700::1003"], port: 443, name: "cloudflare-dns.com", path: "dns-query", label: "Cloudflare: malware & adult blocking"),
        Dns(ips: ["185.95.218.42", "185.95.218.43", "2a05:fc84::42", "2a05:fc84::43"], port: 443, name: "dns.digitale-gesellschaft.ch", path: "dns-query", label: "Digitale Gesellschaft (Switzerland)"),
        Dns(ips: ["8.8.8.8", "8.8.4.4", "2001:4860:4860::8888", "2001:4860:4860::8844"], port: 443, name: "dns.google", path: "resolve", label: "Google"),
        Dns(ips: ["155.138.240.237", "2001:19f0:6401:b3d:5400:2ff:fe5a:fb9f"], port: 443, name: "ns03.dns.tin-fan.com", path: "dns-query", label: "OpenNIC: USA"),
        Dns(ips: ["95.217.16.205", "2a01:4f9:c010:6093::3485"], port: 443, name: "ns01.dns.tin-fan.com", path: "dns-query", label: "OpenNIC: Europe")
    ] + (Env.isProduction ? [] : [
        // Debug only entries
        Dns(ips: ["0.0.0.0"], port: 443, name: "localhost", path: "dns-query", label: "Broken DNS (for testing only)")
        ])

    static func defaultDns() -> Dns {
        return hardcoded.first { $0.label == "Cloudflare" }!
    }

}

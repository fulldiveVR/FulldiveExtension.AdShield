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

package model

import com.squareup.moshi.JsonClass
import java.util.*

typealias AccountId = String
typealias GatewayId = String
typealias PrivateKey = String
typealias PublicKey = String
typealias ActiveUntil = Date
typealias DeviceId = String

@JsonClass(generateAdapter = true)
data class AccountWrapper(val account: Account)

@JsonClass(generateAdapter = true)
data class LeaseWrapper(val lease: Lease)

@JsonClass(generateAdapter = true)
data class Gateways(val gateways: List<Gateway>)

@JsonClass(generateAdapter = true)
data class Leases(val leases: List<Lease>)

@JsonClass(generateAdapter = true)
data class Account(
    val id: AccountId,
    val active_until: ActiveUntil = Date(0)
) {
    fun isActive() = active_until > Date()

    override fun toString(): String {
        return "Account(activeUntil=$active_until)"
    }
}

@JsonClass(generateAdapter = true)
data class Gateway(
    val public_key: PublicKey,
    val region: String,
    val location: String,
    val resource_usage_percent: Int,
    val ipv4: String,
    val ipv6: String,
    val port: Int,
    val tags: List<String>?
) {
    fun niceName() = location.split('-').map { it.capitalize() }.joinToString(" ")
    fun overloaded() = resource_usage_percent >= 100

    companion object {}
}

@JsonClass(generateAdapter = true)
data class Lease(
    val account_id: AccountId,
    val public_key: PublicKey,
    val gateway_id: GatewayId,
    val expires: ActiveUntil,
    val alias: String?,
    val vip4: String,
    val vip6: String
) {
    fun niceName() = if (alias?.isNotBlank() == true) alias else public_key.take(5)

    fun isActive() = expires > Date()

    override fun toString(): String {
        // No account ID
        return "Lease(publicKey='$public_key', gatewayId='$gateway_id', expires=$expires, alias=$alias, vip4='$vip4', vip6='$vip6')"
    }

    companion object {}
}

@JsonClass(generateAdapter = true)
data class LeaseRequest(
    val account_id: AccountId,
    val public_key: PublicKey,
    val gateway_id: GatewayId,
    val alias: String
) {
    override fun toString(): String {
        // No account ID
        return "LeaseRequest(publicKey='$public_key', gatewayId='$gateway_id', alias='$alias')"
    }
}

fun Gateway.Companion.mocked(name: String) = Gateway(
    public_key = "mocked-$name",
    region = "mocked",
    location = name,
    resource_usage_percent = 0,
    ipv4 = "0.0.0.0",
    ipv6 = ":",
    port = 8080,
    tags = listOf()
)

fun Lease.Companion.mocked(name: String) = Lease(
    public_key = "mocked-$name",
    account_id = "mockedmocked",
    gateway_id = name,
    expires = Date(),
    alias = name,
    vip6 = ":",
    vip4 = "0.0.0.0"
)
package model

import com.squareup.moshi.JsonClass
import service.EnvironmentService

@JsonClass(generateAdapter = true)
data class BlockaConfig(
    val privateKey: PrivateKey,
    val publicKey: PublicKey,
    val keysGeneratedForAccountId: AccountId,
    val keysGeneratedForDevice: DeviceId,
    val lease: Lease?,
    val gateway: Gateway?,
    val vpnEnabled: Boolean,
    val tunnelEnabled: Boolean = false
) {
    fun getAccountId() = keysGeneratedForAccountId
    fun lease() = lease!!
    fun gateway() = gateway!!
}

// These settings are never backed up to the cloud
@JsonClass(generateAdapter = true)
data class LocalConfig(
    val dnsChoice: DnsId, // Deprecated
    val useChromeTabs: Boolean = false,
    val useDarkTheme: Boolean? = null,
    val themeName: String? = null,
    val locale: String? = null,
    val ipv6: Boolean = true, // Deprecated
    val backup: Boolean = true,
    val useDnsOverHttps: Boolean = false, // Deprecated
    val useBlockaDnsInPlusMode: Boolean = true, // Deprecated
    val escaped: Boolean = true,
    val useForegroundService: Boolean = true
)

// These settings are always backed up to the cloud (if possible)
@JsonClass(generateAdapter = true)
data class SyncableConfig(
    val rateAppShown: Boolean,
    val notFirstRun: Boolean,
    val rated: Boolean = false
)

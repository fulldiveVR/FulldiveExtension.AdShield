package model

import repository.DnsDataSource
import repository.PackDataSource
import service.EnvironmentService
import ui.utils.now

object Defaults {

    val PACKS_VERSION = 25

    fun stats() = StatsPersisted(entries = emptyMap())
    fun allowed() = Allowed(value = listOf())
    fun denied() = Denied(value = listOf())
    fun packs() = Packs(PackDataSource.getPacks(), version = PACKS_VERSION, lastRefreshMillis = 0)
    fun localConfig() = LocalConfig(dnsChoice = BuildSpecificDefaults.dns)
    fun syncableConfig() = SyncableConfig(rateAppShown = false, notFirstRun = false)
    fun dnsWrapper() = DnsWrapper(DnsDataSource.getDns())

    fun blockaConfig() = BlockaConfig(
        privateKey = "",
        publicKey = "",
        keysGeneratedForAccountId = "",
        keysGeneratedForDevice = EnvironmentService.getDeviceId(),
        lease = null,
        gateway = null,
        vpnEnabled = false
    )

    fun adsCounter() = AdsCounter(persistedValue = 0L)

    fun bypassedAppIds() = BypassedAppIds(emptyList()) // Also check AppRepository

    fun blockaRepoConfig() = BlockaRepoConfig(
        name = "default",
        forBuild = "*"
    )

    fun noSeenUpdate() = BlockaRepoUpdate(
        mirrors = emptyList(),
        infoUrl = "",
        newest = ""
    )

    fun noPayload() = BlockaRepoPayload(
        cmd = ""
    )

    fun noAfterUpdate() = BlockaAfterUpdate()

    fun noNetworkSpecificConfigs() = NetworkSpecificConfigs(configs = listOf(
        defaultNetworkConfig(),
        defaultNetworkConfig().copy(network = NetworkDescriptor.cell(null)),
        defaultNetworkConfig().copy(network = NetworkDescriptor.wifi(null))
    ))

    fun networkConfig(network: NetworkDescriptor) = defaultNetworkConfig().copy(network = network)

    fun defaultNetworkConfig() = NetworkSpecificConfig(
        network = NetworkDescriptor.fallback(),
        encryptDns = true,
        useNetworkDns = false,
        dnsChoice = DnsDataSource.cloudflare.id,
        useBlockaDnsInPlusMode = true,
        forceLibreMode = false,
        enabled = false,
        createdAt = now()
    )

}

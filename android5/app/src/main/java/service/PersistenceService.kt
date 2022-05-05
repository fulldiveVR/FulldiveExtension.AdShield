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

import blocka.LegacyAccountImport
import model.*
import repository.PackMigration
import tunnel.LegacyAdsCounterImport
import tunnel.LegacyBlocklistImport
import ui.ActivationViewModel
import utils.cause
import utils.Logger
import kotlin.reflect.KClass

object PersistenceService {

    private val log = Logger("Persistence")

    private val json = JsonSerializationService
    private val newline = NewlineSerializationService

    private val prefs = SharedPreferencesStorageService
    private val file = FileStorageService

    const val KEY_STATS = "stats"
    const val KEY_PACKS = "packs"
    const val KEY_BLOCKA_CONFIG = "blockaConfig"
    const val KEY_LOCAL_CONFIG = "localConfig"
    const val KEY_SYNCABLE_CONFIG = "syncableConfig"
    const val KEY_DNS = "dns"
    const val KEY_ACTIVATION_STATE = "activationState"
    const val KEY_ACCOUNT = "account"
    const val KEY_ADS_COUNTER = "adsCounter"
    const val KEY_BY_PASSED_APPS = "bypassedApps"
    const val KEY_BLOCKA_REPO_CONFIG = "blockaRepoConfig"
    const val KEY_BLOCKA_REPO_UPDATE = "blockaRepoUpdate"
    const val KEY_BLOCKA_REPO_PAYLOAD = "blockaRepoPayload"
    const val KEY_BLOCKA_AFTER_UPDATE = "blockaAfterUpdate"
    const val KEY_NETWORK_SPECIFIC_CONFIGS = "networkSpecificConfigs"

    fun save(obj: Any) {
        try {
            when (obj) {
                is Denied -> file.save(
                    key = BlocklistService.USER_DENIED,
                    data = newline.serialize(obj)
                )
                is Allowed -> file.save(
                    key = BlocklistService.USER_ALLOWED,
                    data = newline.serialize(obj)
                )
                else -> prefs.save(getPrefsKey(obj::class), json.serialize(obj))
            }
        } catch (ex: Exception) {
            log.e("Could not save persistence, ignoring".cause(ex))
        }
    }

    fun <T : Any> load(type: KClass<T>): T {
        try {
            val (string, deserializer) = when (type) {
                Denied::class -> {
                    val legacy = LegacyBlocklistImport.importLegacyBlocklistUserDenied()
                    if (legacy != null) {
                        save(Denied(legacy)) // To save in the current format
                        legacy.joinToString("\n") to newline
                    } else file.load(key = BlocklistService.USER_DENIED) to newline
                }
                Allowed::class -> {
                    val legacy = LegacyBlocklistImport.importLegacyBlocklistUserAllowed()
                    if (legacy != null) {
                        save(Allowed(legacy)) // To save in the current format
                        legacy.joinToString("\n") to newline
                    } else file.load(key = BlocklistService.USER_ALLOWED) to newline
                }
                Account::class -> {
                    val legacy = LegacyAccountImport.importLegacyAccount()
                    if (legacy != null) {
                        save(legacy) // To save in the current format
                        legacy to PassthroughSerializationService
                    } else prefs.load(getPrefsKey(type)) to json
                }
                AdsCounter::class -> {
                    val legacy = LegacyAdsCounterImport.importLegacyCounter()
                    if (legacy != null) {
                        save(legacy) // To save in the current format
                        legacy to PassthroughSerializationService
                    } else prefs.load(getPrefsKey(type)) to json
                }
                else -> prefs.load(getPrefsKey(type)) to json
            }
            if (string != null) {
                val deserialized = deserializer.deserialize(string, type)
                return when (type) {
                    Packs::class -> {
                        val (packs, migrated) = PackMigration.migrate(deserialized as Packs)
                        if (migrated) save(packs)
                        packs as T
                    }
                    else -> deserialized
                }
            }
            log.v("No persistence, using defaults for: $type")
            return getDefault(type)
        } catch (ex: Exception) {
            log.e("Could not load persistence, restoring defaults for: $type: ${ex.message}")
            return getDefault(type)
        }
    }

    private fun getPrefsKey(type: KClass<*>) = when (type) {
        StatsPersisted::class -> KEY_STATS
        Packs::class -> KEY_PACKS
        BlockaConfig::class -> KEY_BLOCKA_CONFIG
        LocalConfig::class -> KEY_LOCAL_CONFIG
        SyncableConfig::class -> KEY_SYNCABLE_CONFIG
        DnsWrapper::class -> KEY_DNS
        ActivationViewModel.ActivationState::class -> KEY_ACTIVATION_STATE
        Account::class -> KEY_ACCOUNT
        AdsCounter::class -> KEY_ADS_COUNTER
        BypassedAppIds::class -> KEY_BY_PASSED_APPS
        BlockaRepoConfig::class -> KEY_BLOCKA_REPO_CONFIG
        BlockaRepoUpdate::class -> KEY_BLOCKA_REPO_UPDATE
        BlockaRepoPayload::class -> KEY_BLOCKA_REPO_PAYLOAD
        BlockaAfterUpdate::class -> KEY_BLOCKA_AFTER_UPDATE
        NetworkSpecificConfigs::class -> KEY_NETWORK_SPECIFIC_CONFIGS
        else -> throw BlokadaException("Unsupported type for persistence: $type")
    }

    private fun <T : Any> getDefault(type: KClass<T>) = when (type) {
        StatsPersisted::class -> Defaults.stats() as T
        Allowed::class -> Defaults.allowed() as T
        Denied::class -> Defaults.denied() as T
        Packs::class -> Defaults.packs() as T
        BlockaConfig::class -> Defaults.blockaConfig() as T
        LocalConfig::class -> Defaults.localConfig() as T
        SyncableConfig::class -> Defaults.syncableConfig() as T
        DnsWrapper::class -> Defaults.dnsWrapper() as T
        ActivationViewModel.ActivationState::class -> ActivationViewModel.ActivationState.INACTIVE as T
        Account::class -> throw NoPersistedAccount()
        AdsCounter::class -> Defaults.adsCounter() as T
        BypassedAppIds::class -> Defaults.bypassedAppIds() as T
        BlockaRepoConfig::class -> Defaults.blockaRepoConfig() as T
        BlockaRepoUpdate::class -> Defaults.noSeenUpdate() as T
        BlockaRepoPayload::class -> Defaults.noPayload() as T
        BlockaAfterUpdate::class -> Defaults.noAfterUpdate() as T
        NetworkSpecificConfigs::class -> Defaults.noNetworkSpecificConfigs() as T
        else -> throw BlokadaException("No default for persisted type: $type")
    }
}
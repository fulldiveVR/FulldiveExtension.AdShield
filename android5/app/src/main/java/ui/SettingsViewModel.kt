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

package ui

import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.*
import service.PersistenceService
import utils.Logger
import utils.cause

class SettingsViewModel : ViewModel() {

    private val log = Logger("Settings")
    private val persistence = PersistenceService

    private val _dnsEntries = MutableLiveData<DnsWrapper>()
    val dnsEntries: LiveData<List<Pair<DnsId, Dns>>> = _dnsEntries.map { entry ->
        entry.value.map { it.id to it }
    }

    private val _localConfig = MutableLiveData<LocalConfig>()
    val localConfig = _localConfig.distinctUntilChanged()
    val selectedDns = localConfig.map { it.dnsChoice }

    private val _syncableConfig = MutableLiveData<SyncableConfig>()
    val syncableConfig = _syncableConfig

    init {
        _localConfig.value = persistence.load(LocalConfig::class)
        _syncableConfig.value = persistence.load(SyncableConfig::class)
        _dnsEntries.value = persistence.load(DnsWrapper::class)
        log.v("Config: ${_localConfig.value}")
    }

    fun setUseBlockaDnsInPlusMode(use: Boolean) {
        _localConfig.value?.let { current ->
            viewModelScope.launch {
                try {
                    log.v("Changing use Blocka DNS in Plus mode: $use")
                    val new = current.copy(useBlockaDnsInPlusMode = use)
//                    engine.changeDns(getCurrentDns(), dnsForPlusMode = decideDnsForPlusMode(useBlockaDnsInPlusMode = use))
                    persistence.save(new)
                    _localConfig.value = new
                } catch (ex: Exception) {
                    log.e("Failed changing setting".cause(ex))

                    // Notify the listener to reset its value to what it was
                    viewModelScope.launch {
                        delay(1000)
                        _localConfig.value = current
                    }
                }
            }
        }
    }

    fun setFirstTimeSeen() {
        log.v("Marking first time as seen")
        _syncableConfig.value?.let { current ->
            viewModelScope.launch {
                val new = current.copy(notFirstRun = true)
                persistence.save(new)
                _syncableConfig.value = new
            }
        }
    }

    fun setRatedApp() {
        log.v("Marking app as rated")
        _syncableConfig.value?.let { current ->
            viewModelScope.launch {
                val new = current.copy(rated = true)
                persistence.save(new)
                _syncableConfig.value = new
            }
        }
    }

    fun getUseChromeTabs(): Boolean {
        return _localConfig.value?.useChromeTabs ?: false
    }

    fun setUseChromeTabs(use: Boolean) {
        log.v("Switching the use of Chrome Tabs: $use")
        _localConfig.value?.let { current ->
            viewModelScope.launch {
                val new = current.copy(useChromeTabs = use)
                persistence.save(new)
                _localConfig.value = new
            }
        }
    }



    fun getLocale(): String? {
        return _localConfig.value?.locale
    }

    fun setLocale(locale: String?) {
        log.v("Setting locale: $locale")
        _localConfig.value?.let { current ->
            viewModelScope.launch {
                val new = current.copy(locale = locale)
                persistence.save(new)
                _localConfig.value = new
            }
        }
    }

    fun setUseBackup(backup: Boolean) {
        log.v("Setting use cloud backup: $backup")
        _localConfig.value?.let { current ->
            viewModelScope.launch {
                val new = current.copy(backup = backup)
                persistence.save(new)
                _localConfig.value = new
            }
        }
    }

    fun setEscaped(escaped: Boolean) {
        log.v("Setting escaped: $escaped")
        _localConfig.value?.let { current ->
            viewModelScope.launch {
                val new = current.copy(escaped = escaped)
                persistence.save(new)
                _localConfig.value = new
            }
        }
    }

    fun setUseForegroundService(use: Boolean) {
        log.v("Setting use Foreground Service: $use")
        _localConfig.value?.let { current ->
            viewModelScope.launch {
                val new = current.copy(useForegroundService = use)
                persistence.save(new)
                _localConfig.value = new
            }
        }
    }

    fun getUseForegroundService(): Boolean {
        return _localConfig.value?.useForegroundService ?: false
    }
}
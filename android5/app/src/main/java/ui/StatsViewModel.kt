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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import engine.EngineService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import model.*
import service.PersistenceService
import service.RemoteConfigService
import service.StatsService
import ui.advanced.packs.PacksViewModel
import utils.Logger
import utils.cause

class StatsViewModel : ViewModel() {

    enum class Sorting {
        RECENT, TOP
    }

    enum class Filter {
        ALL, BLOCKED, ALLOWED
    }

    private val log = Logger("Stats")
    private val persistence = PersistenceService
    private val engine = EngineService
    private val statistics = StatsService

    private var sorting = Sorting.RECENT
    private var filter = Filter.ALL
    private var packsFilter = PacksViewModel.Filter.HIGHLIGHTS
    private var searchTerm: String? = null

    private val _stats = MutableLiveData<Stats>()
    val stats: LiveData<Stats> = _stats.distinctUntilChanged()
    val history = _stats.map {
        applyFilters(it.entries)
    }

    private val _allowed = MutableLiveData<Allowed>()
    val allowed = _allowed.map { it.value }

    private val _denied = MutableLiveData<Denied>()
    val denied = _denied.map { it.value }

    private val _packs = MutableLiveData<Packs>()
    val packs = _packs.map { applyPacksFilters(it.packs) }

    private val _customBlocklistConfig = MutableLiveData<CustomBlocklistConfig>()
    val customBlocklistConfig = _customBlocklistConfig

    private var currentCustomBlocklistConfig = CustomBlocklistConfig.emptyConfig

    private val activeTags = listOf("official")

    init {
        viewModelScope.launch {
            _allowed.value = persistence.load(Allowed::class)
            _denied.value = persistence.load(Denied::class)
            _packs.value = persistence.load(Packs::class)
            _customBlocklistConfig.value = getCustomBlocklistConfig()
            currentCustomBlocklistConfig = getCustomBlocklistConfig()
            statistics.setup()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                _stats.value = statistics.getStats()
                _allowed.value = _allowed.value
                _denied.value = _denied.value
                _customBlocklistConfig.value = getCustomBlocklistConfig()
            } catch (ex: Exception) {
                log.e("Could not load stats".cause(ex))
            }
        }
    }

    fun clear() {
        statistics.clear()
        refresh()
    }

    fun get(forName: String): HistoryEntry? {
        return history.value?.firstOrNull { it.name == forName }
    }

    fun getFilter() = filter
    fun getSorting() = sorting

    fun filter(filter: Filter) {
        this.filter = filter
        updateLiveData()
    }

    fun sort(sort: Sorting) {
        this.sorting = sort
        updateLiveData()
    }

    fun search(search: String?) {
        this.searchTerm = search
        updateLiveData()
    }

    fun allow(name: String) {
        _allowed.value?.let { current ->
            viewModelScope.launch {
                try {
                    customBlocklistConfig.value?.let { currentCustomBlocklistConfig = it }
                    val new = current.allow(name)
                    persistence.save(new)
                    _customBlocklistConfig.value = getCustomBlocklistConfig()
                    _allowed.value = new
                    updateLiveData()
                    engine.reloadBlockLists(
                        getActiveUrls(),
                        getCustomBlocklistConfig(),
                        currentCustomBlocklistConfig
                    )
                } catch (ex: Exception) {
                    log.e("Could not allow host $name".cause(ex))
                    persistence.save(current)
                }
            }
        }
    }

    fun unallow(name: String) {
        _allowed.value?.let { current ->
            viewModelScope.launch {
                try {
                    customBlocklistConfig.value?.let { currentCustomBlocklistConfig = it }
                    val new = current.unallow(name)
                    persistence.save(new)
                    _customBlocklistConfig.value = getCustomBlocklistConfig()
                    _allowed.value = new
                    updateLiveData()
                    engine.reloadBlockLists(
                        getActiveUrls(),
                        getCustomBlocklistConfig(),
                        currentCustomBlocklistConfig
                    )
                } catch (ex: Exception) {
                    log.e("Could not unallow host $name".cause(ex))
                    persistence.save(current)
                }
            }
        }
    }

    fun deny(name: String) {
        _denied.value?.let { current ->
            viewModelScope.launch {
                try {
                    customBlocklistConfig.value?.let { currentCustomBlocklistConfig = it }
                    val new = current.deny(name)
                    persistence.save(new)
                    _customBlocklistConfig.value = getCustomBlocklistConfig()
                    _denied.value = new
                    updateLiveData()
                    engine.reloadBlockLists(
                        getActiveUrls(),
                        getCustomBlocklistConfig(),
                        currentCustomBlocklistConfig
                    )
                } catch (ex: Exception) {
                    log.e("Could not deny host $name".cause(ex))
                    persistence.save(current)
                }
            }
        }
    }

    fun undeny(name: String) {
        _denied.value?.let { current ->
            viewModelScope.launch {
                try {
                    customBlocklistConfig.value?.let { currentCustomBlocklistConfig = it }
                    val new = current.undeny(name)
                    persistence.save(new)
                    _customBlocklistConfig.value = getCustomBlocklistConfig()
                    _denied.value = new
                    updateLiveData()
                    engine.reloadBlockLists(
                        getActiveUrls(),
                        getCustomBlocklistConfig(),
                        currentCustomBlocklistConfig
                    )
                } catch (ex: Exception) {
                    log.e("Could not undeny host $name".cause(ex))
                    persistence.save(current)
                }
            }
        }
    }

    fun isAllowed(name: String): Boolean {
        return _allowed.value?.value?.contains(name) ?: false
    }

    fun isDenied(name: String): Boolean {
        return _denied.value?.value?.contains(name) ?: false
    }

    fun getActiveUrls(): Set<String> {
        // Also include urls of any active pack
        return _packs.value?.let { packs ->
            packs.packs.filter { it.status.installed }
                .flatMap { it.getUrls(PackFilterType.WildcardsOnly) }.toSet()
        } ?: emptySet()
    }

    fun getCustomBlocklistConfig(): CustomBlocklistConfig {
        return CustomBlocklistConfig(
            persistence.load(Allowed::class).value,
            persistence.load(Denied::class).value
        )
    }

    fun getCurrentCustomBlicklistConfig(): CustomBlocklistConfig {
        return currentCustomBlocklistConfig
    }

    fun onConfigUpdate(jsonConfig: String) {
        customBlocklistConfig.value?.let { currentCustomBlocklistConfig = it }
        val type = object : TypeToken<CustomBlocklistConfig>() {}.type
        val config: CustomBlocklistConfig = Gson().fromJson(jsonConfig, type)
        persistence.save(Allowed(config.isAllowed))
        persistence.save(
            Denied(
                config.isDenied.toSet()
                    .plus(RemoteConfigService.getAdblockWorkCheckDomain())
                    .toList()
            )
        )

        viewModelScope.launch(Dispatchers.Main) {
            engine.reloadBlockLists(
                getActiveUrls(),
                getCustomBlocklistConfig(),
                currentCustomBlocklistConfig
            )
            // This will cause to emit new event and to refresh the public LiveData
            _allowed.value = Allowed(config.isAllowed)
            _denied.value = Denied(config.isDenied)
            _customBlocklistConfig.value = getCustomBlocklistConfig()
        }
    }

    fun updateBlockedDomains() {
        val t = getCustomBlocklistConfig()
        val config = t.copy(
            isDenied = t.isDenied
                .toSet()
                .plus(RemoteConfigService.getAdblockWorkCheckDomain())
                .toList()
        )
        persistence.save(Allowed(config.isAllowed))
        persistence.save(Denied(config.isDenied))
        viewModelScope.launch(Dispatchers.Main) {
            engine.reloadBlockLists(
                getActiveUrls(),
                config,
                getCustomBlocklistConfig()
            )
            // This will cause to emit new event and to refresh the public LiveData
            _allowed.value = Allowed(getCustomBlocklistConfig().isAllowed)
            _denied.value = Denied(getCustomBlocklistConfig().isDenied)
            _customBlocklistConfig.value = getCustomBlocklistConfig()
        }
    }

    private fun updateLiveData() {
        viewModelScope.launch {
            // This will cause to emit new event and to refresh the public LiveData
            _stats.value = _stats.value
        }
    }

    private fun applyFilters(history: List<HistoryEntry>): List<HistoryEntry> {
        var entries = history

        // Apply search term
        searchTerm?.run {
            entries = history.filter { it.name.contains(this, ignoreCase = true) }
        }

        // Apply filtering
        when (filter) {
            Filter.BLOCKED -> {
                // Show blocked and denied hosts only
                entries =
                    entries.filter { it.type == HistoryEntryType.blocked || it.type == HistoryEntryType.blocked_denied }
            }
            Filter.ALLOWED -> {
                // Show allowed and bypassed hosts only
                entries =
                    entries.filter { it.type != HistoryEntryType.blocked && it.type != HistoryEntryType.blocked_denied }
            }
            else -> {}
        }

        // Apply sorting
        return when (sorting) {
            Sorting.TOP -> {
                // Sorted by the number of requests
                entries.sortedByDescending { it.requests }
            }
            Sorting.RECENT -> {
                // Sorted by recent
                entries.sortedByDescending { it.time }
            }
        }
    }

    private fun applyPacksFilters(allPacks: List<Pack>): List<Pack> {
        return when (packsFilter) {
            PacksViewModel.Filter.ACTIVE -> {
                allPacks.filter { pack ->
                    pack.status.installed
                }
            }
            PacksViewModel.Filter.ALL -> {
                allPacks.filter { pack ->
                    !activeTags.intersect(pack.tags).isEmpty()
                }
            }
            else -> {
                allPacks.filter { pack ->
                    pack.tags.contains(Pack.recommended) /* && !pack.status.installed */
                }
            }
        }
    }

}
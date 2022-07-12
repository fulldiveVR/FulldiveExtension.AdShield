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

package engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import model.CustomBlocklistConfig
import service.BlocklistService
import service.EnvironmentService
import service.StatsService
import utils.Logger

internal object FilteringService {

    private val log = Logger("Filtering")
    private val blocklist = BlocklistService
    private val stats = StatsService
    private val scope = GlobalScope

    private var merged = emptyList<Host>()
    private var userAllowed = emptyList<Host>()
    private var userDenied = emptyList<Host>()

    private var filteringStrategy: FilteringStrategy = NoopFilteringStrategy

    fun reload(urls: Set<String>, config: CustomBlocklistConfig, currentConfig: CustomBlocklistConfig) {
        // init adp if have adp blocklist.
        log.v("Reloading blocklist")
        merged = blocklist.loadMerged()
        userAllowed = blocklist.loadUserAllowed()
        userDenied = blocklist.loadUserDenied()
        log.v("Reloaded: ${merged.size} hosts, + user: ${userDenied.size} denied, ${userAllowed.size} allowed")
        if (merged.size < 10000) {
            log.w("Merged blocklist is suspiciously small, may not block sufficiently")
        }

        filteringStrategy = when {
            EnvironmentService.isSlim() -> {
                log.w("Slim build, will not block anything")
                NoopFilteringStrategy
            }
            merged.isEmpty() && userDenied.isEmpty() -> {
                log.e("Empty merged blocklist and user denied list, will not block anything")
                NoopFilteringStrategy
            }
            else -> {
                SimpleFilteringStrategy(merged)
            }
        }

        ABPService.setAdblockSubscriptions(urls)
        ABPService.updateCustomBlocklists(config, currentConfig)
    }

    fun allowed(host: Host): Boolean {
        return if (userAllowed.contains(host)) {
            scope.launch(Dispatchers.Main) { stats.passedAllowed(host) }
            true
        } else false
    }

    fun denied(host: Host): Boolean {
        return if (userDenied.contains(host)) {
            scope.launch(Dispatchers.Main) { stats.blockedDenied(host) }
            true
        } else if (filteringStrategy.denied(host)) {
            scope.launch(Dispatchers.Main) { stats.blocked(host) }
            true
        } else {
            scope.launch(Dispatchers.Main) { stats.passed(host) }
            false
        }
    }

}

typealias Host = String

private interface FilteringStrategy {
    fun denied(host: Host): Boolean
}

private class SimpleFilteringStrategy(
    private val merged: List<Host>,
) : FilteringStrategy {

    override fun denied(host: Host): Boolean {
        return merged.contains(host) || ABPService.isBlocked(host)
    }
}

private object NoopFilteringStrategy : FilteringStrategy {
    override fun denied(host: Host) = false
}
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

package utils

import service.EnvironmentService
import java.net.URLEncoder

object Links {
    private const val FULLDIVE_URL = "https://fulldive.com"

    const val intro = FULLDIVE_URL
    const val whyUpgrade = FULLDIVE_URL
    const val whatIsDns = FULLDIVE_URL
    const val whyVpnPerms = FULLDIVE_URL
    const val howToRestore = FULLDIVE_URL
    const val tunnelFailure = FULLDIVE_URL
    const val startOnBoot = FULLDIVE_URL

    const val kb = FULLDIVE_URL
    const val discordInvite = "https://discord.com/invite/3f79ygxZ3y"
    const val donate = FULLDIVE_URL
    const val privacy = "https://fulldive.com/privacy-policy"
    const val terms = FULLDIVE_URL
    const val credits = FULLDIVE_URL
    const val community = FULLDIVE_URL
    const val idoAnnouncement = "https://www.fulldive.com/fulladshield"
    const val dnsSettings = "https://static.fdvr.co/dns-settings/index.html"
    const val appsSettings = "https://static.fdvr.co/app-settings/index.html"

    val updated = if (EnvironmentService.isSlim()) FULLDIVE_URL else FULLDIVE_URL

    fun manageSubscriptions(accountId: String) =
        if (EnvironmentService.isSlim()) support(accountId)
        else "https://app.blokada.org/activate/$accountId"

    fun support(accountId: String) =
        "https://app.blokada.org/support?account-id=$accountId" +
                "&user-agent=${URLEncoder.encode(EnvironmentService.getUserAgent())}"

    fun isSubscriptionLink(link: String) = link.startsWith(FULLDIVE_URL)

    fun isDiscordLink(link: String) = link.startsWith(discordInvite)
}

/*
 * This file is part of Blokada.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright © 2021 Blocka AB. All rights reserved.
 *
 * @author Karol Gusak (karol@blocka.net)
 */

package utils

import service.EnvironmentService
import java.net.URLEncoder

object Links {

    val intro = "https://fulldive.com"
    val whyUpgrade = "https://fulldive.com"
    val whatIsDns = "https://fulldive.com"
    val whyVpnPerms = "https://fulldive.com"
    val howToRestore = "https://fulldive.com"
    val tunnelFailure = "https://fulldive.com"
    val startOnBoot = "https://fulldive.com"

    val kb = "https://fulldive.com"
    val donate = "https://fulldive.com"
    val privacy = "https://fulldive.com"
    val terms = "https://fulldive.com"
    val credits = "https://fulldive.com"
    val community = "https://fulldive.com"

    val updated =
        if (EnvironmentService.isSlim()) "https://fulldive.com"
        else "https://fulldive.com"

    fun manageSubscriptions(accountId: String) =
        if (EnvironmentService.isSlim()) support(accountId)
        else "https://app.blokada.org/activate/$accountId"

    fun support(accountId: String) =
        "https://app.blokada.org/support?account-id=$accountId" +
        "&user-agent=${URLEncoder.encode(EnvironmentService.getUserAgent())}"

    fun isSubscriptionLink(link: String) = link.startsWith("https://fulldive.com")

}

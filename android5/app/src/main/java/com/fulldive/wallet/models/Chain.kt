/*
 * Copyright (c) 2022 FullDive
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.fulldive.wallet.models

import org.adshield.BuildConfig

object Chain {
    const val chainName = "imversed-canary"
    const val chainAddressPrefix = "imv"
    const val mainDenom = "aimv"
    const val fdCoinDenom = "FDToken"
    const val fullNameCoin = "Imversed Staking Coin"
    const val symbolTitle = "IMV"
    const val fdCoinSymbolTitle = "FD Coin"
    const val divideDecimal = 18
    const val displayDecimal = 18
    val grpcApiHost = ApiHost.from(BuildConfig.GRPC_API_HOST)
}
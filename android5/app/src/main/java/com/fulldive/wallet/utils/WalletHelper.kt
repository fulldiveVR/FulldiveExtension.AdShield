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

package com.fulldive.wallet.utils

import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

object WalletHelper {
    fun getReadableBalance(
        input: BigDecimal,
        divideDecimal: Int,
        displayDecimal: Int
    ): SpannableString {
        val amount = input
            .movePointLeft(divideDecimal)
            .setScale(displayDecimal, BigDecimal.ROUND_DOWN)
        val result = SpannableString(
            getDecimalFormat(displayDecimal).format(amount)
        )
        result.setSpan(
            RelativeSizeSpan(0.8f),
            result.length - displayDecimal,
            result.length,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
        return result
    }

    fun getDecimalFormat(count: Int): DecimalFormat {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        val decimalformat = formatter as DecimalFormat
        decimalformat.roundingMode = RoundingMode.DOWN
        val stringBuilder = StringBuilder("###,###,###,###,###,###,###,##0")
        if (count > 0) {
            if (count <= 18) {
                stringBuilder.append('.')
                for (i in 0 until count) {
                    stringBuilder.append('0')
                }
            } else {
                stringBuilder.append(".000000")
            }
        }
        decimalformat.applyLocalizedPattern(stringBuilder.toString())
        return decimalformat
    }
}
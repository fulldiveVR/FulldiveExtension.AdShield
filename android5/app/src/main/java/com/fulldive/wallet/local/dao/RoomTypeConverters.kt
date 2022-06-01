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

package com.fulldive.wallet.local.dao

import androidx.room.TypeConverter
import com.fulldive.wallet.models.ExchangeValue
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

@Suppress("unused")
class RoomTypeConverters {

    private val gson = GsonBuilder().create()

    @TypeConverter
    fun listToExchangeValuesToString(obj: List<ExchangeValue>?): String? = gson.toJson(obj)

    @TypeConverter
    fun stringToExchangeValues(data: String?): List<ExchangeValue>? {
        return gson.fromJson(data, object : TypeToken<List<ExchangeValue>>() {}.type)
    }
}

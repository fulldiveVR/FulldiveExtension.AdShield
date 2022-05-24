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

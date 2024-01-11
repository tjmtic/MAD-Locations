package com.abyxcz.data.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken

class ListTypeConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromJsonArray(value: JsonArray): String {
            return Gson().toJson(value)
        }

        @TypeConverter
        @JvmStatic
        fun toJsonArray(value: String): JsonArray {
            return Gson().fromJson(value, object : TypeToken<JsonArray?>() {}.type)
        }
    }
}
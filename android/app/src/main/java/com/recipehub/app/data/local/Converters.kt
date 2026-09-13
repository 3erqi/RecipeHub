package com.recipehub.app.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun listToJson(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun jsonToList(value: String): List<String> = Json.decodeFromString(value)
}

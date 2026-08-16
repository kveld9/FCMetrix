package com.kveld9.fcmetrix.data.local.converter

import androidx.room.TypeConverter
import com.kveld9.fcmetrix.ui.model.PlayerData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LineupConverters {
    @TypeConverter
    fun fromPlayerDataList(value: List<PlayerData>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toPlayerDataList(value: String): List<PlayerData> {
        return Json.decodeFromString(value)
    }
}

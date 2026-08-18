package com.mlopes.gestor.data.local.db

import androidx.room.TypeConverter

/**
 * Conversores de tipo. Mantem a interface minimal: as entidades guardam
 * String em tudo (datas em ISO 8601, listas em CSV, JSON como string).
 */
class Converters {
    @TypeConverter
    fun stringToInt(value: String?): Int? = value?.toIntOrNull()

    @TypeConverter
    fun intToString(value: Int?): String? = value?.toString()
}

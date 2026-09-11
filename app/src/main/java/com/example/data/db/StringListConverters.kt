package com.example.data.db

import androidx.room.TypeConverter

/**
 * Room TypeConverter to persist and retrieve lists of strings (e.g. imagePaths)
 * for artisan catalog entries.
 */
class StringListConverters {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        val trimmed = value.trim()
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            return trimmed.removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
                .filter { it.isNotBlank() }
        }
        if (trimmed.contains("|||")) {
            return trimmed.split("|||").map { it.trim() }.filter { it.isNotBlank() }
        }
        return trimmed.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    @TypeConverter
    fun toString(list: List<String>?): String {
        if (list.isNullOrEmpty()) return ""
        return list.filter { it.isNotBlank() }.joinToString("|||")
    }
}

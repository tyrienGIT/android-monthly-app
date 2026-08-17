package com.maimonthlyhoppinings.data

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

data class BackupEventType(
    val id: String,
    val label: String,
    val color: String,
)

data class BackupEvent(
    val id: Long,
    val typeId: String,
    val title: String,
    val details: String,
    val emoji: String = "",
    val startDateEpochDay: Long,
    val endDateEpochDay: Long,
    val createdAtMillis: Long,
)

data class BackupEntry(
    val id: Long,
    val eventId: Long,
    val title: String,
    val emoji: String = "",
    val dateEpochDay: Long,
    val startTimeMinutesOfDay: Int?,
    val details: String,
    val intensity: Int,
    val createdAtMillis: Long,
)

data class BackupCustomTheme(
    val id: Long,
    val name: String,
    val lightPrimaryArgb: Int,
    val lightSecondaryArgb: Int,
    val lightTertiaryArgb: Int,
    val darkPrimaryArgb: Int,
    val darkSecondaryArgb: Int,
    val darkTertiaryArgb: Int,
    val createdAtMillis: Long,
)

data class BackupPreferences(
    val themeMode: String,
    val activeColorTheme: String,
)

data class BackupFile(
    val format: String = FORMAT,
    val formatVersion: Int = FORMAT_VERSION,
    val exportedAtMillis: Long,
    val eventTypes: List<BackupEventType>,
    val events: List<BackupEvent>,
    val entries: List<BackupEntry>,
    val customThemes: List<BackupCustomTheme>,
    val preferences: BackupPreferences?,
) {
    fun stringify(): String {
        val root = JSONObject()
        root.put("format", format)
        root.put("formatVersion", formatVersion)
        root.put("exportedAtMillis", exportedAtMillis)
        root.put("eventTypes", JSONArray().also { array ->
            eventTypes.forEach { type ->
                array.put(
                    JSONObject()
                        .put("id", type.id)
                        .put("label", type.label)
                        .put("color", type.color),
                )
            }
        })
        root.put("events", JSONArray().also { array ->
            events.forEach { event ->
                array.put(
                    JSONObject()
                        .put("id", event.id)
                        .put("typeId", event.typeId)
                        .put("title", event.title)
                        .put("details", event.details)
                        .put("emoji", event.emoji)
                        .put("startDateEpochDay", event.startDateEpochDay)
                        .put("endDateEpochDay", event.endDateEpochDay)
                        .put("createdAtMillis", event.createdAtMillis),
                )
            }
        })
        root.put("entries", JSONArray().also { array ->
            entries.forEach { entry ->
                val obj = JSONObject()
                    .put("id", entry.id)
                    .put("eventId", entry.eventId)
                    .put("title", entry.title)
                    .put("emoji", entry.emoji)
                    .put("dateEpochDay", entry.dateEpochDay)
                    .put("details", entry.details)
                    .put("intensity", entry.intensity)
                    .put("createdAtMillis", entry.createdAtMillis)
                if (entry.startTimeMinutesOfDay == null) {
                    obj.put("startTimeMinutesOfDay", JSONObject.NULL)
                } else {
                    obj.put("startTimeMinutesOfDay", entry.startTimeMinutesOfDay)
                }
                array.put(obj)
            }
        })
        root.put("customThemes", JSONArray().also { array ->
            customThemes.forEach { theme ->
                array.put(
                    JSONObject()
                        .put("id", theme.id)
                        .put("name", theme.name)
                        .put("lightPrimaryArgb", theme.lightPrimaryArgb)
                        .put("lightSecondaryArgb", theme.lightSecondaryArgb)
                        .put("lightTertiaryArgb", theme.lightTertiaryArgb)
                        .put("darkPrimaryArgb", theme.darkPrimaryArgb)
                        .put("darkSecondaryArgb", theme.darkSecondaryArgb)
                        .put("darkTertiaryArgb", theme.darkTertiaryArgb)
                        .put("createdAtMillis", theme.createdAtMillis),
                )
            }
        })
        if (preferences != null) {
            root.put(
                "preferences",
                JSONObject()
                    .put("themeMode", preferences.themeMode)
                    .put("activeColorTheme", preferences.activeColorTheme),
            )
        }
        return root.toString(2)
    }

    companion object {
        const val FORMAT = "mai-monthly-hoppinings-backup"
        const val FORMAT_VERSION = 1

        fun parse(json: String): BackupFile {
            val root = try {
                JSONObject(json)
            } catch (e: JSONException) {
                throw IllegalArgumentException("Invalid backup file", e)
            }
            val format = root.optString("format")
            if (format != FORMAT) {
                throw IllegalArgumentException("Unknown backup format")
            }
            val formatVersion = root.optInt("formatVersion", -1)
            if (formatVersion != FORMAT_VERSION) {
                throw IllegalArgumentException("Unsupported backup format version")
            }
            return try {
                BackupFile(
                    format = format,
                    formatVersion = formatVersion,
                    exportedAtMillis = root.optLong("exportedAtMillis", 0L),
                    eventTypes = root.objectArray("eventTypes").map { obj ->
                        BackupEventType(
                            id = obj.getString("id"),
                            label = obj.optionalString("label"),
                            color = obj.optionalString("color"),
                        )
                    },
                    events = root.objectArray("events").map { obj ->
                        BackupEvent(
                            id = obj.getLong("id"),
                            typeId = obj.getString("typeId"),
                            title = obj.optionalString("title"),
                            details = obj.optionalString("details"),
                            emoji = obj.optionalString("emoji"),
                            startDateEpochDay = obj.getLong("startDateEpochDay"),
                            endDateEpochDay = obj.getLong("endDateEpochDay"),
                            createdAtMillis = obj.optLong("createdAtMillis", 0L),
                        )
                    },
                    entries = root.objectArray("entries").map { obj ->
                        BackupEntry(
                            id = obj.getLong("id"),
                            eventId = obj.getLong("eventId"),
                            title = obj.optionalString("title"),
                            emoji = obj.optionalString("emoji"),
                            dateEpochDay = obj.getLong("dateEpochDay"),
                            startTimeMinutesOfDay = obj.optionalInt("startTimeMinutesOfDay"),
                            details = obj.optionalString("details"),
                            intensity = obj.optInt("intensity", 5),
                            createdAtMillis = obj.optLong("createdAtMillis", 0L),
                        )
                    },
                    customThemes = root.objectArray("customThemes").map { obj ->
                        BackupCustomTheme(
                            id = obj.getLong("id"),
                            name = obj.optionalString("name"),
                            lightPrimaryArgb = obj.getLong("lightPrimaryArgb").toInt(),
                            lightSecondaryArgb = obj.getLong("lightSecondaryArgb").toInt(),
                            lightTertiaryArgb = obj.getLong("lightTertiaryArgb").toInt(),
                            darkPrimaryArgb = obj.getLong("darkPrimaryArgb").toInt(),
                            darkSecondaryArgb = obj.getLong("darkSecondaryArgb").toInt(),
                            darkTertiaryArgb = obj.getLong("darkTertiaryArgb").toInt(),
                            createdAtMillis = obj.optLong("createdAtMillis", 0L),
                        )
                    },
                    preferences = root.optJSONObject("preferences")?.let { prefs ->
                        BackupPreferences(
                            themeMode = prefs.getString("themeMode"),
                            activeColorTheme = prefs.getString("activeColorTheme"),
                        )
                    },
                )
            } catch (e: JSONException) {
                throw IllegalArgumentException("Invalid backup file", e)
            }
        }
    }
}

private fun JSONObject.objectArray(key: String): List<JSONObject> {
    val array = optJSONArray(key) ?: return emptyList()
    return List(array.length()) { index -> array.getJSONObject(index) }
}

private fun JSONObject.optionalString(key: String, default: String = ""): String {
    if (!has(key) || isNull(key)) return default
    return getString(key)
}

private fun JSONObject.optionalInt(key: String): Int? {
    if (!has(key) || isNull(key)) return null
    return getInt(key)
}

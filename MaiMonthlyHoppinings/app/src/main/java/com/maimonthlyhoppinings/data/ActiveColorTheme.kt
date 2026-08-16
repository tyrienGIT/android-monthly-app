package com.maimonthlyhoppinings.data

/**
 * Active colour theme selection: a built-in preset or a user-saved custom theme.
 */
sealed class ActiveColorTheme {
    data class Preset(val theme: ColorTheme) : ActiveColorTheme()
    data class Custom(val themeId: Long) : ActiveColorTheme()

    fun encode(): String = when (this) {
        is Preset -> "PRESET:${theme.name}"
        is Custom -> "CUSTOM:$themeId"
    }

    companion object {
        val default: ActiveColorTheme = Preset(ColorTheme.default)

        fun decode(raw: String?): ActiveColorTheme {
            if (raw.isNullOrBlank()) return default
            val parts = raw.split(':', limit = 2)
            if (parts.size != 2) return default
            return when (parts[0]) {
                "PRESET" -> {
                    val theme = runCatching { ColorTheme.valueOf(parts[1]) }.getOrNull()
                    if (theme != null) Preset(theme) else default
                }
                "CUSTOM" -> {
                    val id = parts[1].toLongOrNull()
                    if (id != null && id > 0L) Custom(id) else default
                }
                else -> default
            }
        }
    }
}

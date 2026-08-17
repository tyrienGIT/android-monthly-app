package com.maimonthlyhoppinings.data

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    ;

    companion object {
        val default: ThemeMode = DARK
    }
}

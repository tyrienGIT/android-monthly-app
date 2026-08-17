package com.maimonthlyhoppinings.data

/**
 * Colour theme axis (independent of light/dark mode).
 * Each option supplies a balanced Material base palette.
 */
enum class ColorTheme(
    val displayName: String,
    val description: String,
) {
    COASTAL(
        displayName = "Coastal",
        description = "Sea teal with a soft coral accent",
    ),
    FOREST(
        displayName = "Forest",
        description = "Deep moss green with warm gold",
    ),
    SLATE(
        displayName = "Slate",
        description = "Cool blue-gray with sky accents",
    ),
    EMBER(
        displayName = "Ember",
        description = "Brick red balanced with stone taupe",
    ),
    INK(
        displayName = "Ink",
        description = "Navy ink with amber highlights",
    ),
    ORCHARD(
        displayName = "Orchard",
        description = "Muted plum with sage green",
    ),
    GROVE(
        displayName = "Grove",
        description = "Olive green with citrus zest",
    ),
    CANYON(
        displayName = "Canyon",
        description = "Sandstone brown with copper",
    ),
    GLACIER(
        displayName = "Glacier",
        description = "Icy cyan with crisp blue",
    ),
    ROSEWOOD(
        displayName = "Rosewood",
        description = "Dusty rose with charcoal green",
    ),
    ;

    companion object {
        val default: ColorTheme = SLATE
    }
}

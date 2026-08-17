package com.maimonthlyhoppinings.ui.tutorial

enum class TutorialScreen {
    Home,
    Calendar,
    Trends,
    Settings,
    EventDetail,
    StartEvent,
    EntryEditor,
    Data,
}

enum class TutorialSection {
    Full,
    Home,
    Calendar,
    Trends,
    EventDetail,
    StartEvent,
    EntryEditor,
    Settings,
    Data,
}

data class TutorialStep(
    val id: String,
    val screen: TutorialScreen,
    val targetId: String?,
    val title: String,
    val body: String,
)

object TutorialTargetIds {
    const val HOME_WELCOME = "home_welcome"
    const val HOME_FAB = "home_fab"
    const val HOME_CALENDAR = "home_calendar"
    const val HOME_TRENDS = "home_trends"
    const val HOME_SETTINGS = "home_settings"
    const val HOME_NAV_ICONS = "home_nav_icons"

    const val CALENDAR_HEAT = "calendar_heat"
    const val CALENDAR_PLUS = "calendar_plus"
    const val CALENDAR_DAY_PANE = "calendar_day_pane"

    const val TRENDS_CHIPS = "trends_chips"
    const val TRENDS_WAVE = "trends_wave"
    const val TRENDS_EVENTS = "trends_events"

    const val EVENT_ENTRIES = "event_entries"
    const val EVENT_ADD = "event_add"
    const val EVENT_HEATMAP = "event_heatmap"
    const val EVENT_SWIPE = "event_swipe"

    const val START_INTRO = "start_intro"
    const val START_CATEGORY_DATES = "start_category_dates"
    const val START_SPAN = "start_span"

    const val ENTRY_INTENSITY = "entry_intensity"
    const val ENTRY_TIME = "entry_time"
    const val ENTRY_DAY = "entry_day"

    const val SETTINGS_PREFS = "settings_prefs"
    const val SETTINGS_CATEGORIES = "settings_categories"
    const val SETTINGS_DATA = "settings_data"
    const val SETTINGS_VIEW_TUTORIAL = "settings_view_tutorial"

    const val DATA_LOCAL = "data_local"
    const val DATA_MERGE_REPLACE = "data_merge_replace"
}

object TutorialSteps {
    val fullTour: List<TutorialStep> = listOf(
        TutorialStep(
            id = "full_home_welcome",
            screen = TutorialScreen.Home,
            targetId = TutorialTargetIds.HOME_WELCOME,
            title = "Your personal tracker",
            body = "Mai Monthly Hoppinings stays on this device — no accounts or sync. An event is the parent (a category and date span). Dated entries live under it.",
        ),
        TutorialStep(
            id = "full_home_fab",
            screen = TutorialScreen.Home,
            targetId = TutorialTargetIds.HOME_FAB,
            title = "Start an event",
            body = "Use this to begin a new event. Pick a category and a date span, then add single-day entries under it.",
        ),
        TutorialStep(
            id = "full_home_calendar",
            screen = TutorialScreen.Home,
            targetId = TutorialTargetIds.HOME_CALENDAR,
            title = "Calendar",
            body = "Open the calendar to see each event as a heat band across its days.",
        ),
        TutorialStep(
            id = "full_calendar_heat",
            screen = TutorialScreen.Calendar,
            targetId = TutorialTargetIds.CALENDAR_HEAT,
            title = "One band per event",
            body = "Each event paints one heat band across its whole span — even days without an entry. Tap a day to inspect it.",
        ),
        TutorialStep(
            id = "full_calendar_plus",
            screen = TutorialScreen.Calendar,
            targetId = TutorialTargetIds.CALENDAR_PLUS,
            title = "Add from the calendar",
            body = "Plus opens Add new first, then events from the last year, newest first. Pick one to add an entry on the selected day.",
        ),
        TutorialStep(
            id = "full_trends_category",
            screen = TutorialScreen.Trends,
            targetId = TutorialTargetIds.TRENDS_CHIPS,
            title = "One category at a time",
            body = "Trends charts daily peak intensity for a single category. Switch chips to change category — the wave is days, not one dot per event.",
        ),
        TutorialStep(
            id = "full_trends_events",
            screen = TutorialScreen.Trends,
            targetId = TutorialTargetIds.TRENDS_EVENTS,
            title = "Zoom and inspect",
            body = "Tap an event to zoom the wave to that span. Drag the wave to inspect a day.",
        ),
        TutorialStep(
            id = "full_settings",
            screen = TutorialScreen.Settings,
            targetId = TutorialTargetIds.SETTINGS_PREFS,
            title = "Categories and data",
            body = "Rename types and change their colours here. Export and import a JSON backup that stays on this device.",
        ),
    )

    val home: List<TutorialStep> = listOf(
        TutorialStep(
            id = "home_welcome",
            screen = TutorialScreen.Home,
            targetId = TutorialTargetIds.HOME_WELCOME,
            title = "Your personal tracker",
            body = "This is an offline tracker. Events hold dated entries. Home lists every event you have started.",
        ),
        TutorialStep(
            id = "home_fab",
            screen = TutorialScreen.Home,
            targetId = TutorialTargetIds.HOME_FAB,
            title = "Start an event",
            body = "The plus starts a new event with a category and date span.",
        ),
        TutorialStep(
            id = "home_nav",
            screen = TutorialScreen.Home,
            targetId = TutorialTargetIds.HOME_NAV_ICONS,
            title = "Calendar, Trends, Settings",
            body = "Calendar shows heat across each event's span. Trends charts one category's daily intensity. Settings holds categories, appearance, and backup.",
        ),
    )

    val calendar: List<TutorialStep> = listOf(
        TutorialStep(
            id = "calendar_heat",
            screen = TutorialScreen.Calendar,
            targetId = TutorialTargetIds.CALENDAR_HEAT,
            title = "Heat across the span",
            body = "One band per event covers every day in its span. Days without an entry still paint.",
        ),
        TutorialStep(
            id = "calendar_plus",
            screen = TutorialScreen.Calendar,
            targetId = TutorialTargetIds.CALENDAR_PLUS,
            title = "Add an entry",
            body = "Plus lists Add new first, then events ending in the last year, newest first.",
        ),
        TutorialStep(
            id = "calendar_day",
            screen = TutorialScreen.Calendar,
            targetId = TutorialTargetIds.CALENDAR_DAY_PANE,
            title = "The selected day",
            body = "This pane lists events that cover the day you tapped, and their entries.",
        ),
    )

    val trends: List<TutorialStep> = listOf(
        TutorialStep(
            id = "trends_category",
            screen = TutorialScreen.Trends,
            targetId = TutorialTargetIds.TRENDS_CHIPS,
            title = "One category",
            body = "The chart shows one category at a time. Tap another chip to switch.",
        ),
        TutorialStep(
            id = "trends_wave",
            screen = TutorialScreen.Trends,
            targetId = TutorialTargetIds.TRENDS_WAVE,
            title = "Daily intensity",
            body = "The wave is peak intensity per day, not one point per event. Days without an entry sit at zero. Drag to inspect a day.",
        ),
        TutorialStep(
            id = "trends_events",
            screen = TutorialScreen.Trends,
            targetId = TutorialTargetIds.TRENDS_EVENTS,
            title = "Zoom into an event",
            body = "Tap an event to zoom the wave to that span, then open the event if you want.",
        ),
    )

    val eventDetail: List<TutorialStep> = listOf(
        TutorialStep(
            id = "event_entries",
            screen = TutorialScreen.EventDetail,
            targetId = TutorialTargetIds.EVENT_ENTRIES,
            title = "Entries belong here",
            body = "Each entry is one day under this event — optional time and intensity 1-10.",
        ),
        TutorialStep(
            id = "event_add",
            screen = TutorialScreen.EventDetail,
            targetId = TutorialTargetIds.EVENT_ADD,
            title = "Add an entry",
            body = "Add a dated entry. If the day is outside the event's span, the span widens.",
        ),
        TutorialStep(
            id = "event_heatmap",
            screen = TutorialScreen.EventDetail,
            targetId = TutorialTargetIds.EVENT_HEATMAP,
            title = "Intensity heatmap",
            body = "This strip shows intensity across the event's days.",
        ),
        TutorialStep(
            id = "event_swipe",
            screen = TutorialScreen.EventDetail,
            targetId = TutorialTargetIds.EVENT_SWIPE,
            title = "Swipe between events",
            body = "Swipe sideways to move through your events without going back to Home.",
        ),
    )

    val startEvent: List<TutorialStep> = listOf(
        TutorialStep(
            id = "start_intro",
            screen = TutorialScreen.StartEvent,
            targetId = TutorialTargetIds.START_INTRO,
            title = "Event, then entries",
            body = "An event is the parent: category plus a date span. You add single-day entries after it exists.",
        ),
        TutorialStep(
            id = "start_category_dates",
            screen = TutorialScreen.StartEvent,
            targetId = TutorialTargetIds.START_CATEGORY_DATES,
            title = "Category and dates",
            body = "Choose the type and the span this event covers. Title is optional.",
        ),
        TutorialStep(
            id = "start_span",
            screen = TutorialScreen.StartEvent,
            targetId = TutorialTargetIds.START_SPAN,
            title = "The span can widen",
            body = "Adding an entry outside these dates expands the event automatically. You can also widen the span here later.",
        ),
    )

    val entryEditor: List<TutorialStep> = listOf(
        TutorialStep(
            id = "entry_intensity",
            screen = TutorialScreen.EntryEditor,
            targetId = TutorialTargetIds.ENTRY_INTENSITY,
            title = "Intensity 1-10",
            body = "How strong this day felt. Trends uses the peak intensity for the category that day.",
        ),
        TutorialStep(
            id = "entry_time",
            screen = TutorialScreen.EntryEditor,
            targetId = TutorialTargetIds.ENTRY_TIME,
            title = "Time is optional",
            body = "Add a start time if you want, or leave it as a whole-day entry.",
        ),
        TutorialStep(
            id = "entry_day",
            screen = TutorialScreen.EntryEditor,
            targetId = TutorialTargetIds.ENTRY_DAY,
            title = "One day under the event",
            body = "An entry is a single day. If that day is outside the event span, the span grows.",
        ),
    )

    val settings: List<TutorialStep> = listOf(
        TutorialStep(
            id = "settings_categories",
            screen = TutorialScreen.Settings,
            targetId = TutorialTargetIds.SETTINGS_CATEGORIES,
            title = "Categories",
            body = "Rename event types and change their colours. Appearance lives here too.",
        ),
        TutorialStep(
            id = "settings_data",
            screen = TutorialScreen.Settings,
            targetId = TutorialTargetIds.SETTINGS_DATA,
            title = "Data",
            body = "Export or import a JSON backup. It never leaves this device unless you share the file.",
        ),
        TutorialStep(
            id = "settings_replay",
            screen = TutorialScreen.Settings,
            targetId = TutorialTargetIds.SETTINGS_VIEW_TUTORIAL,
            title = "View tutorial",
            body = "Replay the full walkthrough on Home, Calendar, Trends, and Settings.",
        ),
    )

    val data: List<TutorialStep> = listOf(
        TutorialStep(
            id = "data_local",
            screen = TutorialScreen.Data,
            targetId = TutorialTargetIds.DATA_LOCAL,
            title = "Local JSON only",
            body = "Backup is a file you save or pick on this device. There is no cloud sync.",
        ),
        TutorialStep(
            id = "data_merge",
            screen = TutorialScreen.Data,
            targetId = TutorialTargetIds.DATA_MERGE_REPLACE,
            title = "Merge or replace",
            body = "Merge keeps your locals and updates matching IDs. Replace all deletes local items that are not in the file.",
        ),
    )

    fun stepsFor(section: TutorialSection): List<TutorialStep> = when (section) {
        TutorialSection.Full -> fullTour
        TutorialSection.Home -> home
        TutorialSection.Calendar -> calendar
        TutorialSection.Trends -> trends
        TutorialSection.EventDetail -> eventDetail
        TutorialSection.StartEvent -> startEvent
        TutorialSection.EntryEditor -> entryEditor
        TutorialSection.Settings -> settings
        TutorialSection.Data -> data
    }
}

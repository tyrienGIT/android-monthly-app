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
    const val START_EMOJI = "start_emoji"

    const val ENTRY_INTENSITY = "entry_intensity"
    const val ENTRY_EMOJI = "entry_emoji"
    const val ENTRY_TIME = "entry_time"
    const val ENTRY_DAY = "entry_day"

    const val SETTINGS_PREFS = "settings_prefs"
    const val SETTINGS_BOOKS = "settings_books"
    const val SETTINGS_CATEGORIES = "settings_categories"
    const val SETTINGS_DATA = "settings_data"
    const val SETTINGS_FEEDBACK = "settings_feedback"
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
            title = "Just for you, on this phone",
            body = "Nothing is uploaded. The title is your current book — tap it to add another journal. An event is a category and a stretch of days.",
        ),
        TutorialStep(
            id = "full_home_fab",
            screen = TutorialScreen.Home,
            targetId = TutorialTargetIds.HOME_FAB,
            title = "Start an event",
            body = "This plus begins a new event. Choose a type and dates first — you add days under it after.",
        ),
        TutorialStep(
            id = "full_home_calendar",
            screen = TutorialScreen.Home,
            targetId = TutorialTargetIds.HOME_CALENDAR,
            title = "The month view",
            body = "Open Calendar to see each event as a soft band across its days.",
        ),
        TutorialStep(
            id = "full_calendar_heat",
            screen = TutorialScreen.Calendar,
            targetId = TutorialTargetIds.CALENDAR_HEAT,
            title = "One band per event",
            body = "A band covers the whole span — even days you didn’t log. Tap a day to look closer.",
        ),
        TutorialStep(
            id = "full_calendar_plus",
            screen = TutorialScreen.Calendar,
            targetId = TutorialTargetIds.CALENDAR_PLUS,
            title = "Add a day from here",
            body = "Plus shows Add new first, then events from the last year. Pick one to log the selected day.",
        ),
        TutorialStep(
            id = "full_trends_category",
            screen = TutorialScreen.Trends,
            targetId = TutorialTargetIds.TRENDS_CHIPS,
            title = "One category at a time",
            body = "The wave is each day’s intensity, not one dot per event. Tap a chip to switch category.",
        ),
        TutorialStep(
            id = "full_trends_events",
            screen = TutorialScreen.Trends,
            targetId = TutorialTargetIds.TRENDS_EVENTS,
            title = "Zoom and peek",
            body = "Tap an event to zoom into that stretch. Drag the wave to peek at a day.",
        ),
        TutorialStep(
            id = "full_settings",
            screen = TutorialScreen.Settings,
            targetId = TutorialTargetIds.SETTINGS_PREFS,
            title = "Make it yours",
            body = "Books keep separate journals. Categories and backup belong to the book that’s open.",
        ),
    )

    val home: List<TutorialStep> = listOf(
        TutorialStep(
            id = "home_welcome",
            screen = TutorialScreen.Home,
            targetId = TutorialTargetIds.HOME_WELCOME,
            title = "Just for you, on this phone",
            body = "Home lists this book’s events. Tap the title to switch or add another book.",
        ),
        TutorialStep(
            id = "home_fab",
            screen = TutorialScreen.Home,
            targetId = TutorialTargetIds.HOME_FAB,
            title = "Start an event",
            body = "The plus begins a new event — a type and a stretch of dates.",
        ),
        TutorialStep(
            id = "home_nav",
            screen = TutorialScreen.Home,
            targetId = TutorialTargetIds.HOME_NAV_ICONS,
            title = "Around the app",
            body = "Calendar is the month. Trends is one category’s wave. Settings is names, colours, and backup.",
        ),
    )

    val calendar: List<TutorialStep> = listOf(
        TutorialStep(
            id = "calendar_heat",
            screen = TutorialScreen.Calendar,
            targetId = TutorialTargetIds.CALENDAR_HEAT,
            title = "Heat across the span",
            body = "One band per event. Days you didn’t log still show in the band.",
        ),
        TutorialStep(
            id = "calendar_plus",
            screen = TutorialScreen.Calendar,
            targetId = TutorialTargetIds.CALENDAR_PLUS,
            title = "Add a day",
            body = "Plus lists Add new first, then events from the last year.",
        ),
        TutorialStep(
            id = "calendar_day",
            screen = TutorialScreen.Calendar,
            targetId = TutorialTargetIds.CALENDAR_DAY_PANE,
            title = "The day you tapped",
            body = "Here’s what covers that day, and any entries you already logged.",
        ),
    )

    val trends: List<TutorialStep> = listOf(
        TutorialStep(
            id = "trends_category",
            screen = TutorialScreen.Trends,
            targetId = TutorialTargetIds.TRENDS_CHIPS,
            title = "One category",
            body = "The chart shows one type at a time. Tap another chip to switch.",
        ),
        TutorialStep(
            id = "trends_wave",
            screen = TutorialScreen.Trends,
            targetId = TutorialTargetIds.TRENDS_WAVE,
            title = "A wave of days",
            body = "Each point is that day’s peak intensity. Empty days sit at zero. Drag to peek.",
        ),
        TutorialStep(
            id = "trends_events",
            screen = TutorialScreen.Trends,
            targetId = TutorialTargetIds.TRENDS_EVENTS,
            title = "Zoom into an event",
            body = "Tap an event to zoom the wave to that stretch. Open it if you want the details.",
        ),
    )

    val eventDetail: List<TutorialStep> = listOf(
        TutorialStep(
            id = "event_entries",
            screen = TutorialScreen.EventDetail,
            targetId = TutorialTargetIds.EVENT_ENTRIES,
            title = "Days under this event",
            body = "Each entry is one day — optional time, and how strong it felt from 1 to 10.",
        ),
        TutorialStep(
            id = "event_add",
            screen = TutorialScreen.EventDetail,
            targetId = TutorialTargetIds.EVENT_ADD,
            title = "Add a day",
            body = "Log a dated entry here. If it’s outside the span, the event grows to fit.",
        ),
        TutorialStep(
            id = "event_heatmap",
            screen = TutorialScreen.EventDetail,
            targetId = TutorialTargetIds.EVENT_HEATMAP,
            title = "A quiet heatmap",
            body = "This strip is intensity across the event’s days.",
        ),
        TutorialStep(
            id = "event_swipe",
            screen = TutorialScreen.EventDetail,
            targetId = TutorialTargetIds.EVENT_SWIPE,
            title = "Swipe to the next one",
            body = "Swipe sideways to move between events — no need to go back to Home.",
        ),
    )

    val startEvent: List<TutorialStep> = listOf(
        TutorialStep(
            id = "start_intro",
            screen = TutorialScreen.StartEvent,
            targetId = TutorialTargetIds.START_INTRO,
            title = "Event first, days later",
            body = "An event is the parent: a type and a stretch of dates. You add single days after it exists.",
        ),
        TutorialStep(
            id = "start_category_dates",
            screen = TutorialScreen.StartEvent,
            targetId = TutorialTargetIds.START_CATEGORY_DATES,
            title = "Type and dates",
            body = "Pick the category and the span. A title and tags are optional.",
        ),
        TutorialStep(
            id = "start_span",
            screen = TutorialScreen.StartEvent,
            targetId = TutorialTargetIds.START_SPAN,
            title = "The span can grow",
            body = "Logging a day outside these dates widens the event. You can also stretch it here later.",
        ),
        TutorialStep(
            id = "start_emoji",
            screen = TutorialScreen.StartEvent,
            targetId = TutorialTargetIds.START_EMOJI,
            title = "Tag it",
            body = "Optional emoji or kaomoji — up to three. Switch kaomoji by mood: Period, Anxious, Happy, Sad, Cramps.",
        ),
    )

    val entryEditor: List<TutorialStep> = listOf(
        TutorialStep(
            id = "entry_intensity",
            screen = TutorialScreen.EntryEditor,
            targetId = TutorialTargetIds.ENTRY_INTENSITY,
            title = "How strong, 1 to 10",
            body = "How this day felt. Trends uses the peak for that category on that day.",
        ),
        TutorialStep(
            id = "entry_time",
            screen = TutorialScreen.EntryEditor,
            targetId = TutorialTargetIds.ENTRY_TIME,
            title = "Time is optional",
            body = "Add a start time, or leave it as a whole day.",
        ),
        TutorialStep(
            id = "entry_day",
            screen = TutorialScreen.EntryEditor,
            targetId = TutorialTargetIds.ENTRY_DAY,
            title = "One day under the event",
            body = "An entry is a single day. Outside the event’s dates? The span grows to fit.",
        ),
        TutorialStep(
            id = "entry_emoji",
            screen = TutorialScreen.EntryEditor,
            targetId = TutorialTargetIds.ENTRY_EMOJI,
            title = "Tag this day",
            body = "Optional emoji or kaomoji just for this entry. The event can have its own tags too.",
        ),
    )

    val settings: List<TutorialStep> = listOf(
        TutorialStep(
            id = "settings_books",
            screen = TutorialScreen.Settings,
            targetId = TutorialTargetIds.SETTINGS_BOOKS,
            title = "Books",
            body = "Add another journal if you want a separate set of events. Your first book is already here.",
        ),
        TutorialStep(
            id = "settings_categories",
            screen = TutorialScreen.Settings,
            targetId = TutorialTargetIds.SETTINGS_CATEGORIES,
            title = "Categories",
            body = "Rename types and change their colours for this book. Appearance is here too.",
        ),
        TutorialStep(
            id = "settings_data",
            screen = TutorialScreen.Settings,
            targetId = TutorialTargetIds.SETTINGS_DATA,
            title = "Your backup",
            body = "Export or import a JSON file. It stays on this device unless you share it.",
        ),
        TutorialStep(
            id = "settings_feedback",
            screen = TutorialScreen.Settings,
            targetId = TutorialTargetIds.SETTINGS_FEEDBACK,
            title = "Send a note",
            body = "Save markdown notes on this phone, then Share to Messages, email, or copy.",
        ),
        TutorialStep(
            id = "settings_replay",
            screen = TutorialScreen.Settings,
            targetId = TutorialTargetIds.SETTINGS_VIEW_TUTORIAL,
            title = "Walk through again",
            body = "Replay the tour on the real Home, Calendar, Trends, and Settings screens.",
        ),
    )

    val data: List<TutorialStep> = listOf(
        TutorialStep(
            id = "data_local",
            screen = TutorialScreen.Data,
            targetId = TutorialTargetIds.DATA_LOCAL,
            title = "A file on this phone",
            body = "Backup is a JSON file you save or pick here. Nothing syncs to the cloud.",
        ),
        TutorialStep(
            id = "data_merge",
            screen = TutorialScreen.Data,
            targetId = TutorialTargetIds.DATA_MERGE_REPLACE,
            title = "Merge or replace",
            body = "Merge keeps this book and updates matching IDs. Replace all throws away anything in this book that is not in the file.",
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

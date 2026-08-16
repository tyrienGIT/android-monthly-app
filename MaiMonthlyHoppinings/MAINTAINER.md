# Mai Monthly Hoppinings — maintainer guide

Local-only Android tracker. The project folder is `MaiMonthlyHoppinings`; the product name, package, and application class are **Mai Monthly Hoppinings** / `com.maimonthlyhoppinings`.

This file is the onboarding doc: what exists, why it is shaped this way, and the rules that keep later changes from breaking the calendar, heat bands, or stored data.

## Product intent

Offline personal tracker. No accounts, no network, no sync.

The user **starts an event** (type + optional title + date span), then **adds entries** under it (single day, optional time, intensity 1–10). The calendar paints each parent event as one continuous heat band across its span. Intensity along that band is a gradient of the child entries.

Event type labels are still placeholders (`Placeholder type 1` … `5`). They are stored as strings on events, so renaming them is a data-migration problem, not a UI-only change.

## Stack

| Piece | Choice |
| --- | --- |
| Language | Kotlin 2.0.21, JVM 17 |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose (string routes) |
| Persistence | Room `mai_monthly_hoppinings.db` (version **10**) |
| Theme prefs | DataStore `theme_preferences` |
| DI | None. `MaiMonthlyHoppiningsApp` holds repositories; screens use `ViewModelProvider.Factory` |
| Tests | None |
| Git | Not initialized in this folder |

`minSdk` 26, `compileSdk` / `targetSdk` 35. Gradle 8.9, AGP 8.7.3. Versions live in `gradle/libs.versions.toml`.

## What was built (Jul 27–28, 2026)

Started as a native Kotlin calendar (Android-only, strictly local). Grew into a tracker, then was renamed and split into parent events + child entries.

1. **Calendar + day logging** — log something on a day, then add optional time.
2. **Home list** — start destination lists events; calendar is a second screen.
3. **Event types + colours** — five placeholder types, each mapped to Red / Purple / Yellow / Blue / Green. Type colour tints the editor and the heat bands. Dark mode slightly pastels those colours.
4. **Vertical week calendar** — Monday→Sunday rows, weekends shaded as a solid block on the right, scroll down through weeks. Opens with the current month centered. Range is ±52 weeks from this week’s Monday.
5. **Heat bands** — solid stacked bars (max 5 per day), not dots. One band per parent event across its whole span. Intensity is a chronological gradient of that event’s entries. Days with no entry still paint by sampling the gradient. Overlap: keep the most intense bands, then restack in colour-then-`eventId` order so the same event stays on the same lane.
6. **Split calendar pane** — upper ~2/3 is the grid; lower ~1/3 lists events for the selected day. Show 3 sub-entries, then `…+N` to expand, then `…` to open the parent event.
7. **Appearance** — light/dark/system is independent of colour theme. Ten balanced presets (Coastal default). Nested settings + theme builder with linked light/dark HSV seeds. Custom themes persist in Room.
8. **Event / entry split** — parent holds type + span; entries hold date, optional time, optional title, intensity. Parent span expands to cover entries and can be widened by hand, but cannot shrink past existing entries.
9. **Event-detail heatmap** — peak intensity per day across the parent span.
10. **Confirmations** — save and delete dialogs. Empty titles fall back to the type (event) or parent title (entry).

Known bugs that were already patched: theme-apply crash from invalid HSV (`Color.hsv` rejects NaN), Material 3 FABs staying purple because container roles were left on the default scheme, flaky entry saves (double-submit).

## Architecture

```
MaiMonthlyHoppiningsApp
  ├── EventRepository          Room: events + entries
  ├── SavedThemeRepository     Room: custom palettes
  └── ThemePreferences         DataStore: mode + active theme id

MainActivity
  └── ThemeViewModel → MaiMonthlyHoppiningsTheme(palette)
        └── AppNav
              ├── Home / Calendar / Event / Entry  (EventRepository)
              └── Settings graph                   (ThemeViewModel)
```

Package layout:

```
com.maimonthlyhoppinings/
  MaiMonthlyHoppiningsApp.kt          Application + repo wiring
  MainActivity.kt            Edge-to-edge, theme wrapper
  data/                      Entities, DAOs, repos, formatting, heat math
  ui/
    AppNav.kt                All routes
    Confirm*.kt              Shared dialogs
    home/                    Event list
    calendar/                Week grid + selected-day pane
    event/                   Start/edit event, detail, entry editor, pickers
    settings/                Nested appearance menus + theme builder
    theme/                   Palettes, HSV helpers, event-type colours
```

Screens observe `StateFlow` from a ViewModel. Writes go through the repository. Do not query DAOs from UI.

## Domain model

### TrackedEvent (`tracked_events`)

Parent. Fields: `title`, `eventType` (string), `details`, `startDateEpochDay`, `endDateEpochDay`, `createdAtMillis`.

- Title empty → stored/displayed as the type label.
- Span is inclusive epoch days (`LocalDate.toEpochDay()`).
- Deleting an event **cascades** to all entries (`ForeignKey.CASCADE`).

### EventEntry (`event_entries`)

Child of one event. Fields: `eventId`, `title` (optional), `dateEpochDay`, `startTimeMinutesOfDay` (nullable 0–1439), `details`, `intensity` (1–10), `createdAtMillis`.

- Title empty → display the parent title.
- Saving an entry **expands** the parent span to include that date.
- Updating a parent span **widens** if needed so every existing entry still fits. It will not drop entries to honour a narrower manual range.

### Event types (`EventType`)

Hard-coded list in `data/EventType.kt`. Colour comes from `EventTypeColor`, not from the app colour theme. Calendar heat and editor chrome use type colour; chrome (app bars, FABs, settings) uses the Material palette.

### Dates and times

- Persist **epoch days**, never timezone-local midnight millis.
- Material date pickers speak UTC millis. Convert with `utcMillisToEpochDay` in `EventDateTimePickers.kt`. Do not use the device zone for that conversion or days will shift around DST / offset.
- Optional entry time is minutes from midnight.

## Invariants (do not break these)

**Room updates must use `@Update`, never insert-or-replace, on `TrackedEvent`.** `OnConflictStrategy.REPLACE` deletes the row first; CASCADE then wipes every child entry. The repository comment on `updateEvent` exists because this already bit us.

**Bump `AppDatabase` version and treat data as disposable until real migrations exist.** `fallbackToDestructiveMigration()` is on. Schema change = wipe. Version is already 10 from the event/entry/theme iterations.

**Renaming a type label is a migration.** `eventType` is a raw string. `EventType.isValid` rejects unknown values. Change labels only with a Room migration that rewrites existing rows.

**Heat is one band per parent event, not one band per entry.** `EventRepository.observeHeatSegmentsInRange` builds chronological intensity stops from entries (or `EVENT_SPAN_PRESENCE_INTENSITY = 2` if none), then samples along `spanProgress` (0 at start date, 1 at end). Calendar cells draw a short window of that gradient so neighbouring days read as one ribbon.

**Max 5 heat segments per day.** If more events overlap, keep the highest `peakIntensity()`, then sort remaining by `EventTypeColor.ordinal` then `eventId` so lane order is stable across days.

**Intensity is 1–10.** Alpha mapping is `0.18 + (intensity/10)*0.82` in `EventColors.intensityHeatAlpha`. Keep calendar bars and list rows on the same function.

**Theme HSV must go through `safeHsvColor` / `toHsv`.** Compose `Color.hsv` crashes on NaN or out-of-range hue; that produced a white screen. Do not call `Color.hsv` with raw slider math.

**Preset palettes need `withDerivedContainers`.** Material 3 `lightColorScheme` / `darkColorScheme` leave container roles on the purple baseline. FABs and chips will ignore the theme if you skip this.

**Empty title fallbacks:** event → type; entry → parent title. Keep that in both the repository (`EventInput.toEntity`) and the UI confirm dialogs.

## Navigation

Defined in `ui/AppNav.kt` and `ui/settings/SettingsRoutes.kt`.

| Route | Screen |
| --- | --- |
| `home` | Event list (start destination) |
| `calendar` | Week grid |
| `event/new?epochDay=` | Start event; `epochDay` seeds the span when coming from the calendar |
| `event/{id}` | Event detail + per-day heatmap + entries |
| `event/{id}/edit` | Same form as start, editing |
| `entry/new?eventId=&epochDay=` | New entry |
| `entry/{id}` | Edit entry |
| `settings` graph | Settings → Appearance → Light/Dark, Colour themes, Theme builder |

Post-save behaviour is contextual:

- Start event from **home** → event detail (pop to home).
- Start event from **calendar** (seeded date) → new entry for that day (pop to calendar).
- Save new entry → pop to that event’s detail if it is on the stack, else calendar, else one step back.

ViewModels are keyed (`event-new-…`, `entry-edit-…`) so navigating to a different id does not reuse a stale editor.

## Theming

Two independent axes:

1. **Mode** — `ThemeMode.SYSTEM | LIGHT | DARK` in DataStore key `theme_mode`.
2. **Colour theme** — `ActiveColorTheme.Preset(ColorTheme)` or `ActiveColorTheme.Custom(id)`, encoded as `PRESET:COASTAL` / `CUSTOM:12` in `active_color_theme`. Older builds used `color_theme`; `ThemePreferences` still migrates that key.

Presets: Coastal, Forest, Slate, Ember, Ink, Orchard, Grove, Canyon, Glacier, Rosewood. Palettes are hand-tuned in `ui/theme/ColorThemes.kt`.

Custom themes store six ARGB seeds (primary/secondary/tertiary × light/dark) in `saved_color_themes`. `CustomPalette.buildPaletteFromModeSeeds` derives surfaces, containers, and on-colours. Theme builder can **link** light and dark: editing one side runs `deriveDarkAccent` / `deriveLightAccent`.

Deleting the active custom theme falls back to Coastal.

Type colours stay on the event-type axis. Do not tint heat bands with the Material primary.

## Calendar behaviour

- Monday-first weeks (`CalendarModels.mondayOfWeek`).
- Weekend columns (Sat/Sun) share a `surfaceVariant` slab.
- Selected day is a bordered cell; today is a primary-filled day number.
- Add on a day: if no events exist, start a new event seeded to that day; otherwise `PickEventForEntryDialog` (pick parent or start new).
- Selected-day pane groups by parent, then entries (timed first, then untimed, then intensity, then recency).

## How to run

Needs Android Studio / SDK. Historical emulator name: `Virtual_Device_-_API_35_Android_15`.

```bat
gradlew.bat :app:installDebug
adb shell am start -n com.maimonthlyhoppinings/.MainActivity
```

Or open the folder in Android Studio and Run.

There is no in-Cursor preview that replaces an emulator. Debug APKs land under `app/build/outputs/apk/debug/`.

## Common change map

| You want to… | Start here |
| --- | --- |
| Rename / add event types | `data/EventType.kt` + a Room migration that rewrites `tracked_events.eventType` |
| Change type colours | `ui/theme/EventColors.kt` (`EventTypeColor.toComposeColor`) |
| Change heat math or overlap rules | `data/EventRepository.kt` (`observeHeatSegmentsInRange`, `selectHeatSegmentsForDay`) and `ui/calendar/CalendarScreen.kt` (`HeatBar`, `eventHeatBrush`) |
| Change event/entry fields | Entity + DAO + `EventInput`/`EntryInput` + the matching editor ViewModel/Screen. Bump Room version. |
| Add a settings page | New composable under `ui/settings/`, route on `SettingsRoutes`, destination in the `settings` nav graph in `AppNav.kt` |
| Add a colour preset | `data/ColorTheme.kt` + a branch in `ui/theme/ColorThemes.kt` |
| Tweak theme generation | `ui/theme/CustomPalette.kt` (normalization, contrast, containers) |
| Change date/time pickers | `ui/event/EventDateTimePickers.kt` — keep UTC epoch-day conversion |
| Change post-save navigation | `ui/AppNav.kt` only |

## Landmines

- **Destructive migrations.** Treat the DB as wipe-on-schema-change until you add real `Migration` objects and `exportSchema = true`.
- **`allowBackup` is true** in the manifest. Local event data can be included in Android backups. Turn this off or exclude the DB before any release if that is not wanted.
- **No encryption.** Room file is plaintext on device. Fine for a private local prototype; not fine if this becomes shared-device or Play-store health data.
- **No Hilt / no tests.** Easy to regress heat selection, span expansion, and navigation. If you add tests, start with `EventRepository` heat + span helpers — they are pure enough to unit-test without Compose.
- **Calendar range is fixed at construction** (`LocalDate.now()` in `CalendarViewModel`). Crossing midnight or leaving the app open across a year boundary will not rebuild the ±52 week window until process death.
- **Date range picker UX.** Tap **Start** to clear and pick a new range; tap **End** to keep start and adjust end. Documented on the field; do not “simplify” back to two independent pickers without checking that behaviour.
- **Entry save is guarded** (`saveInFlight`) because the confirm dialog + channel could double-insert. Keep a single-flight lock on writes that navigate away.
- **ViewModel factories are copy-pasted.** New screens should follow the same `factory(...)` companion pattern and take repositories from `MaiMonthlyHoppiningsApp`, not construct Room themselves.

## Still unfinished / placeholder

- Event type labels and the “tracker on top of a calendar” domain (real questions, custom properties beyond intensity).
- Real Room migrations and schema export.
- Tests, CI, git.
- Backup / export / import of events.
- Release signing, minify (`isMinifyEnabled = false`), Play listing.
- i18n — user-facing strings are hard-coded in composables; only `app_name` is in `strings.xml`.
- Widget, notifications, lock-screen privacy.

When replacing placeholders, keep type colour as a separate axis from the Material colour theme, and keep intensity heat on the type colour.

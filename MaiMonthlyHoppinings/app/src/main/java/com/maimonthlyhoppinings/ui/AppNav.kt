package com.maimonthlyhoppinings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.maimonthlyhoppinings.MaiMonthlyHoppiningsApp
import com.maimonthlyhoppinings.ui.calendar.CalendarScreen
import com.maimonthlyhoppinings.ui.calendar.CalendarViewModel
import com.maimonthlyhoppinings.ui.event.EntryEditorScreen
import com.maimonthlyhoppinings.ui.event.EntryEditorViewModel
import com.maimonthlyhoppinings.ui.event.EventDetailScreen
import com.maimonthlyhoppinings.ui.event.EventDetailViewModel
import com.maimonthlyhoppinings.ui.event.StartEventScreen
import com.maimonthlyhoppinings.ui.event.StartEventViewModel
import com.maimonthlyhoppinings.ui.home.HomeScreen
import com.maimonthlyhoppinings.ui.home.HomeViewModel
import com.maimonthlyhoppinings.ui.settings.AppearanceScreen
import com.maimonthlyhoppinings.ui.settings.ColorThemeSettingsScreen
import com.maimonthlyhoppinings.ui.settings.DataBackupViewModel
import com.maimonthlyhoppinings.ui.settings.LightDarkSettingsScreen
import com.maimonthlyhoppinings.ui.settings.SettingsHomeScreen
import com.maimonthlyhoppinings.ui.settings.SettingsRoutes
import com.maimonthlyhoppinings.ui.settings.ThemeBuilderScreen
import com.maimonthlyhoppinings.ui.theme.ThemeViewModel
import java.time.LocalDate

private object Routes {
    const val Home = "home"
    const val Calendar = "calendar"
    const val EventNew = "event/new?epochDay={epochDay}"
    const val EventDetail = "event/{eventId}"
    const val EventEdit = "event/{eventId}/edit"
    const val EntryNew = "entry/new?eventId={eventId}&epochDay={epochDay}"
    const val EntryEdit = "entry/{entryId}"

    fun eventNew(epochDay: Long? = null): String {
        return if (epochDay == null) "event/new" else "event/new?epochDay=$epochDay"
    }

    fun eventDetail(eventId: Long): String = "event/$eventId"

    fun eventEdit(eventId: Long): String = "event/$eventId/edit"

    fun entryNew(eventId: Long, epochDay: Long = LocalDate.now().toEpochDay()): String {
        return "entry/new?eventId=$eventId&epochDay=$epochDay"
    }

    fun entryEdit(entryId: Long): String = "entry/$entryId"
}

@Composable
fun AppNav(
    themeViewModel: ThemeViewModel,
) {
    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as MaiMonthlyHoppiningsApp
    val repository = app.eventRepository
    val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
    val activeColorTheme by themeViewModel.activeColorTheme.collectAsStateWithLifecycle()
    val savedThemes by themeViewModel.savedThemes.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Routes.Home,
    ) {
        composable(Routes.Home) {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.factory(repository),
            )
            HomeScreen(
                viewModel = viewModel,
                onOpenSettings = { navController.navigate(SettingsRoutes.Graph) },
                onOpenCalendar = { navController.navigate(Routes.Calendar) },
                onStartEvent = { navController.navigate(Routes.eventNew()) },
                onOpenEvent = { eventId ->
                    navController.navigate(Routes.eventDetail(eventId))
                },
            )
        }

        navigation(
            route = SettingsRoutes.Graph,
            startDestination = SettingsRoutes.Root,
        ) {
            composable(SettingsRoutes.Root) {
                val backupViewModel: DataBackupViewModel = viewModel(
                    factory = DataBackupViewModel.factory(app.backupRepository),
                )
                SettingsHomeScreen(
                    viewModel = backupViewModel,
                    onOpenAppearance = { navController.navigate(SettingsRoutes.Appearance) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(SettingsRoutes.Appearance) {
                AppearanceScreen(
                    themeMode = themeMode,
                    activeColorTheme = activeColorTheme,
                    savedThemes = savedThemes,
                    onOpenLightDark = { navController.navigate(SettingsRoutes.LightDark) },
                    onOpenColorThemes = { navController.navigate(SettingsRoutes.ColorThemes) },
                    onOpenThemeBuilder = { navController.navigate(SettingsRoutes.ThemeBuilder) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(SettingsRoutes.LightDark) {
                LightDarkSettingsScreen(
                    themeMode = themeMode,
                    onThemeModeSelected = themeViewModel::setThemeMode,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(SettingsRoutes.ColorThemes) {
                ColorThemeSettingsScreen(
                    activeColorTheme = activeColorTheme,
                    savedThemes = savedThemes,
                    onPresetSelected = themeViewModel::setPresetColorTheme,
                    onCustomSelected = themeViewModel::setCustomColorTheme,
                    onDeleteCustom = themeViewModel::deleteSavedTheme,
                    onOpenBuilder = { navController.navigate(SettingsRoutes.ThemeBuilder) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(SettingsRoutes.ThemeBuilder) {
                ThemeBuilderScreen(
                    onSave = { payload ->
                        themeViewModel.saveCustomTheme(
                            name = payload.name,
                            lightPrimaryArgb = payload.lightPrimaryArgb,
                            lightSecondaryArgb = payload.lightSecondaryArgb,
                            lightTertiaryArgb = payload.lightTertiaryArgb,
                            darkPrimaryArgb = payload.darkPrimaryArgb,
                            darkSecondaryArgb = payload.darkSecondaryArgb,
                            darkTertiaryArgb = payload.darkTertiaryArgb,
                            applyAfterSave = payload.apply,
                        )
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() },
                )
            }
        }

        composable(Routes.Calendar) {
            val viewModel: CalendarViewModel = viewModel(
                factory = CalendarViewModel.factory(repository),
            )
            CalendarScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onStartEvent = { date ->
                    navController.navigate(Routes.eventNew(date.toEpochDay()))
                },
                onAddEntryForEvent = { date, eventId ->
                    navController.navigate(Routes.entryNew(eventId, date.toEpochDay()))
                },
                onOpenEvent = { eventId ->
                    navController.navigate(Routes.eventDetail(eventId))
                },
                onEditEntry = { entryId ->
                    navController.navigate(Routes.entryEdit(entryId))
                },
            )
        }

        composable(
            route = Routes.EventNew,
            arguments = listOf(
                navArgument("epochDay") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
            ),
        ) { entry ->
            val epochDay = entry.arguments?.getLong("epochDay") ?: -1L
            val seedDate = epochDay.takeIf { it >= 0L }?.let(LocalDate::ofEpochDay)
            val viewModel: StartEventViewModel = viewModel(
                key = "event-new-${seedDate ?: "today"}",
                factory = StartEventViewModel.factory(
                    editingEventId = null,
                    seedDate = seedDate ?: LocalDate.now(),
                    eventRepository = repository,
                ),
            )
            StartEventScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaved = { eventId ->
                    if (seedDate != null) {
                        navController.navigate(Routes.entryNew(eventId, seedDate.toEpochDay())) {
                            popUpTo(Routes.Calendar) { inclusive = false }
                        }
                    } else {
                        navController.navigate(Routes.eventDetail(eventId)) {
                            popUpTo(Routes.Home) { inclusive = false }
                        }
                    }
                },
            )
        }

        composable(
            route = Routes.EventDetail,
            arguments = listOf(
                navArgument("eventId") { type = NavType.LongType },
            ),
        ) { entry ->
            val eventId = entry.arguments?.getLong("eventId") ?: return@composable
            val viewModel: EventDetailViewModel = viewModel(
                key = "event-$eventId",
                factory = EventDetailViewModel.factory(eventId, repository),
            )
            EventDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEditEvent = { navController.navigate(Routes.eventEdit(eventId)) },
                onAddEntry = {
                    navController.navigate(Routes.entryNew(eventId))
                },
                onOpenEntry = { entryId ->
                    navController.navigate(Routes.entryEdit(entryId))
                },
            )
        }

        composable(
            route = Routes.EventEdit,
            arguments = listOf(
                navArgument("eventId") { type = NavType.LongType },
            ),
        ) { entry ->
            val eventId = entry.arguments?.getLong("eventId") ?: return@composable
            val viewModel: StartEventViewModel = viewModel(
                key = "event-edit-$eventId",
                factory = StartEventViewModel.factory(editingEventId = eventId, eventRepository = repository),
            )
            StartEventScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.EntryNew,
            arguments = listOf(
                navArgument("eventId") { type = NavType.LongType },
                navArgument("epochDay") {
                    type = NavType.LongType
                    defaultValue = LocalDate.now().toEpochDay()
                },
            ),
        ) { entry ->
            val eventId = entry.arguments?.getLong("eventId") ?: return@composable
            val epochDay = entry.arguments?.getLong("epochDay")
                ?: LocalDate.now().toEpochDay()
            val date = LocalDate.ofEpochDay(epochDay)
            val viewModel: EntryEditorViewModel = viewModel(
                key = "entry-new-$eventId-$epochDay",
                factory = EntryEditorViewModel.factory(
                    eventId = eventId,
                    editingEntryId = null,
                    initialDate = date,
                    eventRepository = repository,
                ),
            )
            EntryEditorScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaved = {
                    val wentToEvent = navController.popBackStack(
                        route = Routes.eventDetail(eventId),
                        inclusive = false,
                    )
                    if (!wentToEvent) {
                        val wentToCalendar = navController.popBackStack(
                            route = Routes.Calendar,
                            inclusive = false,
                        )
                        if (!wentToCalendar) {
                            navController.popBackStack()
                        }
                    }
                },
            )
        }

        composable(
            route = Routes.EntryEdit,
            arguments = listOf(
                navArgument("entryId") { type = NavType.LongType },
            ),
        ) { entry ->
            val entryId = entry.arguments?.getLong("entryId") ?: return@composable
            val viewModel: EntryEditorViewModel = viewModel(
                key = "entry-edit-$entryId",
                factory = EntryEditorViewModel.factory(
                    eventId = null,
                    editingEntryId = entryId,
                    initialDate = LocalDate.now(),
                    eventRepository = repository,
                ),
            )
            EntryEditorScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
    }
}

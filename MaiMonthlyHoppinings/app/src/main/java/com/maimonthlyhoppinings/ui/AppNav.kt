package com.maimonthlyhoppinings.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.maimonthlyhoppinings.MaiMonthlyHoppiningsApp
import com.maimonthlyhoppinings.ui.book.BookViewModel
import com.maimonthlyhoppinings.ui.book.BooksScreen
import com.maimonthlyhoppinings.ui.calendar.CalendarScreen
import com.maimonthlyhoppinings.ui.calendar.CalendarViewModel
import com.maimonthlyhoppinings.ui.event.EntryEditorScreen
import com.maimonthlyhoppinings.ui.event.EntryEditorViewModel
import com.maimonthlyhoppinings.ui.event.EventDetailScreen
import com.maimonthlyhoppinings.ui.event.StartEventScreen
import com.maimonthlyhoppinings.ui.event.StartEventViewModel
import com.maimonthlyhoppinings.ui.home.HomeScreen
import com.maimonthlyhoppinings.ui.home.HomeViewModel
import com.maimonthlyhoppinings.ui.settings.AppearanceScreen
import com.maimonthlyhoppinings.ui.settings.CategorySettingsScreen
import com.maimonthlyhoppinings.ui.settings.CategorySettingsViewModel
import com.maimonthlyhoppinings.ui.settings.ColorThemeSettingsScreen
import com.maimonthlyhoppinings.ui.settings.DataBackupViewModel
import com.maimonthlyhoppinings.ui.settings.DataSettingsScreen
import com.maimonthlyhoppinings.ui.settings.LightDarkSettingsScreen
import com.maimonthlyhoppinings.ui.settings.SettingsHomeScreen
import com.maimonthlyhoppinings.ui.settings.SettingsRoutes
import com.maimonthlyhoppinings.ui.settings.ThemeBuilderScreen
import com.maimonthlyhoppinings.ui.theme.ThemeViewModel
import com.maimonthlyhoppinings.ui.trends.TrendsScreen
import com.maimonthlyhoppinings.ui.trends.TrendsViewModel
import com.maimonthlyhoppinings.ui.tutorial.LocalTutorialController
import com.maimonthlyhoppinings.ui.tutorial.LocalTutorialTargets
import com.maimonthlyhoppinings.ui.tutorial.TutorialOverlay
import com.maimonthlyhoppinings.ui.tutorial.TutorialScreen
import com.maimonthlyhoppinings.ui.tutorial.TutorialTargetRegistry
import com.maimonthlyhoppinings.ui.tutorial.TutorialViewModel
import java.time.LocalDate

private object Routes {
    const val Home = "home"
    const val Books = "books"
    const val Calendar = "calendar"
    const val Trends = "trends"
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
    val bookViewModel: BookViewModel = viewModel(
        factory = BookViewModel.factory(app.bookManager),
    )
    val booksState by bookViewModel.uiState.collectAsStateWithLifecycle()
    val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
    val activeColorTheme by themeViewModel.activeColorTheme.collectAsStateWithLifecycle()
    val savedThemes by themeViewModel.savedThemes.collectAsStateWithLifecycle()
    val tutorialTargets = remember { TutorialTargetRegistry() }
    val tutorialViewModel: TutorialViewModel = viewModel(
        factory = TutorialViewModel.factory(app.appPreferences),
    )
    val tutorialState by tutorialViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        tutorialViewModel.startFirstRunIfNeeded()
    }

    var lastBookId by remember { mutableStateOf(booksState.active.id) }
    LaunchedEffect(booksState.active.id) {
        if (lastBookId == booksState.active.id) return@LaunchedEffect
        lastBookId = booksState.active.id
        val route = navController.currentBackStackEntry?.destination?.route.orEmpty()
        if (route.startsWith("event") || route.startsWith("entry")) {
            navController.popBackStack(Routes.Home, inclusive = false)
        }
    }

    val tourScreen = tutorialState.step?.screen
    LaunchedEffect(tutorialState.active, tutorialState.isFullTour, tourScreen) {
        if (!tutorialState.active || !tutorialState.isFullTour || tourScreen == null) {
            return@LaunchedEffect
        }
        syncFullTourNavigation(navController, tourScreen)
    }

    fun leaveFullTourIfNeeded(wasFullTour: Boolean) {
        if (wasFullTour) {
            navController.popBackStack(Routes.Home, inclusive = false)
        }
    }

    BackHandler(enabled = tutorialState.active) {
        if (tutorialState.isFirst) {
            val wasFullTour = tutorialState.isFullTour
            tutorialViewModel.skip()
            leaveFullTourIfNeeded(wasFullTour)
        } else {
            tutorialViewModel.back()
        }
    }

    CompositionLocalProvider(
        LocalTutorialTargets provides tutorialTargets,
        LocalTutorialController provides tutorialViewModel,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                bookViewModel = bookViewModel,
                onOpenSettings = { navController.navigate(SettingsRoutes.Graph) },
                onOpenBooks = { navController.navigate(Routes.Books) },
                onOpenCalendar = { navController.navigate(Routes.Calendar) },
                onOpenTrends = { navController.navigate(Routes.Trends) },
                onStartEvent = { navController.navigate(Routes.eventNew()) },
                onOpenEvent = { eventId ->
                    navController.navigate(Routes.eventDetail(eventId))
                },
            )
        }

        composable(Routes.Books) {
            BooksScreen(
                viewModel = bookViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        navigation(
            route = SettingsRoutes.Graph,
            startDestination = SettingsRoutes.Root,
        ) {
            composable(SettingsRoutes.Root) {
                SettingsHomeScreen(
                    onOpenBooks = { navController.navigate(Routes.Books) },
                    onOpenAppearance = { navController.navigate(SettingsRoutes.Appearance) },
                    onOpenCategories = { navController.navigate(SettingsRoutes.Categories) },
                    onOpenData = { navController.navigate(SettingsRoutes.Data) },
                    onReplayTutorial = {
                        navController.popBackStack(Routes.Home, inclusive = false)
                        tutorialViewModel.startFullTour()
                    },
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
            composable(SettingsRoutes.Categories) {
                val categoryViewModel: CategorySettingsViewModel = viewModel(
                    factory = CategorySettingsViewModel.factory(repository),
                )
                CategorySettingsScreen(
                    viewModel = categoryViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(SettingsRoutes.Data) {
                val backupViewModel: DataBackupViewModel = viewModel(
                    factory = DataBackupViewModel.factory(
                        backupRepository = app.backupRepository,
                        autoBackupRepository = app.autoBackupRepository,
                        appPreferences = app.appPreferences,
                    ),
                )
                DataSettingsScreen(
                    viewModel = backupViewModel,
                    bookName = booksState.active.name,
                    onBack = { navController.popBackStack() },
                )
            }
        }

        composable(Routes.Trends) {
            val viewModel: TrendsViewModel = viewModel(
                factory = TrendsViewModel.factory(repository),
            )
            TrendsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenEvent = { eventId ->
                    navController.navigate(Routes.eventDetail(eventId))
                },
            )
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
            EventDetailScreen(
                eventId = eventId,
                eventRepository = repository,
                onBack = { navController.popBackStack() },
                onEditEvent = { id -> navController.navigate(Routes.eventEdit(id)) },
                onAddEntry = { id -> navController.navigate(Routes.entryNew(id)) },
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

            if (tutorialState.active) {
                TutorialOverlay(
                    state = tutorialState,
                    targets = tutorialTargets.targets,
                    onBack = { tutorialViewModel.back() },
                    onNext = {
                        if (tutorialState.isLast) {
                            val wasFullTour = tutorialState.isFullTour
                            tutorialViewModel.finish()
                            leaveFullTourIfNeeded(wasFullTour)
                        } else {
                            tutorialViewModel.next()
                        }
                    },
                    onSkip = {
                        val wasFullTour = tutorialState.isFullTour
                        tutorialViewModel.skip()
                        leaveFullTourIfNeeded(wasFullTour)
                    },
                )
            }
        }
    }
}

private fun syncFullTourNavigation(
    navController: NavHostController,
    screen: TutorialScreen,
) {
    when (screen) {
        TutorialScreen.Home -> {
            if (!navController.isAt(Routes.Home)) {
                navController.popBackStack(Routes.Home, inclusive = false)
            }
        }
        TutorialScreen.Calendar -> {
            if (!navController.isAt(Routes.Calendar)) {
                navController.navigate(Routes.Calendar) {
                    popUpTo(Routes.Home)
                    launchSingleTop = true
                }
            }
        }
        TutorialScreen.Trends -> {
            if (!navController.isAt(Routes.Trends)) {
                navController.navigate(Routes.Trends) {
                    popUpTo(Routes.Home)
                    launchSingleTop = true
                }
            }
        }
        TutorialScreen.Settings -> {
            if (!navController.isAt(SettingsRoutes.Graph)) {
                navController.navigate(SettingsRoutes.Graph) {
                    popUpTo(Routes.Home)
                    launchSingleTop = true
                }
            }
        }
        TutorialScreen.EventDetail,
        TutorialScreen.StartEvent,
        TutorialScreen.EntryEditor,
        TutorialScreen.Data,
        -> Unit
    }
}

private fun NavHostController.isAt(route: String): Boolean {
    return currentDestination?.hierarchy?.any { it.route == route } == true
}

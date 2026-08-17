package com.maimonthlyhoppinings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maimonthlyhoppinings.ui.AppNav
import com.maimonthlyhoppinings.ui.theme.MaiMonthlyHoppiningsTheme
import com.maimonthlyhoppinings.ui.theme.ThemeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as MaiMonthlyHoppiningsApp
            val themeViewModel: ThemeViewModel = viewModel(
                factory = ThemeViewModel.factory(
                    themePreferences = app.themePreferences,
                    savedThemeRepository = app.savedThemeRepository,
                ),
            )
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
            val activePalette by themeViewModel.activePalette.collectAsStateWithLifecycle()

            MaiMonthlyHoppiningsTheme(
                themeMode = themeMode,
                palette = activePalette,
            ) {
                AppNav(themeViewModel = themeViewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val app = application as MaiMonthlyHoppiningsApp
        lifecycleScope.launch(Dispatchers.IO) {
            app.autoBackupRepository.runIfDue()
        }
    }
}

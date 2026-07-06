package com.mdnote.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.mdnote.app.ui.navigation.NavGraph
import com.mdnote.app.ui.theme.MdNoteTheme
import com.mdnote.app.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return SettingsViewModel(applicationContext) as T
                    }
                }
            )

            val themeMode by settingsViewModel.themeMode.collectAsState(initial = com.mdnote.app.ui.theme.ThemeMode.SYSTEM)
            val dynamicColor by settingsViewModel.dynamicColor.collectAsState(initial = true)

            MdNoteTheme(
                themeMode = themeMode,
                dynamicColor = dynamicColor
            ) {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
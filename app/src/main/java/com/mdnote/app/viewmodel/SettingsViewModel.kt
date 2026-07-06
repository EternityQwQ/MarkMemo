package com.mdnote.app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import com.mdnote.app.data.export.LogCollector
import com.mdnote.app.data.repository.SortOrder
import com.mdnote.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsViewModel(private val context: Context) : ViewModel() {

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_SORT_ORDER = stringPreferencesKey("sort_order")
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    }

    private val logCollector = LogCollector(context)

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        when (preferences[KEY_THEME_MODE]) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    val sortOrder: Flow<SortOrder> = context.dataStore.data.map { preferences ->
        when (preferences[KEY_SORT_ORDER]) {
            "CREATED_DESC" -> SortOrder.CREATED_DESC
            "TITLE_ASC" -> SortOrder.TITLE_ASC
            else -> SortOrder.UPDATED_DESC
        }
    }

    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DYNAMIC_COLOR] ?: true
    }

    fun getThemeModeSync(): ThemeMode = runBlocking { themeMode.first() }

    fun getSortOrderSync(): SortOrder = runBlocking { sortOrder.first() }

    fun getDynamicColorSync(): Boolean = runBlocking { dynamicColor.first() }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setSortOrder(order: SortOrder) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SORT_ORDER] = order.name
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DYNAMIC_COLOR] = enabled
        }
    }

    fun exportLogs(): Uri? {
        return logCollector.collectAndExportLogs()
    }

    fun shareLogs(uri: Uri) {
        logCollector.shareLogs(uri)
    }
}
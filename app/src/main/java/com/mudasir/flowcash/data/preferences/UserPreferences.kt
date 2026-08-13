package com.mudasir.flowcash.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

class UserPreferences(private val context: Context) {

    companion object {
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_CURRENCY = stringPreferencesKey("currency_symbol")
        private val KEY_CURRENCY_CODE = stringPreferencesKey("currency_code")
        private val KEY_BIOMETRICS_ENABLED = booleanPreferencesKey("biometrics_enabled")
        private val KEY_SELECTED_ACCOUNT_ID = stringPreferencesKey("selected_account_id")
        private val KEY_DAILY_REMINDER = booleanPreferencesKey("daily_reminder_enabled")
        private val KEY_WEEKLY_SUMMARY = booleanPreferencesKey("weekly_summary_enabled")
        private val KEY_SAVED_EMAIL = stringPreferencesKey("saved_email")
        private val KEY_REMEMBER_ME = booleanPreferencesKey("remember_me")
        private val KEY_DATA_VISIBLE = booleanPreferencesKey("is_data_visible")
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val modeStr = preferences[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(modeStr)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    val currencyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_CURRENCY] ?: "Rs"
    }

    val currencyCodeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_CURRENCY_CODE] ?: "PKR"
    }

    val biometricsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_BIOMETRICS_ENABLED] ?: false
    }

    val isDataVisibleFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DATA_VISIBLE] ?: true
    }

    val selectedAccountIdFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_ACCOUNT_ID] ?: ""
    }

    val dailyReminderFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DAILY_REMINDER] ?: true
    }

    val weeklySummaryFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_WEEKLY_SUMMARY] ?: true
    }

    val savedEmailFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SAVED_EMAIL] ?: ""
    }

    val rememberMeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_REMEMBER_ME] ?: true
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setCurrency(symbol: String, code: String = "") {
        context.dataStore.edit { preferences ->
            preferences[KEY_CURRENCY] = symbol
            if (code.isNotBlank()) {
                preferences[KEY_CURRENCY_CODE] = code
            }
        }
    }

    suspend fun setBiometricsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BIOMETRICS_ENABLED] = enabled
            if (!enabled) {
                preferences[KEY_DATA_VISIBLE] = true
            }
        }
    }

    suspend fun setDataVisible(visible: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DATA_VISIBLE] = visible
        }
    }

    suspend fun setSelectedAccountId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_ACCOUNT_ID] = id
        }
    }

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DAILY_REMINDER] = enabled
        }
    }

    suspend fun setWeeklySummaryEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_WEEKLY_SUMMARY] = enabled
        }
    }

    suspend fun saveRememberMe(remember: Boolean, email: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_REMEMBER_ME] = remember
            if (remember) {
                preferences[KEY_SAVED_EMAIL] = email
            } else {
                preferences[KEY_SAVED_EMAIL] = ""
            }
        }
    }

    suspend fun clearUserPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

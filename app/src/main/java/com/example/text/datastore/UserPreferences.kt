package com.example.text.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")
        val NICKNAME = stringPreferencesKey("nickname")
        val DISPLAY_MODE = stringPreferencesKey("display_mode")
        val REMINDER_DAYS = intPreferencesKey("reminder_days")
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
    }

    val nickname: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[NICKNAME] ?: ""
    }

    val displayMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DISPLAY_MODE] ?: "all"
    }

    val reminderDays: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[REMINDER_DAYS] ?: 3
    }

    val recentSearches: Flow<List<String>> = context.dataStore.data.map { preferences ->
        preferences[RECENT_SEARCHES]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun setNickname(value: String) {
        context.dataStore.edit { it[NICKNAME] = value }
    }

    suspend fun setDisplayMode(value: String) {
        context.dataStore.edit { it[DISPLAY_MODE] = value }
    }

    suspend fun setReminderDays(value: Int) {
        context.dataStore.edit { it[REMINDER_DAYS] = value }
    }

    suspend fun addRecentSearch(query: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[RECENT_SEARCHES]?.split(",")?.toMutableList() ?: mutableListOf()
            current.remove(query)
            current.add(0, query)
            if (current.size > 10) current.removeAt(current.lastIndex)
            preferences[RECENT_SEARCHES] = current.joinToString(",")
        }
    }
}

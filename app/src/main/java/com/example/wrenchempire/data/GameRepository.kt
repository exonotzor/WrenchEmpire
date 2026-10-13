package com.example.wrenchempire.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.wrenchempire.data.model.GameState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wrench_empire_save")

class GameRepository(private val context: Context) {

    companion object {
        private val KEY_GAME_STATE = stringPreferencesKey("game_state")
        private val KEY_LAST_CLOSE = longPreferencesKey("last_close_time")

        private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    }

    /** Emits the current GameState whenever it changes on disk. */
    val gameStateFlow: Flow<GameState> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_GAME_STATE] ?: return@map GameState()
        runCatching { json.decodeFromString<GameState>(raw) }.getOrDefault(GameState())
    }

    suspend fun saveGameState(state: GameState) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GAME_STATE] = json.encodeToString(state)
            prefs[KEY_LAST_CLOSE] = System.currentTimeMillis()
        }
    }

    /** Returns the epoch-ms timestamp of the last app close (for offline calc). */
    val lastCloseTimestamp: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_CLOSE] ?: System.currentTimeMillis()
    }

    suspend fun saveLastCloseTimestamp(ts: Long) {
        context.dataStore.edit { prefs -> prefs[KEY_LAST_CLOSE] = ts }
    }
}

package id.antasari.sumifyai.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appPreferencesDataStore by preferencesDataStore(name = "sumifyai_app_preferences")

class AppPreferencesManager(private val context: Context) {
    val hasSeenWelcome: Flow<Boolean> = context.appPreferencesDataStore.data.map { preferences ->
        preferences[HAS_SEEN_WELCOME] ?: false
    }

    val isDemoMode: Flow<Boolean> = context.appPreferencesDataStore.data.map { preferences ->
        preferences[IS_DEMO_MODE] ?: false
    }

    suspend fun setHasSeenWelcome(value: Boolean) {
        context.appPreferencesDataStore.edit { preferences ->
            preferences[HAS_SEEN_WELCOME] = value
        }
    }

    suspend fun setDemoMode(value: Boolean) {
        context.appPreferencesDataStore.edit { preferences ->
            preferences[IS_DEMO_MODE] = value
        }
    }

    private companion object {
        val HAS_SEEN_WELCOME = booleanPreferencesKey("has_seen_welcome")
        val IS_DEMO_MODE = booleanPreferencesKey("demo_mode_active")
    }
}

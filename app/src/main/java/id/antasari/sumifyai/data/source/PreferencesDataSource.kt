package id.antasari.sumifyai.data.source

import id.antasari.sumifyai.data.local.AppPreferencesManager
import kotlinx.coroutines.flow.Flow

interface PreferencesDataSource {
    val hasSeenWelcome: Flow<Boolean>
    val isDemoMode: Flow<Boolean>
    suspend fun setHasSeenWelcome(value: Boolean)
    suspend fun setDemoMode(value: Boolean)
}

class DataStorePreferencesDataSource(
    private val manager: AppPreferencesManager
) : PreferencesDataSource {
    override val hasSeenWelcome: Flow<Boolean> = manager.hasSeenWelcome
    override val isDemoMode: Flow<Boolean> = manager.isDemoMode

    override suspend fun setHasSeenWelcome(value: Boolean) {
        manager.setHasSeenWelcome(value)
    }

    override suspend fun setDemoMode(value: Boolean) {
        manager.setDemoMode(value)
    }
}

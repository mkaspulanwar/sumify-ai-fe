package id.antasari.sumifyai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.antasari.sumifyai.data.repository.MeetingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val repository: MeetingRepository
) : ViewModel() {
    val hasSeenWelcome: StateFlow<Boolean?> = repository.hasSeenWelcome
        .map<Boolean, Boolean?> { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun completeWelcome(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.setHasSeenWelcome(true)
            onComplete()
        }
    }
}

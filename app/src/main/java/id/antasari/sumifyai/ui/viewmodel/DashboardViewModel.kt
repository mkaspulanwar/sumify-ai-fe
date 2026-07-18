package id.antasari.sumifyai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.antasari.sumifyai.data.repository.MeetingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: MeetingRepository
) : ViewModel() {
    val history = repository.meetings
    val isDemoMode: StateFlow<Boolean> = repository.isDemoMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    init {
        loadMeetingsHistory()
    }

    fun loadMeetingsHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.refreshMeetings()
        }
    }

    fun toggleDemoMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDemoMode(enabled)
        }
    }

    fun deleteMeeting(meetingId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMeeting(meetingId)
        }
    }

    fun deleteAllMeetings() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.meetings.value.forEach { meeting ->
                repository.deleteMeeting(meeting.id)
            }
        }
    }

    fun toggleMeetingFavorite(meetingId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFavorite(meetingId)
        }
    }
}

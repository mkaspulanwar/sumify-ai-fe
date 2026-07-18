package id.antasari.sumifyai.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.antasari.sumifyai.data.audio.AudioInputManager
import id.antasari.sumifyai.data.audio.SelectedAudio
import id.antasari.sumifyai.data.repository.MeetingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateSummaryViewModel(
    private val repository: MeetingRepository,
    private val audioInputManager: AudioInputManager
) : ViewModel() {
    private val _selectedAudioName = MutableStateFlow<String?>(null)
    val selectedAudioName: StateFlow<String?> = _selectedAudioName.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordDurationSeconds = MutableStateFlow(0)
    val recordDurationSeconds: StateFlow<Int> = _recordDurationSeconds.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private var selectedAudio: SelectedAudio? = null
    private var recordTimerJob: Job? = null

    fun startRecording() {
        if (_isRecording.value) return
        clearSelectedFile()
        if (audioInputManager.startRecording()) {
            _isRecording.value = true
            _recordDurationSeconds.value = 0
            recordTimerJob = viewModelScope.launch {
                while (audioInputManager.isRecording) {
                    delay(1_000)
                    _recordDurationSeconds.value += 1
                }
            }
        } else {
            _message.value = "Failed to start audio recording"
        }
    }

    fun stopRecording() {
        if (!_isRecording.value) return
        recordTimerJob?.cancel()
        recordTimerJob = null
        _isRecording.value = false
        selectedAudio = audioInputManager.stopRecording(
            "Live Recording (${formatDuration(_recordDurationSeconds.value)})"
        )
        _selectedAudioName.value = selectedAudio?.displayName
    }

    fun cancelActiveRecording() {
        recordTimerJob?.cancel()
        recordTimerJob = null
        audioInputManager.cancelRecording()
        _isRecording.value = false
        _recordDurationSeconds.value = 0
    }

    fun selectAudioFile(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                cancelActiveRecording()
                audioInputManager.delete(selectedAudio)
                audioInputManager.copyFromUri(uri)
            }.onSuccess {
                selectedAudio = it
                _selectedAudioName.value = it.displayName
            }.onFailure {
                _message.value = it.localizedMessage ?: "Error processing selected audio file"
            }
        }
    }

    fun clearSelectedFile() {
        audioInputManager.delete(selectedAudio)
        selectedAudio = null
        _selectedAudioName.value = null
        _uploadState.value = UploadState.Idle
    }

    fun uploadAudio(
        title: String,
        description: String,
        language: String,
        onUploadSuccess: (String) -> Unit
    ) {
        val audio = selectedAudio
        if (audio == null || !audio.file.exists()) {
            _message.value = "Please select or record an audio first"
            return
        }
        if (title.isBlank()) {
            _message.value = "Title is required"
            return
        }

        _uploadState.value = UploadState.Uploading
        viewModelScope.launch {
            runCatching {
                val demoMode = repository.isDemoMode.first()
                withContext(Dispatchers.IO) {
                    repository.uploadMeeting(
                        file = audio.file,
                        fileName = audio.displayName,
                        title = title,
                        description = description,
                        language = language,
                        demoMode = demoMode
                    )
                } to demoMode
            }.onSuccess { (meeting, demoMode) ->
                _uploadState.value = UploadState.Success(meeting.id)
                onUploadSuccess(meeting.id)
                if (demoMode) {
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.advanceDemoMeeting(meeting.id)
                    }
                }
            }.onFailure {
                val error = it.localizedMessage ?: "Network error"
                _uploadState.value = UploadState.Error(error)
                _message.value = "Upload failed: $error"
            }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun formatDuration(seconds: Int): String =
        String.format("%02d:%02d", seconds / 60, seconds % 60)

    override fun onCleared() {
        recordTimerJob?.cancel()
        audioInputManager.release()
        super.onCleared()
    }
}

sealed interface UploadState {
    data object Idle : UploadState
    data object Uploading : UploadState
    data class Success(val meetingId: String) : UploadState
    data class Error(val message: String) : UploadState
}

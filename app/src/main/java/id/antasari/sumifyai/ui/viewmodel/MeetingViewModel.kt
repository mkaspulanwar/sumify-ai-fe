package id.antasari.sumifyai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.antasari.sumifyai.data.download.PdfDownloader
import id.antasari.sumifyai.data.repository.MeetingRepository
import id.antasari.sumifyai.domain.model.Meeting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MeetingViewModel(
    private val repository: MeetingRepository,
    private val pdfDownloader: PdfDownloader
) : ViewModel() {
    private val _activeMeeting = MutableStateFlow<Meeting?>(null)
    val activeMeeting: StateFlow<Meeting?> = _activeMeeting.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private var activeMeetingId: String? = null
    private var pollingJob: Job? = null

    init {
        viewModelScope.launch {
            repository.meetings.collect { meetings ->
                val id = activeMeetingId ?: return@collect
                meetings.find { it.id == id }?.let { _activeMeeting.value = it }
            }
        }
    }

    fun startPollingMeetingStatus(meetingId: String) {
        activeMeetingId = meetingId
        pollingJob?.cancel()
        _activeMeeting.value = repository.meetings.value.find { it.id == meetingId }
        pollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                runCatching {
                    repository.refreshMeeting(meetingId)
                }.onSuccess {
                    if (it != null) _activeMeeting.value = it
                }
                if (_activeMeeting.value?.status.isTerminalStatus()) break
                delay(if (meetingId.startsWith("mock_")) 1_500 else 3_000)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun cancelMeetingProcessing(meetingId: String) {
        stopPolling()
        viewModelScope.launch(Dispatchers.IO) {
            repository.cancelMeeting(meetingId)
            _activeMeeting.value = repository.meetings.value.find { it.id == meetingId }
        }
    }

    fun downloadPdf(meetingId: String, title: String, downloadUrl: String) {
        viewModelScope.launch {
            runCatching {
                pdfDownloader.enqueue(title, downloadUrl)
            }.onSuccess { download ->
                viewModelScope.launch(Dispatchers.IO) {
                    repository.savePdfDownload(
                        meetingId = meetingId,
                        downloadId = download.id,
                        fileName = download.fileName,
                        localUri = download.localUri
                    )
                }
                _message.value = "PDF Download started! Check notifications."
            }.onFailure {
                _message.value = "Failed to download PDF: ${it.localizedMessage}"
            }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    override fun onCleared() {
        stopPolling()
        super.onCleared()
    }

    private fun String?.isTerminalStatus(): Boolean =
        equals("completed", ignoreCase = true) ||
            equals("failed", ignoreCase = true) ||
            equals("cancelled", ignoreCase = true)
}

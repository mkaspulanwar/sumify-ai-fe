package id.antasari.sumifyai.ui.viewmodel

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.antasari.sumifyai.data.api.ApiConfig
import id.antasari.sumifyai.data.local.AppPreferencesManager
import id.antasari.sumifyai.data.local.LocalHistoryManager
import id.antasari.sumifyai.data.model.MeetingLocal
import id.antasari.sumifyai.utils.AudioRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val PREFS_NAME = "sumifyai_prefs"
    private val KEY_DEMO_MODE = "demo_mode_active"
    private val appPreferencesManager = AppPreferencesManager(context)

    // API Base URL config
    private val _apiBaseUrl = MutableStateFlow(ApiConfig.getBaseUrl(context))
    val apiBaseUrl: StateFlow<String> = _apiBaseUrl.asStateFlow()

    private val _hasSeenWelcome = MutableStateFlow<Boolean?>(null)
    val hasSeenWelcome: StateFlow<Boolean?> = _hasSeenWelcome.asStateFlow()

    // Demo Mode Configuration
    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _isDemoMode = MutableStateFlow(sharedPrefs.getBoolean(KEY_DEMO_MODE, false))
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    // Local History List
    private val _history = MutableStateFlow<List<MeetingLocal>>(emptyList())
    val history: StateFlow<List<MeetingLocal>> = _history.asStateFlow()

    // Live Audio Recording State
    private val audioRecorder = AudioRecorder(context)
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordDurationSeconds = MutableStateFlow(0)
    val recordDurationSeconds: StateFlow<Int> = _recordDurationSeconds.asStateFlow()

    private var recordTimerJob: Job? = null
    private var recordedFile: File? = null

    // Selected Audio File State
    private val _selectedAudioName = MutableStateFlow<String?>(null)
    val selectedAudioName: StateFlow<String?> = _selectedAudioName.asStateFlow()
    private var selectedAudioFile: File? = null

    // API Operation States
    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    // Polling / Detail State
    private val _activeMeeting = MutableStateFlow<MeetingLocal?>(null)
    val activeMeeting: StateFlow<MeetingLocal?> = _activeMeeting.asStateFlow()

    private var pollingJob: Job? = null

    init {
        loadMeetingsHistory()
        observeAppPreferences()
    }

    private fun observeAppPreferences() {
        viewModelScope.launch {
            appPreferencesManager.hasSeenWelcome.collect { hasSeenWelcome ->
                _hasSeenWelcome.value = hasSeenWelcome
            }
        }
    }

    fun completeWelcome(onComplete: () -> Unit) {
        viewModelScope.launch {
            appPreferencesManager.setHasSeenWelcome(true)
            _hasSeenWelcome.value = true
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun loadMeetingsHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = LocalHistoryManager.loadHistory(context)
            _history.value = list
        }
    }

    fun updateApiBaseUrl(newUrl: String) {
        ApiConfig.setBaseUrl(context, newUrl)
        _apiBaseUrl.value = ApiConfig.getBaseUrl(context)
        Toast.makeText(context, "API URL updated: ${_apiBaseUrl.value}", Toast.LENGTH_SHORT).show()
    }

    fun toggleDemoMode(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_DEMO_MODE, enabled).apply()
        _isDemoMode.value = enabled
        val stateText = if (enabled) "Demo Mode (Simulation) active" else "Online Mode active"
        Toast.makeText(context, stateText, Toast.LENGTH_SHORT).show()
    }

    // --- AUDIO RECORDING UTILITIES ---

    fun startRecording() {
        if (_isRecording.value) return

        val cacheFile = File(context.cacheDir, "recording_${System.currentTimeMillis()}.m4a")
        recordedFile = cacheFile

        val success = audioRecorder.start(cacheFile)
        if (success) {
            _isRecording.value = true
            _recordDurationSeconds.value = 0
            selectedAudioFile = null
            _selectedAudioName.value = null

            recordTimerJob = viewModelScope.launch(Dispatchers.Main) {
                while (audioRecorder.isRecording) {
                    delay(1000)
                    _recordDurationSeconds.value += 1
                }
            }
        } else {
            Toast.makeText(context, "Failed to start audio recording", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopRecording() {
        if (!_isRecording.value) return
        audioRecorder.stop()
        recordTimerJob?.cancel()
        recordTimerJob = null
        _isRecording.value = false
        
        recordedFile?.let {
            selectedAudioFile = it
            _selectedAudioName.value = "Live Recording (${formatDuration(_recordDurationSeconds.value)})"
        }
    }

    fun cancelActiveRecording() {
        if (_isRecording.value) {
            audioRecorder.stop()
            recordTimerJob?.cancel()
            recordTimerJob = null
            _isRecording.value = false
        }
        recordedFile?.delete()
        recordedFile = null
        _recordDurationSeconds.value = 0
    }

    // --- FILE SELECTION ---

    fun selectAudioFile(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val name = getFileName(context, uri) ?: "audio_file.mp3"
                _selectedAudioName.value = name

                val cacheFile = File(context.cacheDir, "selected_audio_${System.currentTimeMillis()}")
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(cacheFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                
                cancelActiveRecording()
                selectedAudioFile = cacheFile
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error processing selected audio file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun clearSelectedFile() {
        selectedAudioFile?.delete()
        selectedAudioFile = null
        _selectedAudioName.value = null
    }

    // --- UPLOAD & SIMULATION PIPELINES ---

    fun uploadAudio(title: String, description: String, language: String, onUploadSuccess: (String) -> Unit) {
        val fileToUpload = selectedAudioFile
        if (fileToUpload == null || !fileToUpload.exists()) {
            Toast.makeText(context, "Please select or record an audio first", Toast.LENGTH_SHORT).show()
            return
        }

        if (title.trim().isEmpty()) {
            Toast.makeText(context, "Title is required", Toast.LENGTH_SHORT).show()
            return
        }

        _uploadState.value = UploadState.Uploading

        if (_isDemoMode.value) {
            // Run Simulated Processing
            runSimulatedUpload(title, description, language, fileToUpload, onUploadSuccess)
        } else {
            // Run Real Network Call
            runRealNetworkUpload(title, description, language, fileToUpload, onUploadSuccess)
        }
    }

    private fun runSimulatedUpload(title: String, description: String, language: String, file: File, onUploadSuccess: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val mockId = "mock_" + UUID.randomUUID().toString().substring(0, 8)
            val localMeeting = MeetingLocal(
                id = mockId,
                title = title.trim(),
                description = description.trim(),
                language = language,
                status = "queued",
                localAudioPath = file.absolutePath
            )

            LocalHistoryManager.saveOrUpdateMeeting(context, localMeeting)
            loadMeetingsHistory()

            withContext(Dispatchers.Main) {
                _uploadState.value = UploadState.Success(mockId)
                clearSelectedFile()
                onUploadSuccess(mockId)
            }

            viewModelScope.launch(Dispatchers.IO) {
                delay(2000)
                LocalHistoryManager.updateMeetingStatus(context, mockId, "transcribing")

                delay(4000)
                LocalHistoryManager.updateMeetingStatus(context, mockId, "summarizing")

                delay(4000)
                LocalHistoryManager.updateMeetingStatus(context, mockId, "generating_pdf")

                delay(3000)

                val mockSummaryText = """
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit.
                    Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
                    Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris.
                    Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
                """.trimIndent()

                val mockTranscriptText = """
                    [00:02] Lorem ipsum dolor sit amet.
                    [00:15] Consectetur adipiscing elit sed do eiusmod tempor incididunt.
                    [00:40] Ut enim ad minim veniam quis nostrud exercitation.
                    [01:05] Ullamco laboris nisi ut aliquip ex ea commodo consequat.
                    [01:30] Duis aute irure dolor in reprehenderit.
                    [01:45] Excepteur sint occaecat cupidatat non proident.
                    [02:10] Sunt in culpa qui officia deserunt mollit anim id est laborum.
                """.trimIndent()

                LocalHistoryManager.updateMeetingStatus(
                    context = context,
                    id = mockId,
                    status = "completed",
                    downloadUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                    transcriptExcerpt = mockTranscriptText,
                    summaryText = mockSummaryText
                )

                loadMeetingsHistory()
            }
        }
    }
    private fun runRealNetworkUpload(title: String, description: String, language: String, file: File, onUploadSuccess: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val apiService = ApiConfig.getApiService(context)

                val requestFile = file.asRequestBody("audio/*".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                
                val titlePart = title.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                val descPart = description.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                val langPart = language.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = apiService.uploadAudio(
                    audio = filePart,
                    title = titlePart,
                    description = descPart,
                    language = langPart
                )

                val localMeeting = MeetingLocal(
                    id = response.id,
                    title = title.trim(),
                    description = description.trim(),
                    language = language,
                    status = response.status,
                    localAudioPath = file.absolutePath
                )
                
                LocalHistoryManager.saveOrUpdateMeeting(context, localMeeting)
                loadMeetingsHistory()

                withContext(Dispatchers.Main) {
                    _uploadState.value = UploadState.Success(response.id)
                    clearSelectedFile()
                    onUploadSuccess(response.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val errorMsg = e.localizedMessage ?: "Network error"
                withContext(Dispatchers.Main) {
                    _uploadState.value = UploadState.Error(errorMsg)
                    Toast.makeText(context, "Upload failed: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun startPollingMeetingStatus(meetingId: String) {
        pollingJob?.cancel()
        
        viewModelScope.launch(Dispatchers.IO) {
            val list = LocalHistoryManager.loadHistory(context)
            val existing = list.find { it.id == meetingId }
            _activeMeeting.value = existing
        }

        if (meetingId.startsWith("mock_")) {
            // Simulation polling: Just watch local history updates
            pollingJob = viewModelScope.launch(Dispatchers.IO) {
                var shouldPoll = true
                while (shouldPoll) {
                    delay(1500)
                    val list = LocalHistoryManager.loadHistory(context)
                    val updated = list.find { it.id == meetingId }
                    _activeMeeting.value = updated
                    
                    if (updated?.status?.lowercase() == "completed" || updated?.status?.lowercase() == "failed") {
                        shouldPoll = false
                    }
                }
            }
        } else {
            // Real network polling
            pollingJob = viewModelScope.launch(Dispatchers.IO) {
                var shouldPoll = true
                while (shouldPoll) {
                    try {
                        val apiService = ApiConfig.getApiService(context)
                        val details = apiService.getMeetingDetails(meetingId)

                        LocalHistoryManager.updateMeetingStatus(
                            context = context,
                            id = meetingId,
                            status = details.status,
                            downloadUrl = details.downloadUrl,
                            transcriptExcerpt = details.transcript,
                            summaryText = details.summary
                        )

                        val updatedMeeting = _activeMeeting.value?.copy(
                            status = details.status,
                            downloadUrl = details.downloadUrl,
                            transcriptExcerpt = details.transcript,
                            summaryText = details.summary
                        )
                        _activeMeeting.value = updatedMeeting
                        loadMeetingsHistory()

                        if (details.status.equals("completed", ignoreCase = true) || 
                            details.status.equals("failed", ignoreCase = true)) {
                            shouldPoll = false
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (shouldPoll) {
                        delay(3000)
                    }
                }
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun deleteMeeting(meetingId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            LocalHistoryManager.deleteMeeting(context, meetingId)
            loadMeetingsHistory()
        }
    }

    fun downloadPdf(meetingId: String, title: String, downloadUrl: String) {
        try {
            val fileName = "SumifyAI_${title.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                setTitle("Sumify AI - $title.pdf")
                setDescription("Downloading meeting summary PDF...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
                )
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)
            val localUri = "file://${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath}/$fileName"

            LocalHistoryManager.updateMeetingPdfDownload(
                context = context,
                id = meetingId,
                downloadId = downloadId,
                fileName = fileName,
                localUri = localUri
            )
            loadMeetingsHistory()
            _activeMeeting.value = _activeMeeting.value?.copy(
                pdfDownloadId = downloadId,
                pdfFileName = fileName,
                pdfLocalUri = localUri,
                pdfDownloadedAt = System.currentTimeMillis()
            )
            
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "PDF Download started! Check notifications.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Failed to download PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorder.stop()
        recordTimerJob?.cancel()
        pollingJob?.cancel()
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

    fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}

sealed interface UploadState {
    object Idle : UploadState
    object Uploading : UploadState
    data class Success(val meetingId: String) : UploadState
    data class Error(val message: String) : UploadState
}

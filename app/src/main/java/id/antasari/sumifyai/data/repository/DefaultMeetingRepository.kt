package id.antasari.sumifyai.data.repository

import id.antasari.sumifyai.data.source.MeetingLocalDataSource
import id.antasari.sumifyai.data.source.MeetingRemoteDataSource
import id.antasari.sumifyai.data.source.PreferencesDataSource
import id.antasari.sumifyai.domain.model.Meeting
import java.io.File
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DefaultMeetingRepository(
    private val localDataSource: MeetingLocalDataSource,
    private val remoteDataSource: MeetingRemoteDataSource,
    private val preferencesDataSource: PreferencesDataSource
) : MeetingRepository {
    private val storageMutex = Mutex()
    private val _meetings = MutableStateFlow<List<Meeting>>(emptyList())

    override val meetings: StateFlow<List<Meeting>> = _meetings.asStateFlow()
    override val hasSeenWelcome: Flow<Boolean> = preferencesDataSource.hasSeenWelcome
    override val isDemoMode: Flow<Boolean> = preferencesDataSource.isDemoMode

    override suspend fun setHasSeenWelcome(value: Boolean) {
        preferencesDataSource.setHasSeenWelcome(value)
    }

    override suspend fun setDemoMode(value: Boolean) {
        preferencesDataSource.setDemoMode(value)
    }

    override suspend fun refreshMeetings() {
        storageMutex.withLock {
            _meetings.value = localDataSource.loadMeetings()
        }
    }

    override suspend fun uploadMeeting(
        file: File,
        fileName: String,
        title: String,
        description: String,
        language: String,
        demoMode: Boolean
    ): Meeting {
        val response = if (demoMode) {
            "mock_${UUID.randomUUID().toString().substring(0, 8)}" to "queued"
        } else {
            remoteDataSource.uploadAudio(file, title, description, language).let {
                it.id to it.status
            }
        }

        val meeting = Meeting(
            id = response.first,
            title = title.trim(),
            description = description.trim(),
            language = language,
            status = response.second,
            localAudioPath = file.absolutePath,
            audioFileName = fileName,
            audioFileSizeBytes = file.length()
        )

        storageMutex.withLock {
            localDataSource.saveOrUpdate(meeting)
            _meetings.value = localDataSource.loadMeetings()
        }
        return meeting
    }

    override suspend fun advanceDemoMeeting(meetingId: String) {
        delay(2_000)
        updateStatus(meetingId, "transcribing")
        delay(4_000)
        updateStatus(meetingId, "summarizing")
        delay(4_000)
        updateStatus(meetingId, "generating_pdf")
        delay(3_000)
        updateStatus(
            meetingId = meetingId,
            status = "completed",
            downloadUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
            transcript = """
                [00:02] Lorem ipsum dolor sit amet.
                [00:15] Consectetur adipiscing elit sed do eiusmod tempor incididunt.
                [00:40] Ut enim ad minim veniam quis nostrud exercitation.
                [01:05] Ullamco laboris nisi ut aliquip ex ea commodo consequat.
                [01:30] Duis aute irure dolor in reprehenderit.
                [01:45] Excepteur sint occaecat cupidatat non proident.
                [02:10] Sunt in culpa qui officia deserunt mollit anim id est laborum.
            """.trimIndent(),
            summary = """
                Lorem ipsum dolor sit amet, consectetur adipiscing elit.
                Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
                Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris.
                Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
            """.trimIndent()
        )
    }

    override suspend fun refreshMeeting(meetingId: String): Meeting? {
        val current = storageMutex.withLock {
            localDataSource.loadMeetings().find { it.id == meetingId }
        }
        if (meetingId.startsWith("mock_") || current?.status.isTerminalStatus()) {
            refreshMeetings()
            return meetings.value.find { it.id == meetingId }
        }

        val details = remoteDataSource.getMeetingDetails(meetingId)
        updateStatus(
            meetingId = meetingId,
            status = details.status,
            downloadUrl = details.downloadUrl,
            transcript = details.transcript,
            summary = details.summary
        )
        return meetings.value.find { it.id == meetingId }
    }

    override suspend fun cancelMeeting(meetingId: String) {
        updateStatus(meetingId, "cancelled")
    }

    override suspend fun deleteMeeting(meetingId: String) {
        storageMutex.withLock {
            localDataSource.delete(meetingId)
            _meetings.value = localDataSource.loadMeetings()
        }
    }

    override suspend fun toggleFavorite(meetingId: String) {
        storageMutex.withLock {
            val meeting = localDataSource.loadMeetings().find { it.id == meetingId }
                ?: return@withLock
            localDataSource.updateFavorite(meetingId, !meeting.isFavorite)
            _meetings.value = localDataSource.loadMeetings()
        }
    }

    override suspend fun savePdfDownload(
        meetingId: String,
        downloadId: Long,
        fileName: String,
        localUri: String
    ) {
        storageMutex.withLock {
            localDataSource.updatePdfDownload(meetingId, downloadId, fileName, localUri)
            _meetings.value = localDataSource.loadMeetings()
        }
    }

    private suspend fun updateStatus(
        meetingId: String,
        status: String,
        downloadUrl: String? = null,
        transcript: String? = null,
        summary: String? = null
    ) {
        storageMutex.withLock {
            localDataSource.updateStatus(
                id = meetingId,
                status = status,
                downloadUrl = downloadUrl,
                transcript = transcript,
                summary = summary
            )
            _meetings.value = localDataSource.loadMeetings()
        }
    }

    private fun String?.isTerminalStatus(): Boolean =
        equals("completed", ignoreCase = true) ||
            equals("failed", ignoreCase = true) ||
            equals("cancelled", ignoreCase = true)
}

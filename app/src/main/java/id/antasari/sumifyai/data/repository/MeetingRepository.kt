package id.antasari.sumifyai.data.repository

import id.antasari.sumifyai.domain.model.Meeting
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MeetingRepository {
    val meetings: StateFlow<List<Meeting>>
    val hasSeenWelcome: Flow<Boolean>
    val isDemoMode: Flow<Boolean>

    suspend fun setHasSeenWelcome(value: Boolean)
    suspend fun setDemoMode(value: Boolean)
    suspend fun refreshMeetings()
    suspend fun uploadMeeting(
        file: File,
        fileName: String,
        title: String,
        description: String,
        language: String,
        demoMode: Boolean
    ): Meeting
    suspend fun advanceDemoMeeting(meetingId: String)
    suspend fun refreshMeeting(meetingId: String): Meeting?
    suspend fun cancelMeeting(meetingId: String)
    suspend fun deleteMeeting(meetingId: String)
    suspend fun toggleFavorite(meetingId: String)
    suspend fun savePdfDownload(
        meetingId: String,
        downloadId: Long,
        fileName: String,
        localUri: String
    )
}

package id.antasari.sumifyai.data.repository

import id.antasari.sumifyai.data.model.remote.MeetingCreateResponse
import id.antasari.sumifyai.data.model.remote.MeetingDetailResponse
import id.antasari.sumifyai.data.source.MeetingLocalDataSource
import id.antasari.sumifyai.data.source.MeetingRemoteDataSource
import id.antasari.sumifyai.data.source.PreferencesDataSource
import id.antasari.sumifyai.domain.model.Meeting
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultMeetingRepositoryTest {
    private val local = FakeMeetingLocalDataSource()
    private val remote = FakeMeetingRemoteDataSource()
    private val preferences = FakePreferencesDataSource()
    private val repository = DefaultMeetingRepository(local, remote, preferences)

    @Test
    fun refreshMeetingsPublishesLocalData() = runTest {
        local.meetings += meeting(id = "meeting-1")

        repository.refreshMeetings()

        assertEquals("meeting-1", repository.meetings.value.single().id)
    }

    @Test
    fun demoUploadDoesNotCallRemoteDataSource() = runTest {
        val audio = File.createTempFile("sumify-test", ".m4a")

        val result = repository.uploadMeeting(
            file = audio,
            fileName = "recording.m4a",
            title = "Weekly sync",
            description = "Project update",
            language = "id",
            demoMode = true
        )

        assertTrue(result.id.startsWith("mock_"))
        assertEquals(result, repository.meetings.value.single())
        assertFalse(remote.uploadCalled)
        audio.delete()
    }

    @Test
    fun toggleFavoriteUpdatesPublishedMeeting() = runTest {
        local.meetings += meeting(id = "meeting-2")
        repository.refreshMeetings()

        repository.toggleFavorite("meeting-2")

        assertTrue(repository.meetings.value.single().isFavorite)
    }

    private fun meeting(id: String) = Meeting(
        id = id,
        title = "Title",
        description = "Description",
        language = "id",
        status = "queued"
    )
}

private class FakeMeetingLocalDataSource : MeetingLocalDataSource {
    val meetings = mutableListOf<Meeting>()

    override fun loadMeetings(): List<Meeting> = meetings.toList()

    override fun saveOrUpdate(meeting: Meeting) {
        val index = meetings.indexOfFirst { it.id == meeting.id }
        if (index == -1) meetings.add(0, meeting) else meetings[index] = meeting
    }

    override fun updateStatus(
        id: String,
        status: String,
        downloadUrl: String?,
        transcript: String?,
        summary: String?
    ) {
        val index = meetings.indexOfFirst { it.id == id }
        if (index != -1) {
            meetings[index] = meetings[index].copy(
                status = status,
                downloadUrl = downloadUrl ?: meetings[index].downloadUrl,
                transcriptExcerpt = transcript ?: meetings[index].transcriptExcerpt,
                summaryText = summary ?: meetings[index].summaryText
            )
        }
    }

    override fun updateFavorite(id: String, isFavorite: Boolean) {
        val index = meetings.indexOfFirst { it.id == id }
        if (index != -1) meetings[index] = meetings[index].copy(isFavorite = isFavorite)
    }

    override fun updatePdfDownload(
        id: String,
        downloadId: Long,
        fileName: String,
        localUri: String
    ) = Unit

    override fun delete(id: String) {
        meetings.removeAll { it.id == id }
    }
}

private class FakeMeetingRemoteDataSource : MeetingRemoteDataSource {
    var uploadCalled = false

    override suspend fun uploadAudio(
        file: File,
        title: String,
        description: String,
        language: String
    ): MeetingCreateResponse {
        uploadCalled = true
        return MeetingCreateResponse("remote-1", "queued")
    }

    override suspend fun getMeetingDetails(meetingId: String): MeetingDetailResponse =
        MeetingDetailResponse(
            id = meetingId,
            title = "Title",
            description = null,
            language = "id",
            status = "completed",
            downloadUrl = null,
            createdAt = null,
            transcript = null,
            summary = null
        )
}

private class FakePreferencesDataSource : PreferencesDataSource {
    override val hasSeenWelcome: Flow<Boolean> = MutableStateFlow(false)
    override val isDemoMode: Flow<Boolean> = MutableStateFlow(false)

    override suspend fun setHasSeenWelcome(value: Boolean) = Unit
    override suspend fun setDemoMode(value: Boolean) = Unit
}

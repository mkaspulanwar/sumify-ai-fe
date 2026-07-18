package id.antasari.sumifyai.ui.viewmodel

import id.antasari.sumifyai.data.repository.MeetingRepository
import id.antasari.sumifyai.domain.model.Meeting
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun completeWelcomeUpdatesStateAndInvokesCallback() = runTest(dispatcher) {
        val repository = FakeMeetingRepository()
        val viewModel = OnboardingViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.hasSeenWelcome.collect {}
        }
        advanceUntilIdle()
        var callbackInvoked = false

        viewModel.completeWelcome { callbackInvoked = true }
        advanceUntilIdle()

        assertEquals(true, viewModel.hasSeenWelcome.value)
        assertTrue(callbackInvoked)
    }
}

private class FakeMeetingRepository : MeetingRepository {
    override val meetings: StateFlow<List<Meeting>> = MutableStateFlow(emptyList())
    private val welcome = MutableStateFlow(false)
    override val hasSeenWelcome: Flow<Boolean> = welcome
    override val isDemoMode: Flow<Boolean> = MutableStateFlow(false)

    override suspend fun setHasSeenWelcome(value: Boolean) {
        welcome.value = value
    }

    override suspend fun setDemoMode(value: Boolean) = Unit
    override suspend fun refreshMeetings() = Unit

    override suspend fun uploadMeeting(
        file: File,
        fileName: String,
        title: String,
        description: String,
        language: String,
        demoMode: Boolean
    ): Meeting = error("Not used")

    override suspend fun advanceDemoMeeting(meetingId: String) = Unit
    override suspend fun refreshMeeting(meetingId: String): Meeting? = null
    override suspend fun cancelMeeting(meetingId: String) = Unit
    override suspend fun deleteMeeting(meetingId: String) = Unit
    override suspend fun toggleFavorite(meetingId: String) = Unit

    override suspend fun savePdfDownload(
        meetingId: String,
        downloadId: Long,
        fileName: String,
        localUri: String
    ) = Unit
}

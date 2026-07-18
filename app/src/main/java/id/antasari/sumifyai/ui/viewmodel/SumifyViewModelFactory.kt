package id.antasari.sumifyai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.antasari.sumifyai.AppContainer

class SumifyViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(OnboardingViewModel::class.java) ->
            OnboardingViewModel(container.meetingRepository)
        modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
            DashboardViewModel(container.meetingRepository)
        modelClass.isAssignableFrom(CreateSummaryViewModel::class.java) ->
            CreateSummaryViewModel(
                container.meetingRepository,
                container.createAudioInputManager()
            )
        modelClass.isAssignableFrom(MeetingViewModel::class.java) ->
            MeetingViewModel(container.meetingRepository, container.pdfDownloader)
        else -> error("Unknown ViewModel class: ${modelClass.name}")
    } as T
}

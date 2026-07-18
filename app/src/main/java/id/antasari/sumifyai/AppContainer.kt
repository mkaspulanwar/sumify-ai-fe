package id.antasari.sumifyai

import android.content.Context
import id.antasari.sumifyai.data.api.ApiConfig
import id.antasari.sumifyai.data.audio.AudioInputManager
import id.antasari.sumifyai.data.download.PdfDownloader
import id.antasari.sumifyai.data.local.AppPreferencesManager
import id.antasari.sumifyai.data.repository.DefaultMeetingRepository
import id.antasari.sumifyai.data.repository.MeetingRepository
import id.antasari.sumifyai.data.source.DataStorePreferencesDataSource
import id.antasari.sumifyai.data.source.JsonMeetingLocalDataSource
import id.antasari.sumifyai.data.source.RetrofitMeetingRemoteDataSource

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val meetingRepository: MeetingRepository by lazy {
        DefaultMeetingRepository(
            localDataSource = JsonMeetingLocalDataSource(appContext),
            remoteDataSource = RetrofitMeetingRemoteDataSource(ApiConfig.getApiService()),
            preferencesDataSource = DataStorePreferencesDataSource(
                AppPreferencesManager(appContext)
            )
        )
    }

    fun createAudioInputManager(): AudioInputManager = AudioInputManager(appContext)

    val pdfDownloader: PdfDownloader by lazy {
        PdfDownloader(appContext)
    }
}

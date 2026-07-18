package id.antasari.sumifyai.data.source

import id.antasari.sumifyai.data.api.SumifyApiService
import id.antasari.sumifyai.data.model.remote.MeetingCreateResponse
import id.antasari.sumifyai.data.model.remote.MeetingDetailResponse
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

interface MeetingRemoteDataSource {
    suspend fun uploadAudio(
        file: File,
        title: String,
        description: String,
        language: String
    ): MeetingCreateResponse

    suspend fun getMeetingDetails(meetingId: String): MeetingDetailResponse
}

class RetrofitMeetingRemoteDataSource(
    private val apiService: SumifyApiService
) : MeetingRemoteDataSource {
    override suspend fun uploadAudio(
        file: File,
        title: String,
        description: String,
        language: String
    ): MeetingCreateResponse {
        val requestFile = file.asRequestBody("audio/*".toMediaTypeOrNull())
        return apiService.uploadAudio(
            audio = MultipartBody.Part.createFormData("file", file.name, requestFile),
            title = title.toRequestBody("text/plain".toMediaTypeOrNull()),
            description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
            language = language.toRequestBody("text/plain".toMediaTypeOrNull())
        )
    }

    override suspend fun getMeetingDetails(meetingId: String): MeetingDetailResponse =
        apiService.getMeetingDetails(meetingId)
}

package id.antasari.sumifyai.data.api

import id.antasari.sumifyai.data.model.remote.MeetingCreateResponse
import id.antasari.sumifyai.data.model.remote.MeetingDetailResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface SumifyApiService {

    @Multipart
    @POST("meetings")
    suspend fun uploadAudio(
        @Part audio: MultipartBody.Part,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("language") language: RequestBody?
    ): MeetingCreateResponse

    @GET("meetings/{id}")
    suspend fun getMeetingDetails(
        @Path("id") meetingId: String
    ): MeetingDetailResponse
}

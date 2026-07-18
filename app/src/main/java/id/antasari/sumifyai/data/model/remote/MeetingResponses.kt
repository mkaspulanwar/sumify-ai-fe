package id.antasari.sumifyai.data.model.remote

import com.google.gson.annotations.SerializedName

data class MeetingCreateResponse(
    @SerializedName("id") val id: String,
    @SerializedName("status") val status: String
)

data class MeetingDetailResponse(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("language") val language: String?,
    @SerializedName("status") val status: String,
    @SerializedName("download_url") val downloadUrl: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("transcript") val transcript: String?,
    @SerializedName("summary") val summary: String?
)

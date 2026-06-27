package id.antasari.sumifyai.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response returned when a meeting upload request is successful.
 */
data class MeetingCreateResponse(
    @SerializedName("id") val id: String,
    @SerializedName("status") val status: String
)

/**
 * Response returned when querying meeting details or status.
 */
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

/**
 * Local representation of a meeting for persisting summary history locally.
 */
data class MeetingLocal(
    val id: String,
    val title: String,
    val description: String,
    val language: String,
    var status: String,
    var downloadUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val localAudioPath: String? = null,
    val audioFileName: String? = null,
    val audioFileSizeBytes: Long? = null,
    var transcriptExcerpt: String? = null,
    var summaryText: String? = null,
    var pdfDownloadId: Long? = null,
    var pdfFileName: String? = null,
    var pdfLocalUri: String? = null,
    var pdfDownloadedAt: Long? = null,
    val isFavorite: Boolean = false
)

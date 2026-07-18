package id.antasari.sumifyai.domain.model

data class Meeting(
    val id: String,
    val title: String,
    val description: String,
    val language: String,
    val status: String,
    val downloadUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val localAudioPath: String? = null,
    val audioFileName: String? = null,
    val audioFileSizeBytes: Long? = null,
    val transcriptExcerpt: String? = null,
    val summaryText: String? = null,
    val pdfDownloadId: Long? = null,
    val pdfFileName: String? = null,
    val pdfLocalUri: String? = null,
    val pdfDownloadedAt: Long? = null,
    val isFavorite: Boolean = false
)

package id.antasari.sumifyai.data.source

import android.content.Context
import id.antasari.sumifyai.data.local.LocalHistoryManager
import id.antasari.sumifyai.domain.model.Meeting

interface MeetingLocalDataSource {
    fun loadMeetings(): List<Meeting>
    fun saveOrUpdate(meeting: Meeting)
    fun updateStatus(
        id: String,
        status: String,
        downloadUrl: String? = null,
        transcript: String? = null,
        summary: String? = null
    )
    fun updateFavorite(id: String, isFavorite: Boolean)
    fun updatePdfDownload(id: String, downloadId: Long, fileName: String, localUri: String)
    fun delete(id: String)
}

class JsonMeetingLocalDataSource(
    private val context: Context
) : MeetingLocalDataSource {
    override fun loadMeetings(): List<Meeting> =
        LocalHistoryManager.loadHistory(context)

    override fun saveOrUpdate(meeting: Meeting) {
        LocalHistoryManager.saveOrUpdateMeeting(context, meeting)
    }

    override fun updateStatus(
        id: String,
        status: String,
        downloadUrl: String?,
        transcript: String?,
        summary: String?
    ) {
        LocalHistoryManager.updateMeetingStatus(
            context = context,
            id = id,
            status = status,
            downloadUrl = downloadUrl,
            transcriptExcerpt = transcript,
            summaryText = summary
        )
    }

    override fun updateFavorite(id: String, isFavorite: Boolean) {
        LocalHistoryManager.updateMeetingFavorite(context, id, isFavorite)
    }

    override fun updatePdfDownload(
        id: String,
        downloadId: Long,
        fileName: String,
        localUri: String
    ) {
        LocalHistoryManager.updateMeetingPdfDownload(
            context = context,
            id = id,
            downloadId = downloadId,
            fileName = fileName,
            localUri = localUri
        )
    }

    override fun delete(id: String) {
        LocalHistoryManager.deleteMeeting(context, id)
    }
}

package id.antasari.sumifyai.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import id.antasari.sumifyai.data.model.MeetingLocal
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object LocalHistoryManager {
    private const val FILE_NAME = "sumify_meetings_history.json"
    private val gson = Gson()

    private fun getFile(context: Context): File {
        return File(context.filesDir, FILE_NAME)
    }

    /**
     * Loads the list of saved meetings from local storage.
     */
    fun loadHistory(context: Context): List<MeetingLocal> {
        val file = getFile(context)
        if (!file.exists()) {
            return emptyList()
        }
        return try {
            FileReader(file).use { reader ->
                val type = object : TypeToken<List<MeetingLocal>>() {}.type
                gson.fromJson(reader, type) ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Saves the list of meetings to local storage.
     */
    fun saveHistory(context: Context, meetings: List<MeetingLocal>) {
        val file = getFile(context)
        try {
            FileWriter(file).use { writer ->
                gson.toJson(meetings, writer)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Adds a new meeting to the history. If a meeting with the same ID already exists, it is updated.
     */
    fun saveOrUpdateMeeting(context: Context, meeting: MeetingLocal) {
        val currentHistory = loadHistory(context).toMutableList()
        val index = currentHistory.indexOfFirst { it.id == meeting.id }
        if (index != -1) {
            currentHistory[index] = meeting.copy(isFavorite = currentHistory[index].isFavorite)
        } else {
            // Prepend new meetings to show the latest first
            currentHistory.add(0, meeting)
        }
        saveHistory(context, currentHistory)
    }

    /**
     * Updates the status of an existing meeting in the history.
     */
    fun updateMeetingStatus(context: Context, id: String, status: String, downloadUrl: String? = null, transcriptExcerpt: String? = null, summaryText: String? = null) {
        val currentHistory = loadHistory(context).toMutableList()
        val index = currentHistory.indexOfFirst { it.id == id }
        if (index != -1) {
            val meeting = currentHistory[index]
            val updated = meeting.copy(
                status = status,
                downloadUrl = downloadUrl ?: meeting.downloadUrl,
                transcriptExcerpt = transcriptExcerpt ?: meeting.transcriptExcerpt,
                summaryText = summaryText ?: meeting.summaryText
            )
            currentHistory[index] = updated
            saveHistory(context, currentHistory)
        }
    }

    fun updateMeetingPdfDownload(
        context: Context,
        id: String,
        downloadId: Long,
        fileName: String,
        localUri: String,
        downloadedAt: Long = System.currentTimeMillis()
    ) {
        val currentHistory = loadHistory(context).toMutableList()
        val index = currentHistory.indexOfFirst { it.id == id }
        if (index != -1) {
            val meeting = currentHistory[index]
            val updated = meeting.copy(
                pdfDownloadId = downloadId,
                pdfFileName = fileName,
                pdfLocalUri = localUri,
                pdfDownloadedAt = downloadedAt
            )
            currentHistory[index] = updated
            saveHistory(context, currentHistory)
        }
    }

    fun updateMeetingFavorite(context: Context, id: String, isFavorite: Boolean) {
        val currentHistory = loadHistory(context).toMutableList()
        val index = currentHistory.indexOfFirst { it.id == id }
        if (index != -1) {
            val meeting = currentHistory[index]
            currentHistory[index] = meeting.copy(isFavorite = isFavorite)
            saveHistory(context, currentHistory)
        }
    }

    /**
     * Deletes a meeting from the local history list.
     */
    fun deleteMeeting(context: Context, id: String) {
        val currentHistory = loadHistory(context).filterNot { it.id == id }
        saveHistory(context, currentHistory)
    }
}

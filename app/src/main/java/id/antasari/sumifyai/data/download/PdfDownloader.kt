package id.antasari.sumifyai.data.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

data class PdfDownload(
    val id: Long,
    val fileName: String,
    val localUri: String
)

class PdfDownloader(
    private val context: Context
) {
    fun enqueue(title: String, downloadUrl: String): PdfDownload {
        val fileName = "SumifyAI_${title.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle("Sumify AI - $title.pdf")
            setDescription("Downloading meeting summary PDF...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = manager.enqueue(request)
        val directory = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        return PdfDownload(
            id = downloadId,
            fileName = fileName,
            localUri = "file://${directory.absolutePath}/$fileName"
        )
    }
}

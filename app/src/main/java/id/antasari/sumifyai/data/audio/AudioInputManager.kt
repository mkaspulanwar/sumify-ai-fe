package id.antasari.sumifyai.data.audio

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import id.antasari.sumifyai.utils.AudioRecorder
import java.io.File
import java.io.FileOutputStream

data class SelectedAudio(
    val file: File,
    val displayName: String
)

class AudioInputManager(
    private val context: Context,
    private val recorder: AudioRecorder = AudioRecorder(context)
) {
    private var recordedFile: File? = null

    val isRecording: Boolean
        get() = recorder.isRecording

    fun startRecording(): Boolean {
        if (recorder.isRecording) return false
        val output = File(context.cacheDir, "recording_${System.currentTimeMillis()}.m4a")
        recordedFile = output
        return recorder.start(output)
    }

    fun stopRecording(displayName: String): SelectedAudio? {
        if (!recorder.isRecording) return null
        recorder.stop()
        return recordedFile?.let { SelectedAudio(it, displayName) }
    }

    fun cancelRecording() {
        recorder.stop()
        recordedFile?.delete()
        recordedFile = null
    }

    fun copyFromUri(uri: Uri): SelectedAudio {
        val displayName = queryDisplayName(uri) ?: "audio_file.mp3"
        val output = File(context.cacheDir, "selected_audio_${System.currentTimeMillis()}")
        val input = requireNotNull(context.contentResolver.openInputStream(uri)) {
            "Unable to open the selected audio file."
        }
        input.use { inputStream ->
            FileOutputStream(output).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return SelectedAudio(output, displayName)
    }

    fun delete(selection: SelectedAudio?) {
        selection?.file?.delete()
    }

    fun release() {
        recorder.stop()
    }

    private fun queryDisplayName(uri: Uri): String? {
        if (uri.scheme != "content") return uri.lastPathSegment
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index == -1) null else cursor.getString(index)
        }
    }
}

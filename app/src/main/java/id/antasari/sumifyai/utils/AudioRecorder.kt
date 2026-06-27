package id.antasari.sumifyai.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

class AudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    var isRecording = false
        private set

    /**
     * Starts recording audio and saves it to the specified file.
     */
    fun start(outputFile: File): Boolean {
        if (isRecording) return false

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(outputFile.absolutePath)
        }

        return try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            isRecording = true
            true
        } catch (e: IOException) {
            e.printStackTrace()
            releaseRecorder()
            false
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            releaseRecorder()
            false
        }
    }

    /**
     * Stops the active recording.
     */
    fun stop() {
        if (!isRecording) return

        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            releaseRecorder()
        }
    }

    private fun releaseRecorder() {
        mediaRecorder?.reset()
        mediaRecorder?.release()
        mediaRecorder = null
        isRecording = false
    }
}

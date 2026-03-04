package org.monogram.presentation.chatsScreen.currentChat.components.inputbar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.File
import kotlin.math.log10

@Composable
fun rememberVoiceRecorder(
    onRecordingFinished: (String, Int, ByteArray) -> Unit
): VoiceRecorderState {
    val context = LocalContext.current
    val state = remember { VoiceRecorderState(context) }

    DisposableEffect(Unit) {
        onDispose {
            state.stopRecording(cancel = true)
        }
    }

    return state.apply {
        this.onRecordingFinished = onRecordingFinished
    }
}

class VoiceRecorderState(private val context: Context) {
    var isRecording by mutableStateOf(false)
        private set
    var isLocked by mutableStateOf(false)
        private set
    var durationMillis by mutableLongStateOf(0L)
        private set
    var amplitude by mutableFloatStateOf(0f)
        private set

    val waveform = mutableStateListOf<Byte>()

    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var startTime = 0L

    var onRecordingFinished: ((String, Int, ByteArray) -> Unit)? = null

    @Suppress("DEPRECATION")
    fun startRecording() {
        if (isRecording) return

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Request permission
            return
        }

        try {
            // OGG/OPUS is only natively supported from Android 10 (API 29)
            val supportsOggOpus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            val extension = if (supportsOggOpus) "ogg" else "m4a"

            val file = File(context.cacheDir, "voice_note_${System.currentTimeMillis()}.$extension")
            currentFile = file
            waveform.clear()

            // MediaRecorder(Context) is API 31+, fallback to parameterless constructor for older APIs
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)

                if (supportsOggOpus) {
                    setOutputFormat(MediaRecorder.OutputFormat.OGG)
                    setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
                    setAudioEncodingBitRate(320000)
                    setAudioSamplingRate(48000)
                } else {
                    // Fallback to AAC in an MP4 container for Android 6.0 - 9.0
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioEncodingBitRate(320000) // 320 kbps
                    setAudioSamplingRate(48000) // 48 kHz
                }

                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            startTime = System.currentTimeMillis()
            isRecording = true
            isLocked = false
            durationMillis = 0
        } catch (e: Exception) {
            e.printStackTrace()
            releaseResources()
            isRecording = false
        }
    }

    private fun releaseResources() {
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            // Ignore: stop() can fail if called too soon after start()
        }
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            // Ignore
        }
        mediaRecorder = null
    }

    fun lockRecording() {
        if (isRecording) {
            isLocked = true
        }
    }

    fun stopRecording(cancel: Boolean = false) {
        val wasRecording = isRecording
        val capturedDurationMillis = durationMillis

        releaseResources()
        isRecording = false
        isLocked = false

        if (wasRecording && !cancel && currentFile != null) {
            val durationSec = (capturedDurationMillis / 1000).toInt()
            if (durationSec >= 1) {
                onRecordingFinished?.invoke(currentFile!!.absolutePath, durationSec, waveform.toByteArray())
            } else {
                currentFile?.delete()
            }
        } else {
            currentFile?.delete()
        }
        currentFile = null
    }

    @Composable
    fun UpdateLoop() {
        LaunchedEffect(isRecording) {
            while (isActive && isRecording) {
                durationMillis = System.currentTimeMillis() - startTime

                val maxAmp = try {
                    mediaRecorder?.maxAmplitude ?: 0
                } catch (e: Exception) {
                    0
                }
                
                amplitude = if (maxAmp > 0) {
                    (20 * log10(maxAmp.toDouble() / 32767.0)).toFloat().coerceIn(-60f, 0f)
                } else -60f

                // Map -60..0 to 0..31 for TDLib waveform
                val normalized = ((amplitude + 60) / 60 * 31).toInt().coerceIn(0, 31)
                waveform.add(normalized.toByte())

                delay(100)
            }
        }
    }
}
package uni.matilde.lam01.core.util.recorder

import java.io.File

interface AudioRecorder {
    fun startRecording(outputFile: File)
    fun stop()
}
package uni.matilde.lam01.util.recorder

import java.io.File

interface AudioRecorder {
    fun startRecording(outputFile: File)
    fun stop()
}
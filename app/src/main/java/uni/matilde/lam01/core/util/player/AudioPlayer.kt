package uni.matilde.lam01.core.util.player

import java.io.File

interface AudioPlayer {

    fun playFile(file: File)
    fun stop()
}
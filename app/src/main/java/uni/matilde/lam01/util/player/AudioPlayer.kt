package uni.matilde.lam01.util.player

import java.io.File

interface AudioPlayer {

    fun playFile(file: File)
    fun stop()
}
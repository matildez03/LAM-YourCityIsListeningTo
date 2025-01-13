package uni.matilde.lam01.work

import android.content.Context
import androidx.work.WorkerParameters
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import uni.matilde.lam01.App
import java.io.File
import uni.matilde.lam01.util.NotificationHelper
import androidx.work.CoroutineWorker


class AudioUploadCoroutineWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = App.instance.audioRepository

    override suspend fun doWork(): Result {
        val filePath = inputData.getString("filePath") ?: return Result.failure()
        val latitude = inputData.getDouble("latitude", 0.0)
        val longitude = inputData.getDouble("longitude", 0.0)

        return try {
            val multipartFile = createMultipartFile(filePath)

            // Esegui l'upload come funzione `suspend`
            val result = repository.uploadAudio(
                longitude = longitude,
                latitude = latitude,
                file = multipartFile
            )

            if (result.isSuccess) {
                NotificationHelper.showNotification(
                    context = applicationContext,
                    channelId = "audio_upload_channel",
                    title = "Caricamento completato",
                    message = "L'audio è stato caricato con successo."
                )
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun createMultipartFile(filePath: String): MultipartBody.Part {
        val file = File(filePath)
        return MultipartBody.Part.createFormData(
            "file",
            file.name,
            file.asRequestBody("audio/mpeg".toMediaTypeOrNull())
        )
    }
}

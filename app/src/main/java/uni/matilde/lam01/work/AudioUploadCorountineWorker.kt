package uni.matilde.lam01.work

import android.content.Context
import android.util.Log
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
        val username = inputData.getString("username") ?: "Anonimo"
        val locationName = inputData.getString("locationName") ?: "Posizione sconosciuta"

        return try {
            // Recupera direttamente l'AudioEntity o null
            val audioEntity = repository.uploadAndSaveAudio(username, filePath, latitude, longitude, locationName).getOrNull()

            if (audioEntity != null) {
                // Mostra una notifica in caso di successo
                NotificationHelper.showUploadNotification(
                    context = applicationContext,
                    channelId = "audio_upload_channel",
                    title = "Caricamento completato",
                    message = "L'audio è stato caricato con successo."
                )
                Result.success()
            } else {
                Log.e("AudioUploadWorker", "Errore: risultato nullo durante l'upload.")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("AudioUploadWorker", "Errore durante l'upload: ${e.message}")
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

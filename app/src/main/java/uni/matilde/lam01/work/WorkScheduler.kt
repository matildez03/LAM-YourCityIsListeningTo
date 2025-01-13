package uni.matilde.lam01.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object WorkScheduler {
    fun scheduleAudioUpload(
        username: String,
        locationName: String,
        context: Context,
        filePath: String,
        latitude: Double,
        longitude: Double
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED) // Solo su Wi-Fi
            .build()

        val inputData = Data.Builder()
            .putString("username", username)
            .putString("locationName", locationName)
            .putString("filePath", filePath)
            .putDouble("latitude", latitude)
            .putDouble("longitude", longitude)
            .build()

        val uploadWorkRequest = OneTimeWorkRequestBuilder<AudioUploadCoroutineWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(uploadWorkRequest)
    }
}

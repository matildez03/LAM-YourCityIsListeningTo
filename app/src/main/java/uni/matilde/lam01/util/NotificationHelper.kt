package uni.matilde.lam01.util

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import uni.matilde.lam01.MainActivity
import uni.matilde.lam01.R

object NotificationHelper {

    // Mappa per gestire i diversi canali
    private val channels = mapOf(
        "audio_upload_channel" to ChannelData(
            name = "Caricamento Audio",
            description = "Notifiche per il caricamento degli audio",
            importance = NotificationManager.IMPORTANCE_HIGH
        ),
        "general_channel" to ChannelData(
            name = "Generale",
            description = "Notifiche generali dell'app",
            importance = NotificationManager.IMPORTANCE_DEFAULT
        )
    )

    // Classe per descrivere i dati di un canale
    data class ChannelData(
        val name: String,
        val description: String,
        val importance: Int
    )

    // Creare i canali di notifica
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            channels.forEach { (channelId, channelData) ->
                val channel = NotificationChannel(
                    channelId,
                    channelData.name,
                    channelData.importance
                ).apply {
                    description = channelData.description
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    // Richiedere il permesso per le notifiche (necessario da Android 13+)
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001 // Codice di richiesta personalizzato
                )
            }
        }
    }

    // Mostrare una notifica
    fun showUploadNotification(
        context: Context,
        channelId: String,
        title: String,
        message: String,
        notificationId: Int? = null
    ) {
        val notificationManager = NotificationManagerCompat.from(context)

        // Controlla i permessi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return // Permesso non concesso, non inviare la notifica
        }

        // Intent per aprire l'app e navigare al composable "audios"
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "audios") // Indica che vuoi navigare a "audios"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId ?: generateNotificationId(), notification)
    }

    private fun generateNotificationId(): Int {
        return System.currentTimeMillis().toInt()
    }
}

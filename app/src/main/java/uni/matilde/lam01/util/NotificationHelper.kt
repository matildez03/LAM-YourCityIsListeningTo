package uni.matilde.lam01.util

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
    fun showNotification(
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

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId ?: generateNotificationId(), notification)
    }

    // Mostrare una notifica per lavori riusciti
    fun showSuccessNotification(
        context: Context,
        title: String = "Caricamento completato",
        message: String = "Il tuo file è stato caricato con successo."
    ) {
        showNotification(
            context = context,
            channelId = "audio_upload_channel",
            title = title,
            message = message
        )
    }

    // Mostrare una notifica per lavori falliti
    fun showFailureNotification(
        context: Context,
        title: String = "Caricamento fallito",
        message: String = "Si è verificato un problema durante il caricamento del file."
    ) {
        showNotification(
            context = context,
            channelId = "audio_upload_channel",
            title = title,
            message = message
        )
    }

    private fun generateNotificationId(): Int {
        return System.currentTimeMillis().toInt()
    }
}

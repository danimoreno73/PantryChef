package com.pantrychef.back.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.pantrychef.R

/**
 * Helper para crear y mostrar notificaciones del sistema
 */
class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_REMINDERS = "reminders"
        const val CHANNEL_ALERTS = "alerts"
        const val NOTIFICATION_ID_REMINDER = 1
        const val NOTIFICATION_ID_ALERT = 2
    }

    init {
        createNotificationChannels()
    }

    /**
     * Crea los canales de notificación (requerido en Android O+)
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal para recordatorios diarios
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Recordatorios",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios diarios para registrar comidas"
            }

            // Canal para alertas de bajo stock
            val alertChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "Alertas de despensa",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de productos con bajo stock"
            }

            // Registrar canales
            notificationManager.createNotificationChannels(listOf(reminderChannel, alertChannel))
        }
    }

    /**
     * Muestra notificación de recordatorio diario
     */
    fun showDailyReminder(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_REMINDER, notification)
    }

    /**
     * Muestra notificación de alerta de bajo stock
     */
    fun showLowStockAlert(urgentCount: Int) {
        val title = "Productos bajos en stock"
        val message = if (urgentCount == 1) {
            "Tienes 1 producto urgente"
        } else {
            "Tienes $urgentCount productos urgentes"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_ALERT, notification)
    }

    fun checkNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
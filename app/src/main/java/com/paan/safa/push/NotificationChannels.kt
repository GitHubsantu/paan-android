package com.paan.safa.push

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import com.paan.safa.R

object NotificationChannels {
    // A channel's sound can't be changed after it exists. To change the sound later, use a new id (v2).
    const val READY = "order_ready_v1"
    const val UPDATES = "order_updates_v1"

    fun create(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val sound = Uri.parse("android.resource://${context.packageName}/${R.raw.order_ready}")
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        manager.createNotificationChannel(
            NotificationChannel(READY, context.getString(R.string.notification_channel_ready_name), NotificationManager.IMPORTANCE_HIGH).apply {
                description = context.getString(R.string.notification_channel_ready_description)
                setSound(sound, attrs)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(UPDATES, context.getString(R.string.notification_channel_updates_name), NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = context.getString(R.string.notification_channel_updates_description)
            },
        )
    }
}

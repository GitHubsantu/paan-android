package com.paan.safa.push

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.paan.safa.MainActivity
import com.paan.safa.R
import com.paan.safa.data.local.SessionStore
import com.paan.safa.domain.repository.NotificationRepository
import com.paan.safa.domain.repository.OrderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PaanMessagingService : FirebaseMessagingService() {

    @Inject lateinit var tokens: DeviceTokenRepository
    @Inject lateinit var orders: OrderRepository
    @Inject lateinit var notifications: NotificationRepository
    @Inject lateinit var store: SessionStore

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        scope.launch { if (store.token.first() != null) tokens.register() }
    }

    @SuppressLint("MissingPermission") // notify() is skipped by the system when the permission is off
    override fun onMessageReceived(message: RemoteMessage) {
        val d = message.data
        if (d["type"] != "order_status") return

        val orderId = d["order_id"].orEmpty()
        val ready = d["status"] == "ready"
        val title = d["title"] ?: getString(R.string.push_fallback_title)
        val body = d["body"].orEmpty()

        // Keep the app's own lists and the bell badge fresh if the app is running.
        scope.launch {
            orders.refresh()
            if (store.token.first() != null) notifications.syncAfterPush()
        }

        val open = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(MainActivity.EXTRA_ORDER_ID, orderId)
        }
        val pending = PendingIntent.getActivity(
            this, orderId.hashCode(), open, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(this, if (ready) NotificationChannels.READY else NotificationChannels.UPDATES)
            .setSmallIcon(R.drawable.ic_stat_order)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(if (ready) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            // Same id per order: a newer status replaces the older notification.
            NotificationManagerCompat.from(this).notify(orderId.hashCode(), notification)
        }
    }
}

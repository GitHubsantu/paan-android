package com.paan.safa.domain.repository

import com.paan.safa.domain.model.AppNotification
import kotlinx.coroutines.flow.StateFlow

interface NotificationRepository {
    val notifications: StateFlow<List<AppNotification>>
    val unreadCount: StateFlow<Int>
    val hasMore: StateFlow<Boolean>

    /** Reloads page 1 (also updates the unread count). */
    suspend fun refresh(): Result<Unit>
    suspend fun loadMore(): Result<Unit>
    /** Cheap call for the badge. */
    suspend fun refreshUnreadCount(): Result<Unit>

    suspend fun markRead(id: String): Result<Unit>
    suspend fun markAllRead(): Result<Unit>
    suspend fun delete(id: String): Result<Unit>

    /** Marks every unread notification of this order as read (used when a push is tapped). */
    suspend fun markOrderRead(orderId: String)
    /** A push arrived: reload the list if it was ever opened, otherwise just the count. */
    suspend fun syncAfterPush()

    /** Forgets everything (logout). Late responses from the old login are ignored. */
    fun clearLocal()
}
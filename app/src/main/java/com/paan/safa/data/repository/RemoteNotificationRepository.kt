package com.paan.safa.data.repository

import com.paan.safa.data.remote.ApiException
import com.paan.safa.data.remote.PageMeta
import com.paan.safa.data.remote.PaanApi
import com.paan.safa.data.remote.apiCall
import com.paan.safa.data.remote.apiCallUnit
import com.paan.safa.data.remote.apiCallWithMeta
import com.paan.safa.data.remote.toDomain
import com.paan.safa.domain.model.AppNotification
import com.paan.safa.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteNotificationRepository @Inject constructor(
    private val api: PaanApi,
) : NotificationRepository {

    private val _items = MutableStateFlow<List<AppNotification>>(emptyList())
    override val notifications: StateFlow<List<AppNotification>> = _items.asStateFlow()

    private val _unread = MutableStateFlow(0)
    override val unreadCount: StateFlow<Int> = _unread.asStateFlow()

    private val _hasMore = MutableStateFlow(false)
    override val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val lock = Mutex()                      // one list load at a time
    private val generation = AtomicInteger(0)       // bumped on logout: old responses are dropped
    @Volatile private var loaded = false
    private var page = 0

    override suspend fun refresh(): Result<Unit> = lock.withLock {
        val gen = generation.get()
        apiCallWithMeta { api.notifications(PAGE_SIZE, 1) }.map { (list, meta) ->
            if (gen == generation.get()) {
                _items.value = list.map { it.toDomain() }
                page = 1
                _hasMore.value = meta.hasNext()
                meta?.unreadCount?.let { _unread.value = it }
                loaded = true
            }
            Unit
        }
    }

    override suspend fun loadMore(): Result<Unit> = lock.withLock {
        if (!_hasMore.value) return@withLock Result.success(Unit)
        val gen = generation.get()
        val next = page + 1
        apiCallWithMeta { api.notifications(PAGE_SIZE, next) }.map { (list, meta) ->
            if (gen == generation.get()) {
                val known = _items.value.mapTo(HashSet()) { it.id }
                val fresh = list.map { it.toDomain() }.filter { it.id !in known }
                _items.update { current -> current + fresh }
                page = next
                _hasMore.value = meta.hasNext()
                meta?.unreadCount?.let { _unread.value = it }
            }
            Unit
        }
    }

    override suspend fun refreshUnreadCount(): Result<Unit> {
        val gen = generation.get()
        return apiCall { api.unreadCount() }.map { dto ->
            if (gen == generation.get()) _unread.value = dto.unreadCount
            Unit
        }
    }

    // ---- Changes: update the screen first, undo if the server says no ----

    override suspend fun markRead(id: String): Result<Unit> {
        val itemsBefore = _items.value
        val unreadBefore = _unread.value
        if (itemsBefore.firstOrNull { it.id == id }?.isRead == true) return Result.success(Unit)

        applyRead(id)
        return apiCallUnit { api.markNotificationRead(id) }.fold(
            onSuccess = {
                applyRead(id)
                refreshUnreadCount()
                Result.success(Unit)
            },
            onFailure = { e ->
                if (e.isNotFound()) {
                    removeLocal(id); Result.success(Unit)
                } else {
                    _items.value = itemsBefore; _unread.value = unreadBefore
                    Result.failure(e)
                }
            },
        )
    }

    override suspend fun markAllRead(): Result<Unit> {
        val itemsBefore = _items.value
        val unreadBefore = _unread.value
        if (unreadBefore == 0 && itemsBefore.none { !it.isRead }) return Result.success(Unit)

        applyAllRead()
        return apiCallUnit { api.markAllNotificationsRead() }.fold(
            onSuccess = {
                applyAllRead()
                refreshUnreadCount()
                Result.success(Unit)
            },
            onFailure = { e ->
                _items.value = itemsBefore; _unread.value = unreadBefore
                Result.failure(e)
            },
        )
    }

    override suspend fun delete(id: String): Result<Unit> {
        val itemsBefore = _items.value
        val unreadBefore = _unread.value

        removeLocal(id)
        return apiCallUnit { api.deleteNotification(id) }.fold(
            onSuccess = {
                removeLocal(id)
                refreshUnreadCount()
                Result.success(Unit)
            },
            onFailure = { e ->
                if (e.isNotFound()) {
                    Result.success(Unit)
                } else {
                    _items.value = itemsBefore; _unread.value = unreadBefore
                    Result.failure(e)
                }
            },
        )
    }

    override suspend fun markOrderRead(orderId: String) {
        refresh()   // the push may be newer than our cache
        _items.value.filter { it.orderId == orderId && !it.isRead }.forEach { markRead(it.id) }
    }

    override suspend fun syncAfterPush() {
        if (loaded) refresh() else refreshUnreadCount()
    }

    override fun clearLocal() {
        generation.incrementAndGet()
        _items.value = emptyList()
        _unread.value = 0
        _hasMore.value = false
        page = 0
        loaded = false
    }

    // ---- helpers ----

    private fun applyRead(id: String) {
        val wasUnread = _items.value.any { it.id == id && !it.isRead }
        val now = System.currentTimeMillis()
        _items.update { list -> list.map { if (it.id == id && !it.isRead) it.copy(readAt = now) else it } }
        if (wasUnread) _unread.update { (it - 1).coerceAtLeast(0) }
    }

    private fun applyAllRead() {
        val now = System.currentTimeMillis()
        _items.update { list -> list.map { if (it.isRead) it else it.copy(readAt = now) } }
        _unread.value = 0
    }

    private fun removeLocal(id: String) {
        val wasUnread = _items.value.any { it.id == id && !it.isRead }
        _items.update { list -> list.filterNot { it.id == id } }
        if (wasUnread) _unread.update { (it - 1).coerceAtLeast(0) }
    }

    private fun PageMeta?.hasNext(): Boolean = this != null && currentPage < lastPage
    private fun Throwable.isNotFound(): Boolean = (this as? ApiException)?.httpCode == 404

    private companion object {
        const val PAGE_SIZE = 20
    }
}
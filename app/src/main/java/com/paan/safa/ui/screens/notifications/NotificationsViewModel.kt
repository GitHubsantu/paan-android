package com.paan.safa.ui.screens.notifications

import com.paan.safa.data.remote.asUiText
import com.paan.safa.core.ui.UiText
import com.paan.safa.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paan.safa.domain.model.AppNotification
import com.paan.safa.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val items: List<AppNotification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val error: UiText? = null,     // full-screen error (only when there is nothing to show)
    val message: UiText? = null,   // one-time snackbar text
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repo: NotificationRepository,
) : ViewModel() {

    private data class Local(
        val loading: Boolean = true,
        val loadingMore: Boolean = false,
        val error: UiText? = null,
        val message: UiText? = null,
    )

    private val local = MutableStateFlow(Local())

    val state: StateFlow<NotificationsUiState> =
        combine(repo.notifications, repo.unreadCount, repo.hasMore, local) { items, unread, more, l ->
            NotificationsUiState(
                items = items,
                unreadCount = unread,
                isLoading = l.loading && items.isEmpty(),
                isLoadingMore = l.loadingMore,
                hasMore = more,
                error = if (items.isEmpty()) l.error else null,
                message = l.message,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationsUiState())

    init { load() }

    fun load() {
        viewModelScope.launch {
            local.update { it.copy(loading = true, error = null) }
            val failure = repo.refresh().exceptionOrNull()?.asUiText()
            local.update {
                it.copy(
                    loading = false,
                    error = failure,
                    message = if (failure != null && repo.notifications.value.isNotEmpty()) failure else it.message,
                )
            }
        }
    }

    fun loadMore() {
        if (local.value.loadingMore || !repo.hasMore.value) return
        viewModelScope.launch {
            local.update { it.copy(loadingMore = true) }
            val failure = repo.loadMore().exceptionOrNull()?.asUiText()
            local.update { it.copy(loadingMore = false, message = failure ?: it.message) }
        }
    }

    /** Tapping a card marks it read (the screen then opens the order). */
    fun onOpen(n: AppNotification) {
        if (n.isRead) return
        viewModelScope.launch { repo.markRead(n.id).onFailure(::show) }
    }

    fun markAllRead() {
        viewModelScope.launch { repo.markAllRead().onFailure(::show) }
    }

    fun delete(id: String) {
        viewModelScope.launch { repo.delete(id).onFailure(::show) }
    }

    fun messageShown() = local.update { it.copy(message = null) }

    private fun show(e: Throwable) = local.update { it.copy(message = e.asUiText() ?: UiText.Res(R.string.common_something_went_wrong)) }
}
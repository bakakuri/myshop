package ge.myshop.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.HomeRepository
import ge.myshop.app.domain.model.AppNotification
import ge.myshop.app.ui.common.ProductActions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<AppNotification> = emptyList(),
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = true,
    val error: String? = null,
) {
    val unread: Int get() = notifications.count { !it.isRead }
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val session: ProductActions,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        if (!session.isLoggedIn) {
            _uiState.value = NotificationsUiState(isLoading = false, isLoggedIn = false)
            return
        }
        viewModelScope.launch {
            homeRepository.getNotifications()
                .onSuccess { list -> _uiState.update { it.copy(notifications = list, isLoading = false, error = null) } }
                .onFailure { _uiState.update { it.copy(isLoading = false, error = "შეტყობინებების ჩატვირთვა ვერ მოხერხდა.") } }
        }
    }

    fun markRead(id: String) {
        _uiState.update { s -> s.copy(notifications = s.notifications.map { if (it.id == id) it.copy(isRead = true) else it }) }
        viewModelScope.launch { homeRepository.markNotificationRead(id) }
    }

    fun markAllRead() {
        _uiState.update { s -> s.copy(notifications = s.notifications.map { it.copy(isRead = true) }) }
        viewModelScope.launch { homeRepository.markAllNotificationsRead() }
    }
}

package ge.myshop.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.HomeRepository
import ge.myshop.app.domain.model.AppNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<AppNotification> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            homeRepository.getNotifications()
                .fold(
                    onSuccess = { notifs -> _uiState.update { it.copy(notifications = notifs, isLoading = false) } },
                    onFailure = { _uiState.update { it.copy(isLoading = false) } },
                )
        }
    }

    fun markRead(notificationId: String) {
        viewModelScope.launch {
            homeRepository.markNotificationRead(notificationId)
            _uiState.update { state ->
                state.copy(notifications = state.notifications.map {
                    if (it.id == notificationId) it.copy(isRead = true) else it
                })
            }
        }
    }
}

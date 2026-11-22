package ge.myshop.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.CartRepository
import ge.myshop.app.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val cartCount: Int = 0,
    val unreadChatCount: Int = 0,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init { refreshBadges() }

    fun refreshBadges() {
        viewModelScope.launch {
            launch {
                cartRepository.getItemCount()
                    .onSuccess { count -> _uiState.update { it.copy(cartCount = count) } }
            }
            launch {
                chatRepository.getUnreadCount()
                    .onSuccess { count -> _uiState.update { it.copy(unreadChatCount = count) } }
            }
        }
    }
}

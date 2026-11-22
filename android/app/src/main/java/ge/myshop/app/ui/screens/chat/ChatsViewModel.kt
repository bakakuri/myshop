package ge.myshop.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.ChatRepository
import ge.myshop.app.domain.model.Conversation
import ge.myshop.app.ui.common.ProductActions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatsUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val session: ProductActions,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatsUiState())
    val uiState: StateFlow<ChatsUiState> = _uiState.asStateFlow()

    fun load() {
        if (!session.isLoggedIn) {
            _uiState.value = ChatsUiState(isLoading = false, isLoggedIn = false)
            return
        }
        _uiState.update { it.copy(isLoading = it.conversations.isEmpty(), isLoggedIn = true, error = null) }
        viewModelScope.launch {
            chatRepository.getConversations()
                .onSuccess { convs ->
                    // newest activity first; customers never see internal notes
                    val visible = convs.map { c -> c.copy(messages = c.messages.filterNot { it.isInternalNote }) }
                        .sortedByDescending { it.lastMessageAt ?: it.createdAt }
                    _uiState.update { it.copy(conversations = visible, isLoading = false) }
                }
                .onFailure { _uiState.update { it.copy(isLoading = false, error = "ჩათების ჩატვირთვა ვერ მოხერხდა.") } }
        }
    }
}

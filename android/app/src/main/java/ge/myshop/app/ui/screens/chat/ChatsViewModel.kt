package ge.myshop.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.ChatRepository
import ge.myshop.app.domain.model.Conversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatsUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatsUiState())
    val uiState: StateFlow<ChatsUiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            chatRepository.getConversations()
                .fold(
                    onSuccess = { convs -> _uiState.update { it.copy(conversations = convs, isLoading = false) } },
                    onFailure = { _uiState.update { it.copy(isLoading = false) } },
                )
        }
    }
}

package ge.myshop.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.ChatRepository
import ge.myshop.app.domain.model.Conversation
import ge.myshop.app.domain.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatDetailUiState(
    val isLoading: Boolean = true,
    val messages: List<Message> = emptyList(),
    val orderNumber: String? = null,
    val isSending: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatDetailUiState())
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    private var currentConversationId: String? = null

    fun loadConversation(conversationId: String) {
        if (currentConversationId == conversationId) return
        currentConversationId = conversationId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            chatRepository.getMessages(conversationId).fold(
                onSuccess = { messages ->
                    _uiState.update { it.copy(isLoading = false, messages = messages) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                },
            )

            // Mark as read
            chatRepository.markConversationRead(conversationId)

            // Subscribe to real-time new messages
            subscribeToMessages(conversationId)
        }
    }

    private fun subscribeToMessages(conversationId: String) {
        viewModelScope.launch {
            try {
                chatRepository.subscribeToMessages(conversationId).collect { newMessage ->
                    _uiState.update { state ->
                        // Avoid duplicates
                        if (state.messages.any { it.id == newMessage.id }) {
                            state
                        } else {
                            state.copy(messages = state.messages + newMessage)
                        }
                    }
                    // Mark read on new incoming message
                    chatRepository.markConversationRead(conversationId)
                }
            } catch (_: Exception) {
                // Realtime disconnect is non-fatal — user can still send/receive manually
            }
        }
    }

    fun sendMessage(content: String) {
        val conversationId = currentConversationId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }

            chatRepository.sendMessage(conversationId, content).fold(
                onSuccess = { message ->
                    _uiState.update { state ->
                        state.copy(
                            isSending = false,
                            messages = if (state.messages.any { it.id == message.id }) {
                                state.messages
                            } else {
                                state.messages + message
                            },
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSending = false) }
                },
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { chatRepository.unsubscribe() }
    }
}

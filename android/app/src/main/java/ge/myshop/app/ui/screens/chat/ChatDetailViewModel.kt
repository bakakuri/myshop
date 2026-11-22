package ge.myshop.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.ChatRepository
import ge.myshop.app.domain.model.Message
import ge.myshop.app.domain.model.OrderRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatDetailUiState(
    val isLoading: Boolean = true,
    val messages: List<Message> = emptyList(),
    val order: OrderRef? = null,
    val draft: String = "",
    val isSending: Boolean = false,
    val error: String? = null,
    val sendError: String? = null,
)

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatDetailUiState())
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    private var conversationId: String? = null

    fun loadConversation(id: String, force: Boolean = false) {
        if (conversationId == id && !force) return
        conversationId = id
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            launch { chatRepository.getConversation(id).onSuccess { c -> _uiState.update { it.copy(order = c.order) } } }
            chatRepository.getMessages(id)
                .onSuccess { msgs -> _uiState.update { it.copy(isLoading = false, messages = msgs.filterNot { m -> m.isInternalNote }) } }
                .onFailure { _uiState.update { it.copy(isLoading = false, error = "შეტყობინებების ჩატვირთვა ვერ მოხერხდა.") } }
            chatRepository.markConversationRead(id)
            subscribe(id)
        }
    }

    private fun subscribe(id: String) {
        viewModelScope.launch {
            runCatching {
                chatRepository.subscribeToMessages(id).collect { incoming ->
                    _uiState.update { s -> if (s.messages.any { it.id == incoming.id }) s else s.copy(messages = s.messages + incoming) }
                    chatRepository.markConversationRead(id)
                }
            } // realtime drop is non-fatal: messages still load on reopen
        }
    }

    fun updateDraft(text: String) = _uiState.update { it.copy(draft = text.take(2000), sendError = null) }

    fun send() {
        val id = conversationId ?: return
        val text = _uiState.value.draft.trim()
        if (text.isEmpty() || _uiState.value.isSending) return
        _uiState.update { it.copy(isSending = true, sendError = null) }
        viewModelScope.launch {
            chatRepository.sendMessage(id, text)
                .onSuccess { msg ->
                    _uiState.update { s ->
                        s.copy(
                            isSending = false,
                            draft = "",
                            messages = if (s.messages.any { it.id == msg.id }) s.messages else s.messages + msg,
                        )
                    }
                }
                .onFailure { _uiState.update { it.copy(isSending = false, sendError = "შეტყობინება ვერ გაიგზავნა") } }
        }
    }

    fun consumeSendError() = _uiState.update { it.copy(sendError = null) }

    override fun onCleared() {
        super.onCleared()
        // viewModelScope is already cancelled here — use a short-lived scope to close the channel
        CoroutineScope(Dispatchers.IO + NonCancellable).launch { runCatching { chatRepository.unsubscribe() } }
    }
}

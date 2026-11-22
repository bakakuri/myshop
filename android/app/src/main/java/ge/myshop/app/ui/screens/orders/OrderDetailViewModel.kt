package ge.myshop.app.ui.screens.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.ChatRepository
import ge.myshop.app.data.repository.OrderRepository
import ge.myshop.app.domain.model.Order
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderDetailUiState(
    val order: Order? = null,
    val conversationId: String? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val chatRepository: ChatRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    init {
        val orderId: String = savedStateHandle["orderId"] ?: ""
        viewModelScope.launch {
            val orderDeferred = async { orderRepository.getOrder(orderId) }
            val convDeferred = async { chatRepository.getConversationByOrderId(orderId) }
            val order = orderDeferred.await().getOrNull()
            val conv = convDeferred.await().getOrNull()
            _uiState.update { it.copy(order = order, conversationId = conv?.id, isLoading = false) }
        }
    }
}

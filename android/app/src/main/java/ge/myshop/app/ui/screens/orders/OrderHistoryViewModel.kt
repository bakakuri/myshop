package ge.myshop.app.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.OrderRepository
import ge.myshop.app.domain.model.Order
import ge.myshop.app.domain.model.OrderStatus
import ge.myshop.app.ui.common.ProductActions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OrderFilter(val label: String) { ALL("ყველა"), ACTIVE("მიმდინარე"), DONE("დასრულებული") }

data class OrderHistoryUiState(
    val orders: List<Order> = emptyList(),
    val filter: OrderFilter = OrderFilter.ALL,
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = true,
    val error: String? = null,
) {
    val visible: List<Order>
        get() = when (filter) {
            OrderFilter.ALL -> orders
            OrderFilter.ACTIVE -> orders.filter { it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED }
            OrderFilter.DONE -> orders.filter { it.status == OrderStatus.DELIVERED || it.status == OrderStatus.CANCELLED }
        }
}

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val session: ProductActions,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderHistoryUiState())
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    fun load() {
        if (!session.isLoggedIn) {
            _uiState.value = OrderHistoryUiState(isLoading = false, isLoggedIn = false)
            return
        }
        viewModelScope.launch {
            orderRepository.getOrders()
                .onSuccess { list -> _uiState.update { it.copy(orders = list.sortedByDescending { o -> o.createdAt }, isLoading = false, error = null) } }
                .onFailure { _uiState.update { it.copy(isLoading = false, error = "შეკვეთების ჩატვირთვა ვერ მოხერხდა.") } }
        }
    }

    fun setFilter(f: OrderFilter) = _uiState.update { it.copy(filter = f) }
}

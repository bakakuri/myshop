package ge.myshop.app.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.CartRepository
import ge.myshop.app.data.repository.OrderRepository
import ge.myshop.app.domain.model.CartSummary
import ge.myshop.app.domain.model.CreateOrderItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CheckoutEvent {
    data class OrderPlaced(val orderId: String) : CheckoutEvent()
    data class ShowError(val message: String) : CheckoutEvent()
}

data class CheckoutUiState(
    val isLoading: Boolean = true,
    val cartSummary: CartSummary? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    val customerEmail: String = "",
    val city: String = "",
    val address: String = "",
    val deliveryNotes: String = "",
    val customerNote: String = "",
    val nameError: String? = null,
    val phoneError: String? = null,
    val isPlacing: Boolean = false,
    val event: CheckoutEvent? = null,
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        loadCart()
    }

    private fun loadCart() {
        viewModelScope.launch {
            cartRepository.getCartSummary().getOrNull()?.let { summary ->
                _uiState.update { it.copy(isLoading = false, cartSummary = summary) }
            } ?: _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun updateName(value: String) = _uiState.update { it.copy(customerName = value, nameError = null) }
    fun updatePhone(value: String) = _uiState.update { it.copy(customerPhone = value, phoneError = null) }
    fun updateEmail(value: String) = _uiState.update { it.copy(customerEmail = value) }
    fun updateCity(value: String) = _uiState.update { it.copy(city = value) }
    fun updateAddress(value: String) = _uiState.update { it.copy(address = value) }
    fun updateDeliveryNotes(value: String) = _uiState.update { it.copy(deliveryNotes = value) }
    fun updateNote(value: String) = _uiState.update { it.copy(customerNote = value) }

    fun placeOrder() {
        val state = _uiState.value
        var hasError = false

        if (state.customerName.isBlank()) {
            _uiState.update { it.copy(nameError = "სახელი სავალდებულოა") }
            hasError = true
        }
        if (state.customerPhone.isBlank()) {
            _uiState.update { it.copy(phoneError = "ტელეფონი სავალდებულოა") }
            hasError = true
        }
        if (hasError) return

        val summary = state.cartSummary ?: return
        val items = summary.items.map { item ->
            CreateOrderItem(
                productId = item.productId,
                quantity = item.quantity,
            )
        }
        if (items.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPlacing = true) }

            orderRepository.createOrder(
                customerName = state.customerName.trim(),
                customerPhone = state.customerPhone.trim(),
                customerEmail = state.customerEmail.trim().takeIf { it.isNotEmpty() },
                deliveryAddress = state.address.trim().takeIf { it.isNotEmpty() },
                deliveryCity = state.city.trim().takeIf { it.isNotEmpty() },
                deliveryNotes = state.deliveryNotes.trim().takeIf { it.isNotEmpty() },
                customerNote = state.customerNote.trim().takeIf { it.isNotEmpty() },
                items = items,
            ).fold(
                onSuccess = { order ->
                    // Clear the cart after successful order
                    cartRepository.clearCart()
                    _uiState.update {
                        it.copy(
                            isPlacing = false,
                            event = CheckoutEvent.OrderPlaced(order.id),
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isPlacing = false,
                            event = CheckoutEvent.ShowError(e.message ?: "შეკვეთის განთავსება ვერ მოხერხდა"),
                        )
                    }
                },
            )
        }
    }

    fun clearEvent() = _uiState.update { it.copy(event = null) }
}

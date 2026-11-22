package ge.myshop.app.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.AddressRepository
import ge.myshop.app.data.repository.AuthRepository
import ge.myshop.app.data.repository.CartRepository
import ge.myshop.app.data.repository.OrderRepository
import ge.myshop.app.domain.model.CartSummary
import ge.myshop.app.domain.model.CreateOrderItem
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
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
    val customerNote: String = "",
    val nameError: String? = null,
    val phoneError: String? = null,
    val addressError: String? = null,
    val isPlacing: Boolean = false,
    val event: CheckoutEvent? = null,
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
    private val addressRepository: AddressRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    /** One key per checkout: retries after a timeout can't create a second order. */
    private val idempotencyKey = UUID.randomUUID().toString()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val profile = async { authRepository.getProfile().getOrNull() }
            val addresses = async { addressRepository.getAddresses().getOrDefault(emptyList()) }
            val summary = cartRepository.getCartSummary().getOrNull()
            val p = profile.await()
            val address = addresses.await().let { list -> list.firstOrNull { it.isDefault } ?: list.firstOrNull() }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    cartSummary = summary,
                    customerName = it.customerName.ifBlank { address?.recipientName ?: p?.fullName.orEmpty() },
                    customerPhone = it.customerPhone.ifBlank { address?.phone ?: p?.phone.orEmpty() },
                    customerEmail = it.customerEmail.ifBlank { p?.email.orEmpty() },
                    city = it.city.ifBlank { address?.city ?: p?.city.orEmpty() },
                    address = it.address.ifBlank { address?.addressLine.orEmpty() },
                )
            }
        }
    }

    fun updateName(value: String) = _uiState.update { it.copy(customerName = value.take(120), nameError = null) }
    fun updatePhone(value: String) = _uiState.update { it.copy(customerPhone = value.filter { c -> c.isDigit() || c in "+ " }.take(20), phoneError = null) }
    fun updateEmail(value: String) = _uiState.update { it.copy(customerEmail = value.trim().take(120)) }
    fun updateCity(value: String) = _uiState.update { it.copy(city = value.take(60)) }
    fun updateAddress(value: String) = _uiState.update { it.copy(address = value.take(250), addressError = null) }
    fun updateNote(value: String) = _uiState.update { it.copy(customerNote = value.take(1000)) }

    fun placeOrder(requireAddress: Boolean) {
        val state = _uiState.value
        if (state.isPlacing) return
        val digits = state.customerPhone.count { it.isDigit() }
        val nameError = if (state.customerName.isBlank()) "სახელი სავალდებულოა" else null
        val phoneError = when {
            state.customerPhone.isBlank() -> "ტელეფონი სავალდებულოა"
            digits < 9 -> "შეიყვანეთ სწორი ნომერი"
            else -> null
        }
        val addressError = if (requireAddress && state.address.isBlank()) "მისამართი სავალდებულოა" else null
        if (nameError != null || phoneError != null || addressError != null) {
            _uiState.update { it.copy(nameError = nameError, phoneError = phoneError, addressError = addressError) }
            return
        }

        val items = state.cartSummary?.items.orEmpty().map { item ->
            // Only ids and quantities go to the server — it computes every price itself
            CreateOrderItem(productId = item.productId, variantId = item.variantId, quantity = item.quantity)
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
                customerNote = state.customerNote.trim().takeIf { it.isNotEmpty() },
                items = items,
                idempotencyKey = idempotencyKey,
            ).fold(
                onSuccess = { result ->
                    cartRepository.clearCart()
                    _uiState.update { it.copy(isPlacing = false, event = CheckoutEvent.OrderPlaced(result.orderId)) }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(isPlacing = false, event = CheckoutEvent.ShowError("შეკვეთის გაგზავნა ვერ მოხერხდა. შეამოწმეთ მარაგი და სცადეთ ხელახლა."))
                    }
                },
            )
        }
    }

    fun clearEvent() = _uiState.update { it.copy(event = null) }
}

package ge.myshop.app.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.CartRepository
import ge.myshop.app.domain.model.CartItemSummary
import ge.myshop.app.domain.model.CartSummary
import ge.myshop.app.ui.common.ProductActions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = true,
    val cartSummary: CartSummary? = null,
    val error: String? = null,
) {
    val items: List<CartItemSummary> get() = cartSummary?.items.orEmpty()
    val hasUnavailable: Boolean get() = items.any { !it.isAvailable || !it.inStock }
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val actions: ProductActions,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init { loadCart() }

    fun loadCart() {
        if (!actions.isLoggedIn) {
            _uiState.value = CartUiState(isLoading = false, isLoggedIn = false)
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.cartSummary == null, isLoggedIn = true, error = null) }
            cartRepository.getCartSummary()
                .onSuccess { summary -> _uiState.update { it.copy(isLoading = false, cartSummary = summary) } }
                .onFailure { _uiState.update { it.copy(isLoading = false, error = "კალათის ჩატვირთვა ვერ მოხერხდა.") } }
        }
    }

    fun removeItem(item: CartItemSummary) {
        // optimistic
        _uiState.update { s ->
            val rest = s.items.filterNot { it.cartItemId == item.cartItemId }
            s.copy(cartSummary = s.cartSummary?.copy(items = rest, itemCount = rest.sumOf { it.quantity }, subtotal = rest.sumOf { it.lineTotal }))
        }
        viewModelScope.launch {
            cartRepository.removeItem(item.cartItemId)
            loadCart()
        }
    }

    fun changeQuantity(item: CartItemSummary, quantity: Int) {
        if (quantity <= 0) { removeItem(item); return }
        _uiState.update { s ->
            val items = s.items.map { if (it.cartItemId == item.cartItemId) it.copy(quantity = quantity, lineTotal = it.unitPrice * quantity) else it }
            s.copy(cartSummary = s.cartSummary?.copy(items = items, itemCount = items.sumOf { it.quantity }, subtotal = items.sumOf { it.lineTotal }))
        }
        viewModelScope.launch {
            cartRepository.upsertItem(productId = item.productId, variantId = item.variantId, quantity = quantity)
            loadCart() // server totals are the source of truth
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart()
            loadCart()
        }
    }
}

package ge.myshop.app.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.CartRepository
import ge.myshop.app.domain.model.CartSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val isLoading: Boolean = true,
    val cartSummary: CartSummary? = null,
    val error: String? = null,
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init { loadCart() }

    private fun loadCart() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            cartRepository.getCartSummary()
                .fold(
                    onSuccess = { summary -> _uiState.update { it.copy(isLoading = false, cartSummary = summary) } },
                    onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } },
                )
        }
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch {
            cartRepository.removeItem(itemId)
            loadCart()
        }
    }

    fun updateQuantity(itemId: String, productId: String, variantId: String?, quantity: Int) {
        if (quantity <= 0) {
            removeItem(itemId)
            return
        }
        viewModelScope.launch {
            cartRepository.upsertItem(productId = productId, variantId = variantId, quantity = quantity)
            loadCart()
        }
    }
}

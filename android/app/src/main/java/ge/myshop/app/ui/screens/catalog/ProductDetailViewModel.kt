package ge.myshop.app.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.CartRepository
import ge.myshop.app.data.repository.FavoritesRepository
import ge.myshop.app.data.repository.ProductRepository
import ge.myshop.app.domain.model.Product
import ge.myshop.app.domain.model.ProductVariant
import io.github.jan.supabase.auth.Auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProductDetailEvent {
    data class ShowSnackbar(val message: String) : ProductDetailEvent()
    object NavigateToLogin : ProductDetailEvent()
    object NavigateToCart : ProductDetailEvent()
}

data class ProductDetailUiState(
    val isLoading: Boolean = true,
    val product: Product? = null,
    val selectedVariant: ProductVariant? = null,
    val quantity: Int = 1,
    val isFavorite: Boolean = false,
    val isAddingToCart: Boolean = false,
    val error: String? = null,
    val event: ProductDetailEvent? = null,
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val favoritesRepository: FavoritesRepository,
    private val auth: Auth,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            productRepository.getProduct(productId).fold(
                onSuccess = { product ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            product = product,
                            selectedVariant = product.variants.firstOrNull(),
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                },
            )

            // Load favorite state if logged in
            if (auth.currentUserOrNull() != null) {
                favoritesRepository.isFavorite(productId).getOrNull()?.let { fav ->
                    _uiState.update { it.copy(isFavorite = fav) }
                }
            }
        }
    }

    fun selectVariant(variant: ProductVariant) {
        _uiState.update { it.copy(selectedVariant = variant) }
    }

    fun incrementQty() {
        val max = _uiState.value.selectedVariant?.stockQuantity
            ?: _uiState.value.product?.stockQuantity
            ?: 10
        _uiState.update { it.copy(quantity = (it.quantity + 1).coerceAtMost(max)) }
    }

    fun decrementQty() {
        _uiState.update { it.copy(quantity = (it.quantity - 1).coerceAtLeast(1)) }
    }

    fun addToCart() {
        val product = _uiState.value.product ?: return
        if (auth.currentUserOrNull() == null) {
            _uiState.update { it.copy(event = ProductDetailEvent.NavigateToLogin) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingToCart = true) }
            cartRepository.upsertItem(
                productId = product.id,
                variantId = _uiState.value.selectedVariant?.id,
                quantity = _uiState.value.quantity,
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isAddingToCart = false,
                            event = ProductDetailEvent.ShowSnackbar("კალათაში დაემატა ✓"),
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isAddingToCart = false,
                            event = ProductDetailEvent.ShowSnackbar(
                                e.message ?: "შეცდომა კალათაში დამატებისას"
                            ),
                        )
                    }
                },
            )
        }
    }

    fun toggleFavorite() {
        val product = _uiState.value.product ?: return
        if (auth.currentUserOrNull() == null) {
            _uiState.update { it.copy(event = ProductDetailEvent.NavigateToLogin) }
            return
        }

        viewModelScope.launch {
            favoritesRepository.toggleFavorite(product.id).fold(
                onSuccess = { isNowFavorite ->
                    _uiState.update { it.copy(isFavorite = isNowFavorite) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(event = ProductDetailEvent.ShowSnackbar(e.message ?: "შეცდომა"))
                    }
                },
            )
        }
    }

    fun clearEvent() {
        _uiState.update { it.copy(event = null) }
    }
}

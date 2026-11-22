package ge.myshop.app.ui.screens.catalog

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.ProductRepository
import ge.myshop.app.domain.model.Product
import ge.myshop.app.domain.model.ProductReview
import ge.myshop.app.domain.model.ProductVariant
import ge.myshop.app.ui.common.ProductActions
import ge.myshop.app.ui.common.ShopViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductDetailUiState(
    val isLoading: Boolean = true,
    val product: Product? = null,
    val selectedVariant: ProductVariant? = null,
    val quantity: Int = 1,
    val reviews: List<ProductReview> = emptyList(),
    val related: List<Product> = emptyList(),
    val error: String? = null,
) {
    /** Display price; the server recomputes the real price when the order is created. */
    val unitPrice: Double get() = (product?.price ?: 0.0) + (selectedVariant?.priceAdjustment ?: 0.0)
    val oldPrice: Double? get() = product?.compareAtPrice?.let { it + (selectedVariant?.priceAdjustment ?: 0.0) }
    val maxQuantity: Int get() = (selectedVariant?.stockQuantity ?: product?.stockQuantity ?: 0).coerceAtLeast(1)
    val inStock: Boolean get() = product?.isAvailable == true && (selectedVariant?.let { it.isAvailable && it.stockQuantity > 0 } ?: true)
}

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    actions: ProductActions,
    private val productRepository: ProductRepository,
) : ShopViewModel(actions) {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()
    private var loadedId: String? = null

    fun loadProduct(productId: String, force: Boolean = false) {
        if (productId == loadedId && !force) return
        loadedId = productId
        refreshFavorites()
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val reviews = async { productRepository.getReviews(productId) }
            val related = async { productRepository.getRelatedProducts(productId) }
            productRepository.getProduct(productId)
                .onSuccess { product ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            product = product,
                            selectedVariant = product.variants.sortedBy { v -> v.sortOrder }.firstOrNull { v -> v.isAvailable },
                            quantity = 1,
                        )
                    }
                }
                .onFailure { _uiState.update { it.copy(isLoading = false, error = "პროდუქტის ჩატვირთვა ვერ მოხერხდა.") } }
            _uiState.update {
                it.copy(
                    reviews = reviews.await().getOrDefault(emptyList()),
                    related = related.await().getOrDefault(emptyList()),
                )
            }
        }
    }

    fun selectVariant(variant: ProductVariant) = _uiState.update { it.copy(selectedVariant = variant, quantity = 1) }

    fun incrementQty() = _uiState.update { it.copy(quantity = (it.quantity + 1).coerceAtMost(it.maxQuantity)) }

    fun decrementQty() = _uiState.update { it.copy(quantity = (it.quantity - 1).coerceAtLeast(1)) }

    fun addSelectedToCart() {
        val s = _uiState.value
        val product = s.product ?: return
        addToCart(product.id, s.selectedVariant?.id, s.quantity)
    }
}

package ge.myshop.app.ui.screens.catalog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.FavoritesRepository
import ge.myshop.app.data.repository.ProductRepository
import ge.myshop.app.domain.model.Product
import ge.myshop.app.domain.model.ProductFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductListUiState(
    val isLoading: Boolean = true,
    val products: List<Product> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val favoritesRepository: FavoritesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListUiState())
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    init {
        val categoryId: String? = savedStateHandle["categoryId"]
        load(categoryId)
    }

    fun load(categoryId: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val filter = ProductFilter(categoryId = categoryId)
            productRepository.getProducts(filter = filter)
                .fold(
                    onSuccess = { products -> _uiState.update { it.copy(isLoading = false, products = products) } },
                    onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "პროდუქტების ჩატვირთვა ვერ მოხდა") } },
                )
        }
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(productId)
        }
    }
}

package ge.myshop.app.ui.screens.profile

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.FavoritesRepository
import ge.myshop.app.domain.model.Product
import ge.myshop.app.ui.common.ProductActions
import ge.myshop.app.ui.common.ShopViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    actions: ProductActions,
    private val favoritesRepository: FavoritesRepository,
) : ShopViewModel(actions) {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    fun load() {
        if (!actions.isLoggedIn) {
            _uiState.value = FavoritesUiState(isLoading = false, isLoggedIn = false)
            return
        }
        refreshFavorites()
        _uiState.update { it.copy(isLoading = it.products.isEmpty(), error = null) }
        viewModelScope.launch {
            favoritesRepository.getFavorites()
                .onSuccess { favs -> _uiState.update { it.copy(isLoading = false, products = favs.mapNotNull { f -> f.product }) } }
                .onFailure { _uiState.update { it.copy(isLoading = false, error = "რჩეულების ჩატვირთვა ვერ მოხერხდა.") } }
        }
    }

    /** Un-hearting on this screen removes the card right away. */
    override fun toggleFavorite(productId: String) {
        _uiState.update { s -> s.copy(products = s.products.filterNot { it.id == productId }) }
        super.toggleFavorite(productId)
    }
}

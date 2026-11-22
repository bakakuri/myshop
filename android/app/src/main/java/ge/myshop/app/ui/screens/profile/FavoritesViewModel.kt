package ge.myshop.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.FavoritesRepository
import ge.myshop.app.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            favoritesRepository.getFavorites()
                .fold(
                    onSuccess = { favs -> _uiState.update { it.copy(products = favs.mapNotNull { f -> f.product }, isLoading = false) } },
                    onFailure = { _uiState.update { it.copy(isLoading = false) } },
                )
        }
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(productId)
            load()
        }
    }
}

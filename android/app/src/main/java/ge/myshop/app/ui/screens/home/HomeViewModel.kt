package ge.myshop.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.CartRepository
import ge.myshop.app.data.repository.HomeRepository
import ge.myshop.app.data.repository.ProductRepository
import ge.myshop.app.domain.model.Banner
import ge.myshop.app.domain.model.Category
import ge.myshop.app.domain.model.Product
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val banners: List<Banner> = emptyList(),
    val popularProducts: List<Product> = emptyList(),
    val discountedProducts: List<Product> = emptyList(),
    val newProducts: List<Product> = emptyList(),
    val topCategories: List<Category> = emptyList(),
    val cartCount: Int = 0,
    val notificationCount: Int = 0,
    val error: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val bannersDeferred = async { homeRepository.getActiveBanners() }
                val popularDeferred = async { productRepository.getFeaturedProducts(10) }
                val discountedDeferred = async { productRepository.getDiscountedProducts(10) }
                val newDeferred = async { productRepository.getNewProducts(10) }
                val categoriesDeferred = async { productRepository.getCategories() }
                val notifCountDeferred = async { homeRepository.getUnreadNotificationCount() }

                val banners = bannersDeferred.await().getOrDefault(emptyList())
                val popular = popularDeferred.await().getOrDefault(emptyList())
                val discounted = discountedDeferred.await().getOrDefault(emptyList())
                val newProds = newDeferred.await().getOrDefault(emptyList())
                val categories = categoriesDeferred.await().getOrDefault(emptyList())
                val notifCount = notifCountDeferred.await().getOrDefault(0)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        banners = banners,
                        popularProducts = popular,
                        discountedProducts = discounted,
                        newProducts = newProds,
                        topCategories = categories.take(8),
                        notificationCount = notifCount,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }

        refreshCartCount()
    }

    fun refreshCartCount() {
        viewModelScope.launch {
            val count = cartRepository.getItemCount().getOrDefault(0)
            _uiState.update { it.copy(cartCount = count) }
        }
    }
}

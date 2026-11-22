package ge.myshop.app.ui.screens.home

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.HomeRepository
import ge.myshop.app.data.repository.ProductRepository
import ge.myshop.app.domain.model.Banner
import ge.myshop.app.domain.model.BannerType
import ge.myshop.app.domain.model.Category
import ge.myshop.app.domain.model.Product
import ge.myshop.app.domain.model.ProductFilter
import ge.myshop.app.domain.model.SortOrder
import ge.myshop.app.ui.common.ProductActions
import ge.myshop.app.ui.common.ShopViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val heroBanner: Banner? = null,
    val categories: List<Category> = emptyList(),
    val popular: List<Product> = emptyList(),
    val discounted: List<Product> = emptyList(),
    val newest: List<Product> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    actions: ProductActions,
    private val homeRepository: HomeRepository,
    private val productRepository: ProductRepository,
) : ShopViewModel(actions) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { load() }

    fun refresh() = load(refreshing = true)

    fun load(refreshing: Boolean = false) {
        _uiState.update { it.copy(isLoading = !refreshing && it.popular.isEmpty(), isRefreshing = refreshing, error = null) }
        refreshFavorites()
        viewModelScope.launch {
            val banners = async { homeRepository.getActiveBanners() }
            val categories = async { productRepository.getCategories() }
            val popular = async { productRepository.getProducts(ProductFilter(sortBy = SortOrder.POPULAR), pageSize = 10) }
            val discounted = async { productRepository.getDiscountedProducts(limit = 10) }
            val newest = async { productRepository.getNewProducts(limit = 10) }

            val popularResult = popular.await()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    heroBanner = banners.await().getOrNull()?.let { list -> list.firstOrNull { b -> b.type == BannerType.HERO } ?: list.firstOrNull() },
                    categories = categories.await().getOrDefault(emptyList()),
                    popular = popularResult.getOrDefault(emptyList()),
                    discounted = discounted.await().getOrDefault(emptyList()),
                    newest = newest.await().getOrDefault(emptyList()),
                    error = popularResult.exceptionOrNull()?.let { "შეამოწმეთ ინტერნეტ კავშირი და სცადეთ ხელახლა." },
                )
            }
        }
    }
}

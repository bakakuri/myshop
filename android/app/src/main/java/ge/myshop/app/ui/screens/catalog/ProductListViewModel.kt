package ge.myshop.app.ui.screens.catalog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.ProductRepository
import ge.myshop.app.domain.model.Category
import ge.myshop.app.domain.model.Product
import ge.myshop.app.domain.model.ProductFilter
import ge.myshop.app.domain.model.SortOrder
import ge.myshop.app.ui.common.ProductActions
import ge.myshop.app.ui.common.ShopViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductListUiState(
    val title: String = "პროდუქტები",
    val category: Category? = null,
    val products: List<Product> = emptyList(),
    val filter: ProductFilter = ProductFilter(),
    val isGrid: Boolean = true,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val error: String? = null,
) {
    val activeFilterCount: Int
        get() = listOfNotNull(filter.minPrice, filter.maxPrice).size +
            (if (filter.inStockOnly) 1 else 0) + (if (filter.hasDiscount) 1 else 0)
}

@HiltViewModel
class ProductListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    actions: ProductActions,
    private val productRepository: ProductRepository,
) : ShopViewModel(actions) {

    private val categoryId: String? = savedStateHandle.get<String>("categoryId")?.takeIf { it.isNotBlank() }
    private val titleArg: String? = savedStateHandle.get<String>("categoryName")?.takeIf { it.isNotBlank() }

    private val _uiState = MutableStateFlow(
        ProductListUiState(
            title = titleArg ?: "ყველა პროდუქტი",
            filter = presetFilter(categoryId, titleArg),
        ),
    )
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    private var page = 0
    private var loadJob: Job? = null

    init {
        refreshFavorites()
        categoryId?.let { id ->
            viewModelScope.launch {
                productRepository.getCategory(id).onSuccess { c -> _uiState.update { it.copy(category = c, title = c.nameKa) } }
            }
        }
        reload()
    }

    fun reload() {
        loadJob?.cancel()
        page = 0
        _uiState.update { it.copy(isLoading = true, error = null, endReached = false, products = emptyList()) }
        loadJob = viewModelScope.launch { fetch() }
    }

    fun loadMore() {
        val s = _uiState.value
        if (s.isLoading || s.isLoadingMore || s.endReached) return
        _uiState.update { it.copy(isLoadingMore = true) }
        loadJob = viewModelScope.launch { fetch() }
    }

    private suspend fun fetch() {
        productRepository.getProducts(_uiState.value.filter, page = page, pageSize = PAGE_SIZE)
            .onSuccess { list ->
                page++
                _uiState.update {
                    it.copy(
                        products = it.products + list,
                        isLoading = false,
                        isLoadingMore = false,
                        endReached = list.size < PAGE_SIZE,
                    )
                }
            }
            .onFailure {
                _uiState.update { it.copy(isLoading = false, isLoadingMore = false, error = "პროდუქტების ჩატვირთვა ვერ მოხერხდა.") }
            }
    }

    fun setSort(sort: SortOrder) {
        if (sort == _uiState.value.filter.sortBy) return
        _uiState.update { it.copy(filter = it.filter.copy(sortBy = sort)) }
        reload()
    }

    fun applyFilter(minPrice: Double?, maxPrice: Double?, inStockOnly: Boolean, hasDiscount: Boolean) {
        _uiState.update {
            it.copy(filter = it.filter.copy(minPrice = minPrice, maxPrice = maxPrice, inStockOnly = inStockOnly, hasDiscount = hasDiscount))
        }
        reload()
    }

    fun toggleLayout() = _uiState.update { it.copy(isGrid = !it.isGrid) }

    companion object {
        private const val PAGE_SIZE = 20

        /** Home "see all" links pass a title instead of a category. */
        fun presetFilter(categoryId: String?, title: String?): ProductFilter = when {
            categoryId != null -> ProductFilter(categoryId = categoryId)
            title == "ფასდაკლებები" || title == "შეთავაზებები" -> ProductFilter(hasDiscount = true)
            title == "ახალი" -> ProductFilter(sortBy = SortOrder.NEWEST)
            else -> ProductFilter(sortBy = SortOrder.POPULAR)
        }
    }
}

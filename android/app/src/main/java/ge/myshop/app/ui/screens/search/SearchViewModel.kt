package ge.myshop.app.ui.screens.search

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.ProductRepository
import ge.myshop.app.domain.model.Category
import ge.myshop.app.domain.model.Product
import ge.myshop.app.ui.common.ProductActions
import ge.myshop.app.ui.common.ShopViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val searched: Boolean = false,
    val results: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val error: String? = null,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    actions: ProductActions,
    private val productRepository: ProductRepository,
) : ShopViewModel(actions) {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")
    private var searchJob: Job? = null

    init {
        refreshFavorites()
        viewModelScope.launch {
            productRepository.getCategories().onSuccess { c -> _uiState.update { it.copy(categories = c) } }
        }
        viewModelScope.launch {
            queryFlow.debounce(350L).distinctUntilChanged().filter { it.trim().length >= 2 }.collect { search(it.trim()) }
        }
    }

    fun onQueryChange(query: String) {
        val q = query.take(80)
        _uiState.update { it.copy(query = q, results = if (q.isBlank()) emptyList() else it.results, searched = q.isNotBlank() && it.searched) }
        queryFlow.value = q
    }

    fun submit() {
        val q = _uiState.value.query.trim()
        if (q.isNotEmpty()) search(q)
    }

    fun clearQuery() {
        searchJob?.cancel()
        _uiState.update { it.copy(query = "", results = emptyList(), isLoading = false, searched = false, error = null) }
        queryFlow.value = ""
    }

    private fun search(q: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            productRepository.searchProducts(q)
                .onSuccess { products -> _uiState.update { it.copy(isLoading = false, searched = true, results = products) } }
                .onFailure { _uiState.update { it.copy(isLoading = false, searched = true, error = "ძებნა ვერ მოხერხდა.") } }
        }
    }
}

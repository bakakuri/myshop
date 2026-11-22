package ge.myshop.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.ProductRepository
import ge.myshop.app.domain.model.Product
import kotlinx.coroutines.FlowPreview
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
    val results: List<Product> = emptyList(),
    val error: String? = null,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(400L)
                .distinctUntilChanged()
                .filter { it.isNotBlank() }
                .collect { q -> search(q) }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query, results = if (query.isBlank()) emptyList() else it.results) }
        queryFlow.value = query
    }

    fun clearQuery() {
        _uiState.update { it.copy(query = "", results = emptyList(), isLoading = false) }
        queryFlow.value = ""
    }

    private fun search(q: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            productRepository.searchProducts(q)
                .fold(
                    onSuccess = { products -> _uiState.update { it.copy(isLoading = false, results = products) } },
                    onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } },
                )
        }
    }
}

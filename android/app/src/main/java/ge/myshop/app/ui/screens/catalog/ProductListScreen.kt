package ge.myshop.app.ui.screens.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.ui.components.EmptyState
import ge.myshop.app.ui.components.ErrorState
import ge.myshop.app.ui.components.ProductCard
import ge.myshop.app.ui.components.ProductCardSkeleton
import ge.myshop.app.ui.components.ScreenHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    categoryId: String?,
    categoryName: String?,
    onProductClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ProductListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ScreenHeader(
                title = categoryName ?: "პროდუქტები",
                onBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { /* TODO: filter sheet */ }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "ფილტრი")
                    }
                },
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 12.dp, end = 12.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = 8.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) { items(8) { ProductCardSkeleton() } }

            uiState.error != null -> ErrorState(
                message = uiState.error!!,
                onRetry = { viewModel.load(categoryId) },
                modifier = Modifier.padding(padding),
            )

            uiState.products.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    icon = "📦",
                    title = "პროდუქტი არ მოიძებნა",
                    subtitle = "ამ კატეგორიაში პროდუქტი ჯერ არ არის",
                )
            }

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 12.dp, end = 12.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 80.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product.id) },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                    )
                }
            }
        }
    }
}

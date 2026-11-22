package ge.myshop.app.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.ui.common.HandleShopEvents
import ge.myshop.app.ui.common.LocalApp
import ge.myshop.app.ui.components.EmptyState
import ge.myshop.app.ui.components.ErrorState
import ge.myshop.app.ui.components.MyShopPageHeader
import ge.myshop.app.ui.components.ProductCard
import ge.myshop.app.ui.components.ProductCardSkeleton

@Composable
fun FavoritesScreen(
    onProductClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onBrowse: () -> Unit = onNavigateBack,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val favorites by viewModel.favoriteIds.collectAsState()
    val app = LocalApp.current
    HandleShopEvents(viewModel.events, app)
    LaunchedEffect(Unit) { viewModel.load() }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            MyShopPageHeader(
                "რჩეულები",
                onBack = onNavigateBack,
                subtitle = if (state.products.isNotEmpty()) "${state.products.size} შენახული პროდუქტი" else "შენახული პროდუქტები",
            )
        }
        when {
            !state.isLoggedIn -> item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyState(Icons.Rounded.Lock, "შედი ანგარიშზე", subtitle = "რჩეულები შენს ანგარიშზე ინახება", actionText = "შესვლა", onAction = app.openLogin)
            }
            state.isLoading -> items(4) { i -> ProductCardSkeleton(Modifier.pad(i)) }
            state.error != null -> item(span = { GridItemSpan(maxLineSpan) }) { ErrorState(state.error!!, onRetry = viewModel::load) }
            state.products.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyState(
                    Icons.Rounded.FavoriteBorder,
                    "რჩეულები ცარიელია",
                    subtitle = "დააჭირე ♡ პროდუქტზე, რომ აქ შეინახო",
                    actionText = "პროდუქტების ნახვა",
                    onAction = onBrowse,
                )
            }
            else -> itemsIndexed(state.products, key = { _, p -> p.id }) { i, p ->
                ProductCard(
                    product = p,
                    isFavorite = true, // every card here is a favourite
                    onClick = { onProductClick(p.id) },
                    onToggleFavorite = { viewModel.toggleFavorite(p.id) },
                    onAddToCart = { viewModel.addToCart(p.id) },
                    modifier = Modifier.pad(i),
                )
            }
        }
    }
}

private fun Modifier.pad(i: Int): Modifier =
    if (i % 2 == 0) padding(start = 14.dp, end = 6.dp) else padding(start = 6.dp, end = 14.dp)

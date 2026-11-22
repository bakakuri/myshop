package ge.myshop.app.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.ui.common.HandleShopEvents
import ge.myshop.app.ui.common.LocalApp
import ge.myshop.app.ui.components.EmptyState
import ge.myshop.app.ui.components.ErrorState
import ge.myshop.app.ui.components.HeaderAction
import ge.myshop.app.ui.components.MsChip
import ge.myshop.app.ui.components.MsSearchField
import ge.myshop.app.ui.components.MyShopHeader
import ge.myshop.app.ui.components.ProductCard
import ge.myshop.app.ui.components.ProductCardSkeleton
import ge.myshop.app.ui.components.categoryIcon
import ge.myshop.app.ui.components.msText

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onProductClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onCategoryClick: (String, String) -> Unit = { _, _ -> },
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val favorites by viewModel.favoriteIds.collectAsState()
    val app = LocalApp.current
    HandleShopEvents(viewModel.events, app)
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            MyShopHeader(
                leading = HeaderAction(Icons.AutoMirrored.Rounded.ArrowBack, "უკან", onClick = onNavigateBack),
                actions = if (state.query.isNotEmpty()) listOf(HeaderAction(Icons.Rounded.Close, "გასუფთავება", onClick = viewModel::clearQuery)) else emptyList(),
                bottom = {
                    MsSearchField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        onSearch = viewModel::submit,
                        modifier = Modifier.focusRequester(focus),
                    )
                },
            )
        }

        when {
            state.query.isBlank() -> {
                if (state.categories.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text("პოპულარული კატეგორიები", style = msText(18.sp, FontWeight.ExtraBold), modifier = Modifier.padding(horizontal = 16.dp))
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        FlowRow(
                            Modifier.padding(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            state.categories.forEach { c ->
                                MsChip(c.nameKa, onClick = { onCategoryClick(c.id, c.nameKa) }, leading = categoryIcon(c.slug))
                            }
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(Icons.Rounded.Search, "რას ეძებ?", subtitle = "ჩაწერე პროდუქტის ან ბრენდის სახელი")
                }
            }
            state.isLoading -> items(4) { i -> ProductCardSkeleton(Modifier.pad(i)) }
            state.error != null -> item(span = { GridItemSpan(maxLineSpan) }) { ErrorState(state.error!!, onRetry = viewModel::submit) }
            state.searched && state.results.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyState(Icons.Rounded.Search, "„${state.query}“ ვერ მოიძებნა", subtitle = "სცადე სხვა სიტყვა ან კატეგორია")
            }
            else -> {
                if (state.results.isNotEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                    Text("ნაპოვნია ${state.results.size} პროდუქტი", style = msText(14.sp, FontWeight.SemiBold), modifier = Modifier.padding(horizontal = 16.dp))
                }
                itemsIndexed(state.results, key = { _, p -> p.id }) { i, p ->
                    ProductCard(
                        product = p,
                        isFavorite = p.id in favorites,
                        onClick = { onProductClick(p.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(p.id) },
                        onAddToCart = { viewModel.addToCart(p.id) },
                        modifier = Modifier.pad(i),
                    )
                }
            }
        }
    }
}

private fun Modifier.pad(i: Int): Modifier =
    if (i % 2 == 0) padding(start = 14.dp, end = 6.dp) else padding(start = 6.dp, end = 14.dp)

package ge.myshop.app.ui.screens.catalog

import ge.myshop.app.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.domain.model.Product
import ge.myshop.app.domain.model.SortOrder
import ge.myshop.app.ui.common.HandleShopEvents
import ge.myshop.app.ui.common.LocalApp
import ge.myshop.app.ui.components.AddToCartPill
import ge.myshop.app.ui.components.CategoryHeroBanner
import ge.myshop.app.ui.components.DiscountBadge
import ge.myshop.app.ui.components.EmptyState
import ge.myshop.app.ui.components.ErrorState
import ge.myshop.app.ui.components.FavoriteToggle
import ge.myshop.app.ui.components.HeaderAction
import ge.myshop.app.ui.components.MsCard
import ge.myshop.app.ui.components.MsChip
import ge.myshop.app.ui.components.MsImage
import ge.myshop.app.ui.components.MsOutlinedButton
import ge.myshop.app.ui.components.MsPrimaryButton
import ge.myshop.app.ui.components.MsSearchBar
import ge.myshop.app.ui.components.MyShopHeader
import ge.myshop.app.ui.components.MsTextField
import ge.myshop.app.ui.components.PriceRow
import ge.myshop.app.ui.components.ProductCard
import ge.myshop.app.ui.components.ProductCardSkeleton
import ge.myshop.app.ui.components.RatingRow
import ge.myshop.app.ui.components.msShadow
import ge.myshop.app.ui.components.msText
import ge.myshop.app.ui.components.productCountLabel
import ge.myshop.app.ui.theme.MsColor
import ge.myshop.app.ui.theme.MsShape

@Composable
fun ProductListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    viewModel: ProductListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val favorites by viewModel.favoriteIds.collectAsState()
    val app = LocalApp.current
    HandleShopEvents(viewModel.events, app)

    var showFilter by remember { mutableStateOf(false) }
    var showSort by remember { mutableStateOf(false) }
    val gridState = rememberLazyGridState()
    LoadMoreEffect(gridState, onLoadMore = viewModel::loadMore)

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(if (state.isGrid) 2 else 1),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            MyShopHeader(
                leading = HeaderAction(Icons.AutoMirrored.Rounded.ArrowBack, "უკან", onClick = onNavigateBack),
                actions = listOf(HeaderAction(Icons.Rounded.Search, "ძებნა", onClick = onNavigateToSearch)),
                bottom = { MsSearchBar(onClick = onNavigateToSearch, onFilterClick = { showFilter = true }) },
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            CategoryHeroBanner(
                title = state.title,
                subtitle = state.category?.let { productCountLabel(it.productCount) },
                iconSlug = state.category?.slug,
                onBack = if (state.category != null) onNavigateToCategories else null,
                modifier = Modifier.padding(horizontal = 14.dp),
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MsChip(
                    text = if (state.activeFilterCount > 0) "ფილტრი · ${state.activeFilterCount}" else "ფილტრი",
                    onClick = { showFilter = true },
                    selected = true,
                    leading = Icons.Rounded.Tune,
                )
                Box(Modifier.weight(1f)) {
                    MsChip(
                        text = state.filter.sortBy.labelKa(),
                        onClick = { showSort = true },
                        leading = Icons.Rounded.SwapVert,
                        trailing = Icons.Rounded.ExpandMore,
                    )
                    DropdownMenu(
                        expanded = showSort,
                        onDismissRequest = { showSort = false },
                        containerColor = Color.White,
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        SortOrder.entries.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.labelKa(), style = msText(14.sp, if (s == state.filter.sortBy) FontWeight.Bold else FontWeight.Medium)) },
                                trailingIcon = { if (s == state.filter.sortBy) Icon(Icons.Rounded.Check, null, tint = MsColor.Purple600) },
                                onClick = { showSort = false; viewModel.setSort(s) },
                            )
                        }
                    }
                }
                LayoutToggle(isGrid = state.isGrid, onToggle = viewModel::toggleLayout)
            }
        }

        when {
            state.isLoading -> items(4) { i -> ProductCardSkeleton(Modifier.gridPadding(i, state.isGrid)) }
            state.error != null && state.products.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                ErrorState(state.error!!, onRetry = viewModel::reload)
            }
            state.products.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyState(
                    R.drawable.td_search,
                    "პროდუქტები ვერ მოიძებნა",
                    subtitle = if (state.activeFilterCount > 0) "სცადე ფილტრის შეცვლა" else "ამ კატეგორიაში ჯერ პროდუქტი არ არის",
                    actionText = if (state.activeFilterCount > 0) "ფილტრის გასუფთავება" else null,
                    onAction = { viewModel.applyFilter(null, null, inStockOnly = false, hasDiscount = false) },
                )
            }
            else -> {
                itemsIndexed(state.products, key = { _, p -> p.id }) { i, p ->
                    if (state.isGrid) {
                        ProductCard(
                            product = p,
                            isFavorite = p.id in favorites,
                            onClick = { onNavigateToProductDetail(p.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(p.id) },
                            onAddToCart = { viewModel.addToCart(p.id) },
                            modifier = Modifier.gridPadding(i, true),
                        )
                    } else {
                        ProductRowCard(
                            product = p,
                            isFavorite = p.id in favorites,
                            onClick = { onNavigateToProductDetail(p.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(p.id) },
                            onAddToCart = { viewModel.addToCart(p.id) },
                            modifier = Modifier.padding(horizontal = 14.dp),
                        )
                    }
                }
                if (state.isLoadingMore) item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MsColor.Purple600, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }
    }

    if (showFilter) {
        FilterSheet(
            initialMin = state.filter.minPrice,
            initialMax = state.filter.maxPrice,
            initialInStock = state.filter.inStockOnly,
            initialDiscount = state.filter.hasDiscount,
            onDismiss = { showFilter = false },
            onApply = { min, max, stock, discount ->
                showFilter = false
                viewModel.applyFilter(min, max, stock, discount)
            },
        )
    }
}

private fun Modifier.gridPadding(i: Int, grid: Boolean): Modifier = when {
    !grid -> padding(horizontal = 14.dp)
    i % 2 == 0 -> padding(start = 14.dp, end = 6.dp)
    else -> padding(start = 6.dp, end = 14.dp)
}

@Composable
private fun LoadMoreEffect(state: LazyGridState, onLoadMore: () -> Unit) {
    val nearEnd by remember {
        derivedStateOf {
            val info = state.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && last >= info.totalItemsCount - 4
        }
    }
    LaunchedEffect(nearEnd) { if (nearEnd) onLoadMore() }
}

/** Grid / list segmented toggle. */
@Composable
private fun LayoutToggle(isGrid: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier
            .height(44.dp)
            .msShadow(MsShape.Pill, 3.dp)
            .clip(MsShape.Pill)
            .background(Color.White)
            .border(1.dp, MsColor.CardBorder, MsShape.Pill)
            .padding(4.dp),
    ) {
        listOf(true to Icons.Rounded.GridView, false to Icons.Rounded.ViewList).forEach { (grid, icon) ->
            val selected = grid == isGrid
            Box(
                Modifier
                    .size(36.dp)
                    .clip(MsShape.Pill)
                    .background(if (selected) MsColor.Purple100 else Color.Transparent)
                    .clickable(enabled = !selected, role = Role.Button, onClickLabel = if (grid) "ბადე" else "სია", onClick = onToggle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, if (grid) "ბადე" else "სია", tint = if (selected) MsColor.Purple600 else MsColor.InkMuted, modifier = Modifier.size(21.dp))
            }
        }
    }
}

/** Horizontal product card for list mode. */
@Composable
private fun ProductRowCard(
    product: Product,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MsCard(modifier, onClick = onClick) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(116.dp)) {
                MsImage(product.primaryImage, product.nameKa, Modifier.fillMaxSize(), fallbackIcon = Icons.Rounded.ShoppingCart)
                if (product.hasDiscount) DiscountBadge(product.discountPercent, Modifier.padding(6.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(product.nameKa, style = msText(15.sp, FontWeight.Bold), maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    FavoriteToggle(isFavorite, onToggleFavorite, size = 32.dp)
                }
                product.shortDescriptionKa?.let {
                    Text(it, style = msText(12.5.sp, FontWeight.Medium, MsColor.InkSoft), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(4.dp))
                RatingRow(product.averageRating, product.reviewCount, starSize = 13.dp, textSize = 11.sp)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PriceRow(product.price, product.compareAtPrice, priceSize = 18.sp, stacked = true, modifier = Modifier.weight(1f))
                    AddToCartPill(onAddToCart, enabled = product.isAvailable)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    initialMin: Double?,
    initialMax: Double?,
    initialInStock: Boolean,
    initialDiscount: Boolean,
    onDismiss: () -> Unit,
    onApply: (Double?, Double?, Boolean, Boolean) -> Unit,
) {
    var min by remember { mutableStateOf(initialMin?.toLong()?.toString() ?: "") }
    var max by remember { mutableStateOf(initialMax?.toLong()?.toString() ?: "") }
    var inStock by remember { mutableStateOf(initialInStock) }
    var discount by remember { mutableStateOf(initialDiscount) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = MsShape.Sheet,
        containerColor = Color.White,
    ) {
        Column(Modifier.navigationBarsPadding().padding(horizontal = 20.dp).padding(bottom = 20.dp)) {
            Text("ფილტრი", style = msText(22.sp, FontWeight.ExtraBold))
            Spacer(Modifier.height(16.dp))
            Text("ფასი (₾)", style = msText(15.sp, FontWeight.Bold))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MsTextField(min, { min = it.filter(Char::isDigit).take(7) }, "დან", Modifier.weight(1f), keyboardType = KeyboardType.Number)
                MsTextField(max, { max = it.filter(Char::isDigit).take(7) }, "მდე", Modifier.weight(1f), keyboardType = KeyboardType.Number)
            }
            Spacer(Modifier.height(14.dp))
            ToggleRow("მხოლოდ მარაგში არსებული", inStock) { inStock = it }
            ToggleRow("მხოლოდ ფასდაკლებული", discount) { discount = it }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MsOutlinedButton(
                    "გასუფთავება",
                    onClick = { onApply(null, null, false, false) },
                    modifier = Modifier.weight(1f),
                )
                MsPrimaryButton(
                    "ჩვენება",
                    onClick = { onApply(min.toDoubleOrNull(), max.toDoubleOrNull(), inStock, discount) },
                    modifier = Modifier.weight(1f),
                    height = 54.dp,
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onChange(!checked) }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = msText(15.sp, FontWeight.SemiBold), modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MsColor.Purple600,
                uncheckedThumbColor = MsColor.Purple300,
                uncheckedTrackColor = MsColor.Purple100,
                uncheckedBorderColor = MsColor.Purple200,
            ),
        )
    }
}

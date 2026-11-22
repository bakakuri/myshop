package ge.myshop.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.R
import ge.myshop.app.domain.model.Product
import ge.myshop.app.ui.common.HandleShopEvents
import ge.myshop.app.ui.common.LocalApp
import ge.myshop.app.ui.components.CategoryShortcut
import ge.myshop.app.ui.components.ErrorState
import ge.myshop.app.ui.components.HeaderAction
import ge.myshop.app.ui.components.HeroBanner
import ge.myshop.app.ui.components.InfoDialog
import ge.myshop.app.ui.components.MenuSheet
import ge.myshop.app.ui.components.MsSearchBar
import ge.myshop.app.ui.components.MyShopHeader
import ge.myshop.app.ui.components.ProductCard
import ge.myshop.app.ui.components.ProductCardSkeleton
import ge.myshop.app.ui.components.PromoCard
import ge.myshop.app.ui.components.SectionHeader
import ge.myshop.app.ui.components.ShimmerBox
import ge.myshop.app.ui.components.formatGel
import ge.myshop.app.ui.theme.MsBrush
import ge.myshop.app.ui.theme.MsColor
import ge.myshop.app.ui.theme.MsShape

private val SidePad = 14.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCategories: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToProductList: (String?, String?) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToRoute: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val favorites by viewModel.favoriteIds.collectAsState()
    val app = LocalApp.current
    HandleShopEvents(viewModel.events, app)

    var showMenu by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf<Pair<String, List<String>>?>(null) }
    val store = app.store

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            fullWidth {
                MyShopHeader(
                    leading = HeaderAction(Icons.Rounded.Menu, "მენიუ", filled = false) { showMenu = true },
                    actions = listOf(
                        HeaderAction(Icons.Rounded.Notifications, "შეტყობინებები", badge = app.badges.notifications, filled = false, onClick = onNavigateToNotifications),
                    ),
                    bottom = { MsSearchBar(onClick = onNavigateToSearch, onFilterClick = { onNavigateToProductList(null, null) }) },
                )
            }

            when {
                state.isLoading -> {
                    fullWidth { ShimmerBox(Modifier.padding(horizontal = SidePad).fillMaxWidth().height(196.dp), MsShape.Card) }
                    items(4) { i -> ProductCardSkeleton(Modifier.cardPadding(i)) }
                }
                state.error != null && state.popular.isEmpty() -> fullWidth {
                    ErrorState(state.error!!, onRetry = { viewModel.load() })
                }
                else -> {
                    fullWidth {
                        HeroBanner(
                            banner = state.heroBanner,
                            onClick = { onNavigateToProductList(null, "შეთავაზებები") },
                            modifier = Modifier.padding(horizontal = SidePad),
                        )
                    }

                    if (state.categories.isNotEmpty()) fullWidth {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = SidePad, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(state.categories, key = { it.id }) { c ->
                                CategoryShortcut(c, onClick = { onNavigateToProductList(c.id, c.nameKa) })
                            }
                        }
                    }

                    fullWidth {
                        Row(Modifier.padding(horizontal = SidePad), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            val deliveryLines = buildList {
                                add("მიწოდების ვადა: ${store.deliveryEstimate}")
                                store.freeDeliveryThreshold?.let { add("უფასო მიწოდება ${formatGel(it)}-ზე მეტი შეკვეთისას") }
                                if (store.pickupEnabled) add("შესაძლებელია ადგილზე გატანაც")
                                add("ზუსტ პირობებს ადმინისტრატორი შეგითანხმებს ჩათში")
                                store.workingHours?.let { add("სამუშაო საათები: $it") }
                            }
                            PromoCard(
                                title = "მიტანის სერვისი",
                                subtitle = "სწრაფი და საიმედო მიწოდება ${store.deliveryEstimate}",
                                brush = MsBrush.PromoBlue,
                                illustration = R.drawable.td_truck,
                                cta = "დეტალები",
                                onClick = { dialog = "მიტანის სერვისი" to deliveryLines },
                                modifier = Modifier.weight(1f),
                            )
                            PromoCard(
                                title = "უსაფრთხო შეკვეთა",
                                subtitle = "ონლაინ გადახდა არ არის — პირობებს ჩათში შეათანხმებ",
                                brush = MsBrush.PromoPink,
                                illustration = R.drawable.td_shield,
                                onClick = {
                                    dialog = "უსაფრთხო შეკვეთა" to listOf(
                                        "აპში ბარათით გადახდა არ ხდება",
                                        "გაგზავნი შეკვეთის მოთხოვნას",
                                        "ადმინისტრატორი ჩათში დაგიკავშირდება და შეგითანხმებს ფასს, მიწოდებას და გადახდის წესს",
                                        "შეკვეთის სტატუსს აპში ნახავ",
                                    )
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    productSection("პოპულარული პროდუქტები", R.drawable.td_fire, state.popular, favorites,
                        onSeeAll = { onNavigateToProductList(null, "პოპულარული") }, viewModel, onNavigateToProductDetail)
                    productSection("ფასდაკლებები", R.drawable.td_label, state.discounted, favorites,
                        onSeeAll = { onNavigateToProductList(null, "ფასდაკლებები") }, viewModel, onNavigateToProductDetail)
                    productSection("ახალი პროდუქტები", R.drawable.td_sparkles, state.newest, favorites,
                        onSeeAll = { onNavigateToProductList(null, "ახალი") }, viewModel, onNavigateToProductDetail)
                }
            }
        }
    }

    if (showMenu) MenuSheet(onDismiss = { showMenu = false }, onNavigate = onNavigateToRoute)
    dialog?.let { (title, lines) -> InfoDialog(title, lines, onDismiss = { dialog = null }) }
}

private fun LazyGridScope.fullWidth(content: @Composable () -> Unit) {
    item(span = { GridItemSpan(maxLineSpan) }) { content() }
}

/** Two-column card padding: outer edge 14dp, gutter 12dp. */
internal fun Modifier.cardPadding(indexInSection: Int): Modifier =
    if (indexInSection % 2 == 0) padding(start = SidePad, end = 6.dp) else padding(start = 6.dp, end = SidePad)

private fun LazyGridScope.productSection(
    title: String,
    @androidx.annotation.DrawableRes art: Int,
    products: List<Product>,
    favorites: Set<String>,
    onSeeAll: () -> Unit,
    viewModel: HomeViewModel,
    onOpen: (String) -> Unit,
) {
    if (products.isEmpty()) return
    item(span = { GridItemSpan(maxLineSpan) }, key = "header-$title") {
        SectionHeader(
            title = title,
            art = art,
            actionText = "ყველას ნახვა",
            onAction = onSeeAll,
            modifier = Modifier.padding(start = SidePad, end = SidePad, top = 6.dp),
        )
    }
    itemsIndexed(products.take(6), key = { _, p -> "$title-${p.id}" }) { i, p ->
        ProductCard(
            product = p,
            isFavorite = p.id in favorites,
            onClick = { onOpen(p.id) },
            onToggleFavorite = { viewModel.toggleFavorite(p.id) },
            onAddToCart = { viewModel.addToCart(p.id) },
            modifier = Modifier.cardPadding(i),
        )
    }
}

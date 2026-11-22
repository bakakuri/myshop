package ge.myshop.app.ui.screens.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.ui.common.LocalApp
import ge.myshop.app.ui.components.CategoryTile
import ge.myshop.app.ui.components.EmptyState
import ge.myshop.app.ui.components.ErrorState
import ge.myshop.app.ui.components.FreeDeliveryBanner
import ge.myshop.app.ui.components.HeaderAction
import ge.myshop.app.ui.components.InfoDialog
import ge.myshop.app.ui.components.MsSearchBar
import ge.myshop.app.ui.components.MyShopHeader
import ge.myshop.app.ui.components.ShimmerBox
import ge.myshop.app.ui.components.formatGel
import ge.myshop.app.ui.components.msText
import ge.myshop.app.ui.theme.MsShape
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToProductList: (String?, String?) -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val store = LocalApp.current.store
    var showDelivery by remember { mutableStateOf(false) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            MyShopHeader(
                leading = HeaderAction(Icons.AutoMirrored.Rounded.ArrowBack, "უკან", onClick = onNavigateBack),
                actions = listOf(HeaderAction(Icons.Rounded.Search, "ძებნა", onClick = onNavigateToSearch)),
                bottom = { MsSearchBar(onClick = onNavigateToSearch, placeholder = "მოძებნე კატეგორია ან პროდუქტი...") },
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                "ყველა კატეგორია",
                style = msText(22.sp, FontWeight.ExtraBold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
            )
        }

        when {
            state.isLoading -> items(6) { i ->
                ShimmerBox(Modifier.tilePadding(i).fillMaxWidth().height(168.dp), MsShape.Card)
            }
            state.error != null -> item(span = { GridItemSpan(maxLineSpan) }) {
                ErrorState(state.error!!, onRetry = viewModel::load)
            }
            state.categories.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyState(Icons.Rounded.GridView, "კატეგორიები ჯერ არ არის", subtitle = "მალე დაემატება")
            }
            else -> {
                itemsIndexed(state.categories, key = { _, c -> c.id }) { i, c ->
                    CategoryTile(c, i, onClick = { onNavigateToProductList(c.id, c.nameKa) }, modifier = Modifier.tilePadding(i))
                }
                store.freeDeliveryThreshold?.takeIf { store.deliveryEnabled }?.let { threshold ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        FreeDeliveryBanner(
                            threshold = threshold,
                            city = store.city,
                            onClick = { showDelivery = true },
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }
    }

    if (showDelivery) {
        InfoDialog(
            title = "უფასო მიწოდება",
            lines = buildList {
                store.freeDeliveryThreshold?.let { add("${formatGel(it)}-ზე მეტი შეკვეთისას მიწოდება უფასოა") }
                add("მიწოდების ვადა: ${store.deliveryEstimate}")
                add("დეტალებს ადმინისტრატორი შეგითანხმებს ჩათში")
            },
            onDismiss = { showDelivery = false },
        )
    }
}

private fun Modifier.tilePadding(i: Int): Modifier =
    if (i % 2 == 0) padding(start = 14.dp, end = 6.dp) else padding(start = 6.dp, end = 14.dp)

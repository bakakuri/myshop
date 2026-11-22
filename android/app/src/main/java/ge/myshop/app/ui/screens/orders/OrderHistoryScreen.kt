package ge.myshop.app.ui.screens.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.ui.common.LocalApp
import ge.myshop.app.ui.components.EmptyState
import ge.myshop.app.ui.components.ErrorState
import ge.myshop.app.ui.components.LoadingState
import ge.myshop.app.ui.components.MsChip
import ge.myshop.app.ui.components.MyShopPageHeader
import ge.myshop.app.ui.components.OrderSummaryCard

@Composable
fun OrderHistoryScreen(
    onNavigateBack: () -> Unit,
    onOrderClick: (String) -> Unit,
    onStartShopping: () -> Unit = onNavigateBack,
    viewModel: OrderHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val app = LocalApp.current
    LaunchedEffect(Unit) { viewModel.load() }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { MyShopPageHeader("ჩემი შეკვეთები", onBack = onNavigateBack, subtitle = "ისტორია და სტატუსი") }
        if (state.isLoggedIn && state.orders.isNotEmpty()) item {
            Row(Modifier.padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OrderFilter.entries.forEach { f -> MsChip(f.label, onClick = { viewModel.setFilter(f) }, selected = f == state.filter) }
            }
        }
        when {
            !state.isLoggedIn -> item { EmptyState(Icons.Rounded.Lock, "შედი ანგარიშზე", subtitle = "შეკვეთები ანგარიშზეა მიბმული", actionText = "შესვლა", onAction = app.openLogin) }
            state.isLoading -> item { LoadingState() }
            state.error != null -> item { ErrorState(state.error!!, onRetry = viewModel::load) }
            state.orders.isEmpty() -> item {
                EmptyState(Icons.Rounded.Receipt, "შეკვეთები ჯერ არ გაქვს", subtitle = "პირველი შეკვეთის შემდეგ აქ ნახავ სტატუსს", actionText = "შოპინგის დაწყება", onAction = onStartShopping)
            }
            state.visible.isEmpty() -> item { EmptyState(Icons.Rounded.Receipt, "ამ ფილტრით შეკვეთა არ არის") }
            else -> items(state.visible, key = { it.id }) { o ->
                OrderSummaryCard(o, onClick = { onOrderClick(o.id) }, modifier = Modifier.padding(horizontal = 14.dp))
            }
        }
    }
}

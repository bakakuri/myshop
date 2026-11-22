package ge.myshop.app.ui.screens.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.domain.model.Order
import ge.myshop.app.domain.model.OrderStatus
import ge.myshop.app.ui.components.EmptyState
import ge.myshop.app.ui.components.ScreenHeader
import ge.myshop.app.ui.theme.PrimaryPurple

@Composable
fun OrderHistoryScreen(
    onOrderClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: OrderHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { ScreenHeader("შეკვეთები", onBack = onNavigateBack) }) { padding ->
        if (uiState.orders.isEmpty() && !uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(icon = "📦", title = "შეკვეთა არ გაქვს", subtitle = "განათავსეთ პირველი შეკვეთა")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(uiState.orders, key = { it.id }) { order ->
                    OrderHistoryCard(order = order, onClick = { onOrderClick(order.id) })
                }
            }
        }
    }
}

@Composable
private fun OrderHistoryCard(order: Order, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("შეკვეთა #${order.orderNumber}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                OrderStatusBadge(status = order.status)
            }
            Text("₾${"%.2f".format(order.total)}", style = MaterialTheme.typography.bodyMedium, color = PrimaryPurple, fontWeight = FontWeight.SemiBold)
            Text(order.createdAt.substringBefore("T"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun OrderStatusBadge(status: OrderStatus) {
    val (bg, label) = when (status) {
        OrderStatus.PENDING -> Color(0xFFFEF3C7) to Color(0xFFD97706)
        OrderStatus.CONFIRMED -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
        OrderStatus.PROCESSING -> Color(0xFFEDE9FE) to PrimaryPurple
        OrderStatus.SHIPPED -> Color(0xFFE0F2FE) to Color(0xFF0284C7)
        OrderStatus.DELIVERED -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
        OrderStatus.CANCELLED -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
        OrderStatus.REFUNDED -> Color(0xFFF3F4F6) to Color(0xFF6B7280)
    }
    Badge(containerColor = bg) {
        Text(status.labelKa, color = label, style = MaterialTheme.typography.labelSmall)
    }
}

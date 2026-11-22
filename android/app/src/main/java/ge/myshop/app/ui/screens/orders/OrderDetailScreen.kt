package ge.myshop.app.ui.screens.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import ge.myshop.app.domain.model.OrderItem
import ge.myshop.app.ui.components.MyShopButton
import ge.myshop.app.ui.components.ScreenHeader
import ge.myshop.app.ui.theme.PrimaryPurple
import ge.myshop.app.ui.theme.ProductImageShape

@Composable
fun OrderDetailScreen(
    orderId: String,
    onChatClick: (conversationId: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: OrderDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { ScreenHeader("შეკვეთის დეტალები", onBack = onNavigateBack) }) { padding ->
        val order = uiState.order ?: return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Status card
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("შეკვეთა #${order.orderNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        OrderStatusBadge(status = order.status)
                    }
                    Text(order.createdAt.substringBefore("T"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }

            // Items
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("პროდუქტები", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    order.items?.forEachIndexed { idx, item ->
                        if (idx > 0) HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        OrderItemRow(item)
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("სულ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("₾${"%.2f".format(order.total)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                    }
                }
            }

            // Delivery info
            if (!order.deliveryCity.isNullOrBlank() || !order.deliveryAddress.isNullOrBlank()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("მიწოდება", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        order.deliveryCity?.let { Text("ქ. $it") }
                        order.deliveryAddress?.let { Text(it) }
                    }
                }
            }

            // Chat button
            uiState.conversationId?.let { convId ->
                MyShopButton(
                    text = "ჩატში გადასვლა",
                    onClick = { onChatClick(convId) },
                    leadingIcon = Icons.Filled.Chat,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OrderItemRow(item: OrderItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (!item.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.productNameKa,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(ProductImageShape),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(item.productNameKa, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text("× ${item.quantity}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
        Text("₾${"%.2f".format(item.lineTotal)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = PrimaryPurple)
    }
}

package ge.myshop.app.ui.screens.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import ge.myshop.app.domain.model.CartItemSummary
import ge.myshop.app.ui.components.EmptyState
import ge.myshop.app.ui.components.MyShopButton
import ge.myshop.app.ui.components.PriceDisplay
import ge.myshop.app.ui.components.ScreenHeader
import ge.myshop.app.ui.theme.PrimaryPurple
import ge.myshop.app.ui.theme.ProductImageShape

@Composable
fun CartScreen(
    onCheckoutClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { ScreenHeader("კალათა", onBack = onNavigateBack) },
        bottomBar = {
            if (uiState.cartSummary != null && uiState.cartSummary!!.items.isNotEmpty()) {
                CartBottomBar(
                    subtotal = uiState.cartSummary!!.total,
                    itemCount = uiState.cartSummary!!.itemCount,
                    onCheckout = onCheckoutClick,
                )
            }
        },
    ) { padding ->
        val summary = uiState.cartSummary
        if (summary == null || summary.items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = "🛒",
                    title = "კალათა ცარიელია",
                    subtitle = "დაამატეთ პროდუქტი კატალოგიდან",
                )
            }
        } else {
            LazyColumn(
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                items(summary.items, key = { it.id }) { item ->
                    CartItemCard(
                        item = item,
                        onRemove = { viewModel.removeItem(item.id) },
                        onQuantityChange = { newQty -> viewModel.updateQuantity(item.id, item.productId, item.variantId, newQty) },
                    )
                }
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItemSummary,
    onRemove: () -> Unit,
    onQuantityChange: (Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.productName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(72.dp).clip(ProductImageShape),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.productName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 2)
                if (!item.variantLabel.isNullOrBlank()) {
                    Text(item.variantLabel!!, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
                PriceDisplay(price = item.price, modifier = Modifier)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "წაშლა", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilledIconButton(
                        onClick = { if (item.quantity > 1) onQuantityChange(item.quantity - 1) else onRemove() },
                        modifier = Modifier.size(28.dp),
                        colors = FilledIconButtonDefaults.filledIconButtonColors(containerColor = PrimaryPurple.copy(alpha = 0.1f)),
                    ) { Icon(Icons.Filled.Remove, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(14.dp)) }
                    Text(item.quantity.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    FilledIconButton(
                        onClick = { onQuantityChange(item.quantity + 1) },
                        modifier = Modifier.size(28.dp),
                        colors = FilledIconButtonDefaults.filledIconButtonColors(containerColor = PrimaryPurple),
                    ) { Icon(Icons.Filled.Add, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(14.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CartBottomBar(subtotal: Double, itemCount: Int, onCheckout: () -> Unit) {
    Surface(shadowElevation = 8.dp) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("სულ ($itemCount)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                Text("₾${"%.2f".format(subtotal)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryPurple)
            }
            Spacer(Modifier.height(12.dp))
            MyShopButton(
                text = "შეკვეთის განთავსება",
                onClick = onCheckout,
                leadingIcon = Icons.Filled.ShoppingCartCheckout,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

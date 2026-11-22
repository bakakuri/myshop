package ge.myshop.app.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Textsms
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.domain.model.Order
import ge.myshop.app.ui.components.ErrorState
import ge.myshop.app.ui.components.LoadingState
import ge.myshop.app.ui.components.MsCard
import ge.myshop.app.ui.components.MsImage
import ge.myshop.app.ui.components.MsPrimaryButton
import ge.myshop.app.ui.components.MyShopPageHeader
import ge.myshop.app.ui.components.OrderStatusPill
import ge.myshop.app.ui.components.OrderStatusStepper
import ge.myshop.app.ui.components.formatDate
import ge.myshop.app.ui.components.formatGel
import ge.myshop.app.ui.components.formatTime
import ge.myshop.app.ui.components.msText
import ge.myshop.app.ui.theme.MsColor

@Composable
fun OrderDetailScreen(
    orderId: String,
    onChatClick: (conversationId: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: OrderDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val order = state.order

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        MyShopPageHeader(
            order?.let { "შეკვეთა № ${it.orderNumber}" } ?: "შეკვეთა",
            onBack = onNavigateBack,
            subtitle = order?.let { "${formatDate(it.createdAt)} ${formatTime(it.createdAt)}" },
        )
        when {
            state.isLoading -> LoadingState()
            order == null -> ErrorState("შეკვეთა ვერ მოიძებნა", onRetry = onNavigateBack)
            else -> Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Status
                MsCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("შეკვეთის სტატუსი", style = msText(16.sp, FontWeight.ExtraBold), modifier = Modifier.weight(1f))
                        OrderStatusPill(order.status)
                    }
                    Spacer(Modifier.height(14.dp))
                    OrderStatusStepper(order.status)
                }

                // Agreement note from staff (prices, delivery, payment agreed in chat)
                order.agreementNote?.takeIf { it.isNotBlank() }?.let { note ->
                    MsCard(Modifier.fillMaxWidth(), border = MsColor.Purple300, contentPadding = PaddingValues(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Forum, null, tint = MsColor.Purple600, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("შეთანხმებული პირობები", style = msText(15.sp, FontWeight.ExtraBold))
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(note, style = msText(14.sp, FontWeight.Medium, MsColor.InkSoft, lineHeight = 20.sp))
                    }
                }

                ItemsCard(order)
                DeliveryCard(order)

                val convId = state.conversationId ?: order.conversationId
                if (convId != null) {
                    MsPrimaryButton(
                        "ჩათი ადმინისტრატორთან",
                        onClick = { onChatClick(convId) },
                        icon = Icons.Rounded.Textsms,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ItemsCard(order: Order) {
    MsCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Text("პროდუქტები", style = msText(16.sp, FontWeight.ExtraBold))
        Spacer(Modifier.height(10.dp))
        order.items.forEachIndexed { i, item ->
            if (i > 0) Box(Modifier.fillMaxWidth().padding(vertical = 10.dp).height(1.dp).background(MsColor.Divider))
            Row(verticalAlignment = Alignment.CenterVertically) {
                MsImage(item.imageUrl, item.productNameKa, Modifier.size(58.dp), fallbackIcon = Icons.Rounded.ShoppingCart, shape = RoundedCornerShape(14.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.productNameKa, style = msText(14.5.sp, FontWeight.Bold), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    item.variantNameKa?.let { Text(it, style = msText(12.sp, FontWeight.Medium, MsColor.InkSoft)) }
                    Text("${item.quantity} × ${formatGel(item.unitPrice)}", style = msText(12.5.sp, FontWeight.SemiBold, MsColor.InkSoft))
                }
                Text(formatGel(item.lineTotal), style = msText(15.sp, FontWeight.ExtraBold, MsColor.Purple800))
            }
        }
        Box(Modifier.fillMaxWidth().padding(vertical = 12.dp).height(1.dp).background(MsColor.Divider))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ჯამი", style = msText(16.sp, FontWeight.ExtraBold), modifier = Modifier.weight(1f))
            Text(formatGel(order.total), style = msText(24.sp, FontWeight.ExtraBold, MsColor.Purple800))
        }
    }
}

@Composable
private fun DeliveryCard(order: Order) {
    MsCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Text("მიწოდების ინფორმაცია", style = msText(16.sp, FontWeight.ExtraBold))
        Spacer(Modifier.height(8.dp))
        InfoRow(Icons.Rounded.Person, order.customerName)
        InfoRow(Icons.Rounded.Phone, order.customerPhone)
        val place = listOfNotNull(order.deliveryAddress, order.deliveryCity).filter { it.isNotBlank() }.joinToString(", ")
        if (place.isNotBlank()) InfoRow(Icons.Rounded.LocationOn, place)
        order.customerNote?.takeIf { it.isNotBlank() }?.let { InfoRow(Icons.Rounded.ChatBubbleOutline, it) }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(MsColor.Purple100), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MsColor.Purple600, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(text, style = msText(14.sp, FontWeight.SemiBold, MsColor.Ink), modifier = Modifier.padding(top = 6.dp))
    }
}

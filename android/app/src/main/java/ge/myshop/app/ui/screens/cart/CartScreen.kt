package ge.myshop.app.ui.screens.cart

import ge.myshop.app.ui.components.Art3D
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.R
import ge.myshop.app.domain.model.CartItemSummary
import ge.myshop.app.ui.common.LocalApp
import ge.myshop.app.ui.components.DiscountPill
import ge.myshop.app.ui.components.EmptyState
import ge.myshop.app.ui.components.ErrorState
import ge.myshop.app.ui.components.HeaderAction
import ge.myshop.app.ui.components.InfoStrip
import ge.myshop.app.ui.components.LoadingState
import ge.myshop.app.ui.components.MenuSheet
import ge.myshop.app.ui.components.MsCard
import ge.myshop.app.ui.components.MsIconDisc
import ge.myshop.app.ui.components.MsImage
import ge.myshop.app.ui.components.MsOutlinedButton
import ge.myshop.app.ui.components.MsPrimaryButton
import ge.myshop.app.ui.components.MyShopHeader
import ge.myshop.app.ui.components.QuantityStepper
import ge.myshop.app.ui.components.StockLabel
import ge.myshop.app.ui.components.formatGel
import ge.myshop.app.ui.components.msText
import ge.myshop.app.ui.theme.MsColor

@Composable
fun CartScreen(
    onNavigateToCheckout: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToRoute: (String) -> Unit = {},
    viewModel: CartViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val app = LocalApp.current
    var showMenu by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadCart() }
    LaunchedEffect(state.cartSummary?.itemCount) { app.refreshBadges() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            MyShopHeader(
                leading = HeaderAction(Icons.Rounded.Menu, "მენიუ", filled = false) { showMenu = true },
                actions = listOf(HeaderAction(Icons.Rounded.Notifications, "შეტყობინებები", badge = app.badges.notifications, filled = false, onClick = onNavigateToNotifications)),
                bottom = {
                    Row(Modifier.fillMaxWidth().padding(start = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("კალათა", style = msText(32.sp, FontWeight.ExtraBold, Color.White))
                            Text("შეამოწმე პროდუქტები შეკვეთამდე", style = msText(13.sp, FontWeight.Medium, Color.White.copy(alpha = 0.92f)))
                        }
                        Box(Modifier.size(width = 136.dp, height = 104.dp)) {
                            Art3D(R.drawable.td_bags, Modifier.align(Alignment.TopStart).size(62.dp))
                            Art3D(R.drawable.td_cart, Modifier.align(Alignment.BottomEnd).size(104.dp))
                        }
                    }
                },
            )
        }

        when {
            !state.isLoggedIn -> item {
                EmptyState(
                    R.drawable.td_lock,
                    "შედი ანგარიშზე",
                    subtitle = "კალათის სანახავად და შეკვეთისთვის საჭიროა ანგარიში",
                    actionText = "შესვლა",
                    onAction = app.openLogin,
                )
            }
            state.isLoading -> item { LoadingState() }
            state.error != null -> item { ErrorState(state.error!!, onRetry = viewModel::loadCart) }
            state.items.isEmpty() -> item {
                EmptyState(
                    R.drawable.td_cart,
                    "კალათა ცარიელია",
                    subtitle = "დაამატე პროდუქტები და აქ გამოჩნდება",
                    actionText = "შოპინგის დაწყება",
                    onAction = onNavigateToHome,
                )
            }
            else -> {
                items(state.items, key = { it.cartItemId }) { item ->
                    CartItemCard(
                        item = item,
                        onOpen = { onNavigateToProductDetail(item.productId) },
                        onRemove = { viewModel.removeItem(item) },
                        onQuantity = { viewModel.changeQuantity(item, it) },
                        modifier = Modifier.padding(horizontal = 14.dp),
                    )
                }
                item {
                    Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCard(state.cartSummary?.subtotal ?: 0.0, state.cartSummary?.itemCount ?: 0)
                        InfoStrip(
                            listOf(
                                Icons.Rounded.LocalShipping to "მიწოდება: ${app.store.deliveryShort}",
                                Icons.Rounded.VerifiedUser to "უსაფრთხო შეკვეთა",
                            ),
                        )
                        if (state.hasUnavailable) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Warning, null, tint = MsColor.Red, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("ზოგი პროდუქტი აღარ არის მარაგში — წაშალე გასაგრძელებლად", style = msText(13.sp, FontWeight.SemiBold, MsColor.Red))
                            }
                        }
                        MsPrimaryButton(
                            text = "შეკვეთის მოთხოვნის გაგზავნა",
                            onClick = onNavigateToCheckout,
                            icon = Icons.AutoMirrored.Rounded.Send,
                            trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            enabled = !state.hasUnavailable,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        MsOutlinedButton(
                            text = "კალათის გასუფთავება",
                            onClick = { confirmClear = true },
                            icon = Icons.Rounded.DeleteOutline,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }

    if (showMenu) MenuSheet(onDismiss = { showMenu = false }, onNavigate = onNavigateToRoute)
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            containerColor = Color.White,
            title = { Text("კალათის გასუფთავება", style = msText(19.sp, FontWeight.ExtraBold)) },
            text = { Text("ნამდვილად გსურს ყველა პროდუქტის წაშლა კალათიდან?", style = msText(14.sp, FontWeight.Medium, MsColor.InkSoft)) },
            confirmButton = {
                TextButton(onClick = { confirmClear = false; viewModel.clearCart() }) {
                    Text("გასუფთავება", style = msText(15.sp, FontWeight.Bold, MsColor.Red))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("გაუქმება", style = msText(15.sp, FontWeight.Bold, MsColor.Purple600)) }
            },
        )
    }
}

@Composable
private fun CartItemCard(
    item: CartItemSummary,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
    onQuantity: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    MsCard(modifier.fillMaxWidth(), onClick = onOpen) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            MsImage(item.imageUrl, item.productNameKa, Modifier.size(96.dp), fallbackIcon = Icons.Rounded.ShoppingCart)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(item.productNameKa, style = msText(16.sp, FontWeight.ExtraBold), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        item.variantNameKa?.let { Text(it, style = msText(12.5.sp, FontWeight.Medium, MsColor.InkSoft), maxLines = 1) }
                    }
                    MsIconDisc(Icons.Rounded.DeleteOutline, "წაშლა", onRemove, size = 34.dp, background = MsColor.Purple100)
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(formatGel(item.unitPrice), style = msText(18.sp, FontWeight.ExtraBold, MsColor.Purple800))
                    item.previousPrice?.takeIf { it > item.unitPrice }?.let { old ->
                        Text(
                            formatGel(old),
                            style = msText(12.sp, FontWeight.Medium, MsColor.InkMuted).copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                        )
                        DiscountPill((((old - item.unitPrice) / old) * 100).toInt())
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) { StockLabel(item.isAvailable && item.inStock, size = 12.sp) }
                    QuantityStepper(
                        quantity = item.quantity,
                        onDecrement = { onQuantity(item.quantity - 1) },
                        onIncrement = { onQuantity(item.quantity + 1) },
                        height = 40.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(total: Double, count: Int) {
    MsCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.LocalOffer, null, tint = MsColor.Purple600, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("შეკვეთის ჯამი:", style = msText(16.sp, FontWeight.ExtraBold))
                Text("$count პროდუქტი", style = msText(12.sp, FontWeight.Medium, MsColor.InkSoft))
            }
            Text(formatGel(total), style = msText(26.sp, FontWeight.ExtraBold, MsColor.Purple800))
        }
    }
}

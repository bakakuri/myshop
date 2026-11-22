package ge.myshop.app.ui.screens.cart

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.R
import ge.myshop.app.ui.common.LocalApp
import ge.myshop.app.ui.components.EmptyState
import ge.myshop.app.ui.components.HeaderAction
import ge.myshop.app.ui.components.InfoStrip
import ge.myshop.app.ui.components.LoadingState
import ge.myshop.app.ui.components.MsCard
import ge.myshop.app.ui.components.MsImage
import ge.myshop.app.ui.components.MsPrimaryButton
import ge.myshop.app.ui.components.MsTextField
import ge.myshop.app.ui.components.MyShopHeader
import ge.myshop.app.ui.components.formatGel
import ge.myshop.app.ui.components.msShadow
import ge.myshop.app.ui.components.msText
import ge.myshop.app.ui.theme.MsColor

@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onOrderPlaced: (String) -> Unit,
    onNavigateHome: () -> Unit = {},
    viewModel: CheckoutViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val app = LocalApp.current

    LaunchedEffect(state.event) {
        when (val e = state.event) {
            is CheckoutEvent.OrderPlaced -> { viewModel.clearEvent(); app.refreshBadges(); onOrderPlaced(e.orderId) }
            is CheckoutEvent.ShowError -> { viewModel.clearEvent(); app.showMessage(e.message) }
            null -> Unit
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding()) {
        MyShopHeader(
            leading = HeaderAction(Icons.AutoMirrored.Rounded.ArrowBack, "უკან", onClick = onNavigateBack),
            actions = listOf(HeaderAction(Icons.Rounded.Home, "მთავარი", onClick = onNavigateHome)),
            bottom = { ReadyBubble() },
        )

        val summary = state.cartSummary
        when {
            state.isLoading -> LoadingState()
            summary == null || summary.items.isEmpty() -> EmptyState(
                Icons.Rounded.ShoppingCart,
                "კალათა ცარიელია",
                subtitle = "შეკვეთისთვის ჯერ დაამატე პროდუქტები",
                actionText = "მთავარზე დაბრუნება",
                onAction = onNavigateHome,
            )
            else -> Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // ── Order summary
                MsCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 16.dp)) {
                    Text("შეკვეთის შეჯამება", style = msText(17.sp, FontWeight.ExtraBold), modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(12.dp))
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(summary.items, key = { it.cartItemId }) { item ->
                            Row(
                                Modifier
                                    .width(190.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MsColor.Purple100.copy(alpha = 0.6f))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                MsImage(item.imageUrl, null, Modifier.size(56.dp), fallbackIcon = Icons.Rounded.ShoppingCart, shape = RoundedCornerShape(12.dp))
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(item.productNameKa, style = msText(13.sp, FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${item.quantity} ც · ${formatGel(item.lineTotal)}", style = msText(12.sp, FontWeight.SemiBold, MsColor.Purple700))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(1.dp).background(MsColor.Divider))
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("საერთო თანხა:", style = msText(15.sp, FontWeight.Bold), modifier = Modifier.weight(1f))
                        Text(formatGel(summary.subtotal), style = msText(24.sp, FontWeight.ExtraBold, MsColor.Purple800))
                    }
                }

                // ── Delivery info form
                MsCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                    Text("მიწოდების ინფორმაცია", style = msText(17.sp, FontWeight.ExtraBold))
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        MsTextField(state.customerName, viewModel::updateName, "სახელი და გვარი", leadingIcon = Icons.Rounded.Person, error = state.nameError)
                        MsTextField(state.customerPhone, viewModel::updatePhone, "ტელეფონი", leadingIcon = Icons.Rounded.Phone, error = state.phoneError, keyboardType = KeyboardType.Phone)
                        MsTextField(state.customerEmail, viewModel::updateEmail, "ელ-ფოსტა (არასავალდებულო)", leadingIcon = Icons.Rounded.Email, keyboardType = KeyboardType.Email)
                        MsTextField(state.address, viewModel::updateAddress, if (app.store.requireAddress) "მისამართი" else "მისამართი (არასავალდებულო)", leadingIcon = Icons.Rounded.LocationOn, error = state.addressError)
                        MsTextField(state.city, viewModel::updateCity, "ქალაქი", leadingIcon = Icons.Rounded.LocationCity)
                        MsTextField(
                            state.customerNote,
                            viewModel::updateNote,
                            "კომენტარი ადმინისტრატორისთვის",
                            leadingIcon = Icons.Rounded.ChatBubbleOutline,
                            singleLine = false,
                            minLines = 3,
                        )
                    }
                }

                InfoStrip(
                    listOf(
                        Icons.Rounded.LocalShipping to "მიწოდება: ${app.store.deliveryShort}",
                        Icons.Rounded.VerifiedUser to "უსაფრთხო შეკვეთა",
                    ),
                )

                MsPrimaryButton(
                    text = "შეკვეთის მოთხოვნის გაგზავნა",
                    onClick = { viewModel.placeOrder(app.store.requireAddress) },
                    icon = Icons.AutoMirrored.Rounded.Send,
                    trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    loading = state.isPlacing,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Rounded.Info, null, tint = MsColor.Purple500, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "აპში გადახდა არ ხდება. ადმინისტრატორი ჩათში შეგითანხმებს ფასს, მიწოდებას და გადახდის წესს.",
                        style = msText(12.5.sp, FontWeight.Medium, MsColor.InkSoft, lineHeight = 18.sp),
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

/** Header bubble: "შეკვეთის მოთხოვნა მზადაა გასაგზავნად!" (reference 6). */
@Composable
private fun ReadyBubble() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(R.drawable.ill_cart_bags), null, Modifier.size(width = 112.dp, height = 88.dp))
        Spacer(Modifier.width(8.dp))
        Column(
            Modifier
                .weight(1f)
                .msShadow(RoundedCornerShape(20.dp), 8.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(14.dp),
        ) {
            Text("შეკვეთის მოთხოვნა მზადაა გასაგზავნად!", style = msText(15.sp, FontWeight.ExtraBold, MsColor.Purple800, lineHeight = 19.sp))
            Spacer(Modifier.height(4.dp))
            Text("გადაამოწმე ინფორმაცია და გაგზავნე ადმინისტრატორთან.", style = msText(12.sp, FontWeight.Medium, MsColor.InkSoft, lineHeight = 16.sp))
        }
    }
}

package ge.myshop.app.ui.screens.profile

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Textsms
import androidx.compose.material.icons.rounded.Verified
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.R
import ge.myshop.app.ui.common.LocalApp
import ge.myshop.app.ui.components.HeaderAction
import ge.myshop.app.ui.components.IconTile
import ge.myshop.app.ui.components.LoadingState
import ge.myshop.app.ui.components.MenuRow
import ge.myshop.app.ui.components.MsCard
import ge.myshop.app.ui.components.MsImage
import ge.myshop.app.ui.components.MsOutlinedButton
import ge.myshop.app.ui.components.MsPrimaryButton
import ge.myshop.app.ui.components.MyShopHeader
import ge.myshop.app.ui.components.OrderSummaryCard
import ge.myshop.app.ui.components.SectionHeader
import ge.myshop.app.ui.components.formatGel
import ge.myshop.app.ui.components.msText
import ge.myshop.app.ui.theme.MsBrush
import ge.myshop.app.ui.theme.MsColor
import ge.myshop.app.ui.theme.MsShape

@Composable
fun ProfileScreen(
    onOrdersClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onAddressesClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onLoginClick: () -> Unit,
    onChatClick: () -> Unit = {},
    onOrderClick: (String) -> Unit = {},
    onRegisterClick: () -> Unit = onLoginClick,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val app = LocalApp.current
    val context = LocalContext.current
    var confirmLogout by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { viewModel.load() }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        MyShopHeader(
            actions = if (state.isLoggedIn) listOf(HeaderAction(Icons.Rounded.Settings, "პარამეტრები", onClick = onEditProfileClick)) else emptyList(),
        )

        when {
            !state.isLoggedIn -> GuestCard(onLoginClick, onRegisterClick)
            state.isLoading -> LoadingState()
            else -> Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                val p = state.profile
                // ── Identity card
                MsCard(Modifier.fillMaxWidth()) {
                    Box(Modifier.fillMaxWidth()) {
                        Image(
                            painterResource(R.drawable.ill_bags),
                            null,
                            Modifier.align(Alignment.BottomEnd).offset(x = 10.dp, y = 8.dp).size(width = 120.dp, height = 100.dp),
                        )
                        Column(Modifier.padding(16.dp).padding(end = 70.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Avatar(p?.fullName, p?.avatarUrl)
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            p?.fullName?.takeIf { it.isNotBlank() } ?: "მომხმარებელი",
                                            style = msText(19.sp, FontWeight.ExtraBold),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false),
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Icon(Icons.Rounded.Verified, "დადასტურებული", tint = MsColor.Purple500, modifier = Modifier.size(20.dp))
                                    }
                                    (p?.phone ?: p?.email)?.let { InfoLine(if (p?.phone != null) Icons.Rounded.Phone else Icons.Rounded.Person, it) }
                                    p?.city?.let { InfoLine(Icons.Rounded.LocationOn, listOfNotNull(it, p.country).joinToString(", ")) }
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            MsPrimaryButton("პროფილის რედაქტირება", onEditProfileClick, icon = Icons.Rounded.Edit, height = 46.dp)
                        }
                    }
                }

                // ── Stats
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(Icons.Rounded.Receipt, state.orderCount.toString(), "შეკვეთა", Modifier.weight(1f), onOrdersClick)
                    StatCard(Icons.Rounded.Favorite, state.favoriteCount.toString(), "რჩეული", Modifier.weight(1f), onFavoritesClick)
                    StatCard(Icons.Rounded.AccountBalanceWallet, formatGel(state.ordersTotal), "შეკვეთების ჯამი", Modifier.weight(1f), onOrdersClick)
                }

                // ── Menu
                MsCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(6.dp)) {
                    MenuRow(Icons.Rounded.Receipt, "ჩემი შეკვეთები", "ისტორია და სტატუსი", onOrdersClick)
                    MenuRow(Icons.Rounded.Textsms, "ჩათი", "მიმოწერა ადმინისტრატორთან", onChatClick, badge = app.badges.chat)
                    MenuRow(Icons.Rounded.Favorite, "რჩეული პროდუქტები", "შენახული პროდუქტები", onFavoritesClick)
                    MenuRow(Icons.Rounded.LocationOn, "ჩემი მისამართები", "მიწოდების მისამართები", onAddressesClick)
                    MenuRow(Icons.Rounded.Notifications, "შეტყობინებები", "სტატუსები და სიახლეები", onNotificationsClick, badge = app.badges.notifications)
                    MenuRow(Icons.Rounded.HeadsetMic, "დახმარება", "კონტაქტი და სამუშაო საათები", { showHelp = true })
                }

                // ── Recent orders
                if (state.orders.isNotEmpty()) {
                    SectionHeader("ბოლო შეკვეთები", actionText = "ყველა", onAction = onOrdersClick)
                    state.orders.take(2).forEach { o -> OrderSummaryCard(o, onClick = { onOrderClick(o.id) }) }
                }

                MsPrimaryButton(
                    "გამოსვლა",
                    onClick = { confirmLogout = true },
                    icon = Icons.AutoMirrored.Rounded.Logout,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            containerColor = Color.White,
            title = { Text("გამოსვლა", style = msText(19.sp, FontWeight.ExtraBold)) },
            text = { Text("ნამდვილად გსურს ანგარიშიდან გამოსვლა?", style = msText(14.sp, FontWeight.Medium, MsColor.InkSoft)) },
            confirmButton = {
                TextButton(onClick = { confirmLogout = false; viewModel.signOut { app.refreshBadges() } }) {
                    Text("გამოსვლა", style = msText(15.sp, FontWeight.Bold, MsColor.Red))
                }
            },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("გაუქმება", style = msText(15.sp, FontWeight.Bold, MsColor.Purple600)) } },
        )
    }
    if (showHelp) {
        val s = app.store
        AlertDialog(
            onDismissRequest = { showHelp = false },
            containerColor = Color.White,
            title = { Text("დახმარება", style = msText(19.sp, FontWeight.ExtraBold)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOfNotNull(
                        s.phone?.let { Icons.Rounded.Phone to it },
                        s.email?.let { Icons.Rounded.Person to it },
                        s.workingHours?.let { Icons.Rounded.Receipt to it },
                        Icons.Rounded.Textsms to "შეკითხვები შეკვეთაზე — ჩათში",
                    ).forEach { (icon, text) -> InfoLine(icon, text) }
                }
            },
            confirmButton = {
                s.phone?.let { phone ->
                    TextButton(onClick = {
                        showHelp = false
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.filter { it.isDigit() || it == '+' }}")))
                    }) { Text("დარეკვა", style = msText(15.sp, FontWeight.Bold, MsColor.Purple600)) }
                }
            },
            dismissButton = { TextButton(onClick = { showHelp = false }) { Text("დახურვა", style = msText(15.sp, FontWeight.Bold, MsColor.InkSoft)) } },
        )
    }
}

@Composable
private fun Avatar(name: String?, url: String?) {
    Box(
        Modifier.size(78.dp).clip(CircleShape).background(MsBrush.PrimaryButton).padding(3.dp).clip(CircleShape).background(Color.White).padding(3.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (!url.isNullOrBlank()) {
            MsImage(url, name, Modifier.fillMaxSize(), shape = CircleShape)
        } else {
            Box(Modifier.fillMaxSize().clip(CircleShape).background(MsBrush.Lavender), contentAlignment = Alignment.Center) {
                Text(name?.firstOrNull()?.uppercase() ?: "U", style = msText(30.sp, FontWeight.ExtraBold, MsColor.Purple700))
            }
        }
    }
}

@Composable
private fun InfoLine(icon: ImageVector, text: String) {
    Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MsColor.Purple600, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = msText(13.sp, FontWeight.SemiBold, MsColor.InkSoft), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun StatCard(icon: ImageVector, value: String, label: String, modifier: Modifier, onClick: () -> Unit) {
    MsCard(modifier, shape = MsShape.CardSmall, onClick = onClick) {
        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            IconTile(icon, size = 42.dp)
            Spacer(Modifier.height(8.dp))
            Text(value, style = msText(17.sp, FontWeight.ExtraBold, MsColor.Purple800), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, style = msText(11.sp, FontWeight.SemiBold, MsColor.InkSoft), textAlign = TextAlign.Center, maxLines = 2)
        }
    }
}

@Composable
private fun GuestCard(onLogin: () -> Unit, onRegister: () -> Unit) {
    MsCard(Modifier.padding(14.dp).fillMaxWidth(), contentPadding = PaddingValues(20.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.ill_bags), null, Modifier.size(width = 150.dp, height = 125.dp))
            Spacer(Modifier.height(10.dp))
            Text("კეთილი იყოს შენი მობრძანება!", style = msText(20.sp, FontWeight.ExtraBold), textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(
                "შედი ანგარიშზე, რომ გამოიყენო კალათა, შეკვეთები, რჩეულები და ჩათი.",
                style = msText(14.sp, FontWeight.Medium, MsColor.InkSoft, lineHeight = 20.sp),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(18.dp))
            MsPrimaryButton("შესვლა", onLogin, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            MsOutlinedButton("რეგისტრაცია", onRegister, modifier = Modifier.fillMaxWidth())
        }
    }
}

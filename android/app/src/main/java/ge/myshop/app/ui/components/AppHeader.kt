package ge.myshop.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ge.myshop.app.ui.theme.LogoTextStyle
import ge.myshop.app.ui.theme.PrimaryPurple

/**
 * Home tab app bar — purple background, MYSHOP logo, search/notifications/cart icons with badges.
 */
@Composable
fun AppHeader(
    cartCount: Int = 0,
    notificationCount: Int = 0,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onNotificationsClick: () -> Unit,
) {
    Surface(color = PrimaryPurple, shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = "MYSHOP",
                style = LogoTextStyle,
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Filled.Search, contentDescription = "ძებნა", tint = Color.White)
            }
            BadgedBox(badge = { if (notificationCount > 0) Badge { Text(notificationCount.toString()) } }) {
                IconButton(onClick = onNotificationsClick) {
                    Icon(Icons.Filled.Notifications, contentDescription = "შეტყობინებები", tint = Color.White)
                }
            }
            BadgedBox(badge = { if (cartCount > 0) Badge { Text(cartCount.toString()) } }) {
                IconButton(onClick = onCartClick) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = "კალათა", tint = Color.White)
                }
            }
        }
    }
}

/** Alias kept for backward compatibility with HomeScreen.kt */
@Composable
fun HomeAppHeader(
    cartCount: Int = 0,
    notificationCount: Int = 0,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onNotificationsClick: () -> Unit,
) = AppHeader(cartCount, notificationCount, onSearchClick, onCartClick, onNotificationsClick)

/**
 * Screen-level app bar — purple background, back arrow (optional), centered title, optional actions slot.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.SemiBold, color = Color.White) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "უკან", tint = Color.White)
                }
            }
        },
        actions = { actions?.invoke() },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryPurple),
        windowInsets = WindowInsets(0),
    )
}

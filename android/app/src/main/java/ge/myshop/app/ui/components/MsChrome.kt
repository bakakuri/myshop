package ge.myshop.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Textsms
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Textsms
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ge.myshop.app.R
import ge.myshop.app.ui.theme.MsBrush
import ge.myshop.app.ui.theme.MsColor
import ge.myshop.app.ui.theme.MsShape
import ge.myshop.app.ui.theme.MyShopFont

// ============================================================
// App chrome: logo, gradient header, search bar, floating bottom nav
// ============================================================

@Composable
fun MyShopLogo(modifier: Modifier = Modifier, showTagline: Boolean = true) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Art3D(R.drawable.td_logo_cart, Modifier.size(54.dp))
        Spacer(Modifier.width(6.dp))
        Column {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White)) { append("My") }
                    withStyle(SpanStyle(color = MsColor.Gold)) { append("Shop") }
                },
                style = TextStyle(fontFamily = MyShopFont, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, lineHeight = 32.sp),
            )
            if (showTagline) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "შენი ონლაინ მაღაზია",
                        style = TextStyle(fontFamily = MyShopFont, fontWeight = FontWeight.Medium, fontSize = 11.5.sp, color = Color.White),
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Outlined.FavoriteBorder, null, tint = Color.White, modifier = Modifier.size(13.dp))
                }
            }
        }
    }
}

/** A circular header button. [filled] = dark purple disc (back/search), otherwise a bare white icon (menu/bell). */
data class HeaderAction(
    val icon: ImageVector,
    val contentDescription: String,
    val badge: Int = 0,
    val filled: Boolean = true,
    val onClick: () -> Unit,
)

@Composable
fun HeaderCircleButton(action: HeaderAction, modifier: Modifier = Modifier) {
    Box(modifier.size(46.dp)) {
        Box(
            Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (action.filled) MsColor.HeaderButton.copy(alpha = 0.85f) else Color.Transparent)
                .clickable(role = Role.Button, onClickLabel = action.contentDescription, onClick = action.onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(action.icon, action.contentDescription, tint = Color.White, modifier = Modifier.size(if (action.filled) 24.dp else 28.dp))
        }
        if (action.badge > 0) CountBadge(action.badge, Modifier.align(Alignment.TopEnd).offset(x = 2.dp, y = (-2).dp))
    }
}

@Composable
fun CountBadge(count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier
            .defaultMinSize(minWidth = 19.dp, minHeight = 19.dp)
            .clip(CircleShape)
            .background(MsColor.Badge)
            .border(1.5.dp, Color.White, CircleShape)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (count > 99) "99+" else count.toString(),
            style = TextStyle(fontFamily = MyShopFont, fontWeight = FontWeight.Bold, fontSize = 10.5.sp, color = Color.White, lineHeight = 12.sp),
        )
    }
}

/**
 * Purple gradient header that runs under the status bar and fades into the lavender
 * background (reference screens 1–8). [center] defaults to the MyShop logo.
 */
@Composable
fun MyShopHeader(
    modifier: Modifier = Modifier,
    leading: HeaderAction? = null,
    actions: List<HeaderAction> = emptyList(),
    center: (@Composable () -> Unit)? = null,
    bottom: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(MsBrush.Header)
            .statusBarsPadding()
            .padding(horizontal = 14.dp)
            .padding(top = 4.dp, bottom = if (bottom != null) 10.dp else 18.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(68.dp)) {
            leading?.let { HeaderCircleButton(it, Modifier.align(Alignment.CenterStart)) }
            Box(Modifier.align(Alignment.Center)) { center?.invoke() ?: MyShopLogo() }
            Row(Modifier.align(Alignment.CenterEnd), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                actions.forEach { HeaderCircleButton(it) }
            }
        }
        if (bottom != null) {
            Spacer(Modifier.height(8.dp))
            bottom()
        }
    }
}

// ── Search bar ───────────────────────────────────────────────

/** Read-only search pill that opens the search screen (home / categories). */
@Composable
fun MsSearchBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "მოძებნე პროდუქტი...",
    onFilterClick: (() -> Unit)? = null,
) {
    SearchPillFrame(modifier, onFilterClick, onClick) {
        Text(placeholder, style = TextStyle(fontFamily = MyShopFont, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = MsColor.InkSoft))
    }
}

/** Editable search pill (search screen). */
@Composable
fun MsSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "მოძებნე პროდუქტი...",
    onSearch: () -> Unit = {},
    onFilterClick: (() -> Unit)? = null,
) {
    SearchPillFrame(modifier, onFilterClick) {
        Box(contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text(placeholder, style = TextStyle(fontFamily = MyShopFont, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = MsColor.InkSoft))
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(fontFamily = MyShopFont, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = MsColor.Ink),
                cursorBrush = SolidColor(MsColor.Purple600),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SearchPillFrame(
    modifier: Modifier,
    onFilterClick: (() -> Unit)?,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(10.dp, MsShape.Pill, ambientColor = MsColor.Purple600.copy(alpha = 0.2f), spotColor = MsColor.Purple600.copy(alpha = 0.25f))
            .clip(MsShape.Pill)
            .background(Color(0xFFF3F0FF))
            .border(1.5.dp, Color.White, MsShape.Pill)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClickLabel = "ძებნა", onClick = onClick) else Modifier)
            .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Search, contentDescription = null, tint = MsColor.Purple700, modifier = Modifier.size(27.dp))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) { content() }
        if (onFilterClick != null) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(MsBrush.PrimaryButton)
                    .clickable(role = Role.Button, onClickLabel = "ფილტრი", onClick = onFilterClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Tune, contentDescription = "ფილტრი", tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
    }
}

// ── Bottom navigation (floating white card) ─────────────────

enum class MainTab(val label: String, val selectedIcon: ImageVector, val icon: ImageVector) {
    Home("მთავარი", Icons.Rounded.Home, Icons.Outlined.Home),
    Categories("კატეგორიები", Icons.Rounded.GridView, Icons.Outlined.GridView),
    Cart("კალათა", Icons.Rounded.ShoppingCart, Icons.Outlined.ShoppingCart),
    Chat("ჩათი", Icons.Rounded.Textsms, Icons.Outlined.Textsms),
    Profile("პროფილი", Icons.Rounded.Person, Icons.Outlined.Person),
}

@Composable
fun MsBottomNav(
    selected: MainTab?,
    cartCount: Int,
    chatCount: Int,
    onSelect: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp)
            .padding(bottom = 8.dp, top = 4.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shadow(18.dp, RoundedCornerShape(28.dp), ambientColor = MsColor.Purple600.copy(alpha = 0.18f), spotColor = MsColor.Purple600.copy(alpha = 0.28f))
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MainTab.entries.forEach { tab ->
                val isSelected = tab == selected
                val badge = when (tab) {
                    MainTab.Cart -> cartCount
                    MainTab.Chat -> chatCount
                    else -> 0
                }
                NavItem(tab, isSelected, badge, Modifier.weight(1f)) { onSelect(tab) }
            }
        }
    }
}

@Composable
private fun NavItem(tab: MainTab, selected: Boolean, badge: Int, modifier: Modifier, onClick: () -> Unit) {
    val indicatorAlpha by animateFloatAsState(if (selected) 1f else 0f, label = "navIndicator")
    val labelColor by animateColorAsState(if (selected) MsColor.Purple600 else MsColor.Ink, label = "navLabel")
    Column(
        modifier
            .height(72.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box {
            Icon(
                if (selected) tab.selectedIcon else tab.icon,
                contentDescription = tab.label,
                tint = MsColor.Purple600,
                modifier = Modifier.size(28.dp),
            )
            if (badge > 0) CountBadge(badge, Modifier.align(Alignment.TopEnd).offset(x = 10.dp, y = (-6).dp))
        }
        Spacer(Modifier.height(3.dp))
        Text(
            tab.label,
            maxLines = 1,
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontFamily = MyShopFont,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                fontSize = 11.sp,
                color = labelColor,
            ),
        )
        Spacer(Modifier.height(5.dp))
        Box(
            Modifier
                .alpha(indicatorAlpha)
                .width(46.dp)
                .height(3.5.dp)
                .clip(MsShape.Pill)
                .background(MsBrush.Indicator),
        )
    }
}

/** Bottom padding so scrolling content clears the floating nav. */
val BottomNavClearance: Dp = 8.dp

/** Gradient header with back button and a big white page title (orders, favourites, addresses…). */
@Composable
fun MyShopPageHeader(
    title: String,
    onBack: (() -> Unit)?,
    subtitle: String? = null,
    actions: List<HeaderAction> = emptyList(),
) {
    MyShopHeader(
        leading = onBack?.let { HeaderAction(Icons.AutoMirrored.Rounded.ArrowBack, "უკან", onClick = it) },
        actions = actions,
        bottom = {
            Column(Modifier.padding(start = 6.dp, bottom = 6.dp)) {
                Text(title, style = TextStyle(fontFamily = MyShopFont, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = Color.White))
                subtitle?.let {
                    Text(it, style = TextStyle(fontFamily = MyShopFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f)))
                }
            }
        },
    )
}

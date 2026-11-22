package ge.myshop.app.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================================
// MYSHOP Spacing — 4dp base grid system
// ============================================================

data class MyShopSpacing(
    // Base 4dp increments
    val xxxs: Dp = 2.dp,
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 40.dp,
    val huge: Dp = 48.dp,
    val massive: Dp = 64.dp,

    // Semantic spacing
    val screenPadding: Dp = 16.dp,         // Horizontal screen padding
    val screenPaddingLarge: Dp = 20.dp,
    val cardPadding: Dp = 16.dp,           // Inside card padding
    val cardPaddingSmall: Dp = 12.dp,
    val sectionSpacing: Dp = 24.dp,        // Between home sections
    val itemSpacing: Dp = 12.dp,           // Between list items
    val chipSpacing: Dp = 8.dp,            // Between chips/badges
    val iconTextSpacing: Dp = 8.dp,        // Between icon and text
    val buttonPaddingV: Dp = 14.dp,        // Button vertical padding
    val buttonPaddingH: Dp = 24.dp,        // Button horizontal padding
    val bottomNavHeight: Dp = 72.dp,       // Bottom navigation bar height
    val appBarHeight: Dp = 60.dp,          // App bar height
    val productCardWidth: Dp = 160.dp,     // Product card width in grid
    val productImageAspect: Dp = 160.dp,   // Product image height
    val categoryCardSize: Dp = 80.dp,      // Category shortcut size
    val avatarSize: Dp = 40.dp,            // Standard avatar size
    val avatarSizeLarge: Dp = 60.dp,
    val badgeSize: Dp = 20.dp,             // Notification badge
    val iconSize: Dp = 24.dp,              // Standard icon
    val iconSizeSmall: Dp = 20.dp,
    val iconSizeLarge: Dp = 32.dp,
    val divider: Dp = 1.dp,
    val bottomSheetPeekHeight: Dp = 300.dp,
)

val LocalMyShopSpacing = compositionLocalOf { MyShopSpacing() }

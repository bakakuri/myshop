package ge.myshop.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ============================================================
// MYSHOP Shapes — rounded corner system from screenshots
// Design language: generous rounding, pill buttons, soft cards
// ============================================================

val MyShopShapes = Shapes(
    // Extra small: chips, badges, small tags
    extraSmall = RoundedCornerShape(6.dp),

    // Small: input fields, small cards
    small = RoundedCornerShape(10.dp),

    // Medium: product cards, standard cards
    medium = RoundedCornerShape(16.dp),

    // Large: bottom sheets, drawers, dialogs
    large = RoundedCornerShape(24.dp),

    // Extra large: hero banners, full-width cards
    extraLarge = RoundedCornerShape(32.dp),
)

// ============================================================
// Extended shape tokens not in Material3 defaults
// ============================================================

// Fully rounded — pill buttons, search bar, bottom nav active indicator
val PillShape = RoundedCornerShape(50)

// Product image corner
val ProductImageShape = RoundedCornerShape(12.dp)

// Category card corner
val CategoryCardShape = RoundedCornerShape(16.dp)

// Bottom navigation bar shape
val BottomNavShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)

// FAB / action button (slightly pill-ish)
val ActionButtonShape = RoundedCornerShape(14.dp)

// Image thumbnail in cart / order list
val ThumbnailShape = RoundedCornerShape(8.dp)

// Banner card — hero
val BannerShape = RoundedCornerShape(20.dp)

// Chat bubble — customer (right side)
val ChatBubbleCustomerShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 4.dp,
    bottomEnd = 16.dp,
    bottomStart = 16.dp,
)

// Chat bubble — staff (left side)
val ChatBubbleStaffShape = RoundedCornerShape(
    topStart = 4.dp,
    topEnd = 16.dp,
    bottomEnd = 16.dp,
    bottomStart = 16.dp,
)

// Discount badge — small pill
val BadgeShape = RoundedCornerShape(50)

// Tab indicator pill
val TabIndicatorShape = RoundedCornerShape(50)

// Search bar
val SearchBarShape = RoundedCornerShape(50)

// Modal / dialog bottom sheet
val BottomSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

// Notification card
val NotificationCardShape = RoundedCornerShape(12.dp)

// Avatar circle
val AvatarShape = RoundedCornerShape(50)

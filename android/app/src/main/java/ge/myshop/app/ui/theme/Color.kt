package ge.myshop.app.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// MYSHOP Design System — Color Tokens
// Source of truth: 8 supplied reference screenshots
// ============================================================

// --- Primary Brand ---
val PrimaryPurple = Color(0xFF5B21B6)        // Deep purple main
val PrimaryPurpleMedium = Color(0xFF7C3AED)  // Medium purple
val PrimaryPurpleDark = Color(0xFF4C1D95)    // Dark purple
val PrimaryPurpleLight = Color(0xFF8B5CF6)   // Light purple

// --- Accent Gold ---
val AccentGold = Color(0xFFF59E0B)
val AccentGoldLight = Color(0xFFFBBF24)
val AccentGoldDark = Color(0xFFD97706)

// --- Surfaces ---
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceLavender = Color(0xFFEDE9FE)      // Light lavender card bg
val SurfaceLavenderTint = Color(0xFFF5F3FF)  // Barely purple tint
val BackgroundLight = Color(0xFFF3F0FF)      // App background (very light lavender)

// --- Gradient Start/End ---
val GradientStart = Color(0xFF7C3AED)
val GradientEnd = Color(0xFF4C1D95)
val HeaderGradientStart = Color(0xFF6D28D9)
val HeaderGradientEnd = Color(0xFF4C1D95)

// --- Text ---
val TextPrimary = Color(0xFF1E1B4B)          // Very dark indigo
val TextSecondary = Color(0xFF6B7280)        // Medium grey
val TextMuted = Color(0xFF9CA3AF)            // Light grey
val TextOnPrimary = Color(0xFFFFFFFF)        // White on purple
val TextPrice = Color(0xFF4C1D95)            // Deep purple prices
val TextPriceOld = Color(0xFF9CA3AF)         // Strikethrough old price
val TextLink = Color(0xFF5B21B6)             // Purple links

// --- Semantic ---
val ColorDiscount = Color(0xFFEC4899)        // Hot pink discount badge
val ColorDiscountDark = Color(0xFFDB2777)
val ColorSuccess = Color(0xFF10B981)         // Green (in stock)
val ColorSuccessLight = Color(0xFFD1FAE5)
val ColorError = Color(0xFFEF4444)           // Red
val ColorErrorLight = Color(0xFFFEE2E2)
val ColorWarning = Color(0xFFF59E0B)
val ColorRating = Color(0xFFF59E0B)          // Gold stars
val ColorBadgeRed = Color(0xFFEF4444)        // Notification badges

// --- Promo Banner Colors ---
val BannerBlueStart = Color(0xFF2563EB)
val BannerBlueEnd = Color(0xFF1D4ED8)
val BannerPinkStart = Color(0xFFDB2777)
val BannerPinkEnd = Color(0xFF9333EA)

// --- Category Card Pastel Backgrounds (from screenshots) ---
val CategoryPastelLavender = Color(0xFFEDE9FE)
val CategoryPastelPink = Color(0xFFFCE7F3)
val CategoryPastelYellow = Color(0xFFFEF9C3)
val CategoryPastelMint = Color(0xFFD1FAE5)
val CategoryPastelBlue = Color(0xFFDBEAFE)
val CategoryPastelPeach = Color(0xFFFFEDD5)
val CategoryPastelPurple = CategoryPastelLavender
val CategoryPastelGreen = CategoryPastelMint
val CategoryPastelOrange = CategoryPastelPeach
val SurfaceVariant = SurfaceLavenderTint

val categoryPastels = listOf(
    CategoryPastelLavender,
    CategoryPastelPink,
    CategoryPastelYellow,
    CategoryPastelMint,
    CategoryPastelBlue,
    CategoryPastelPeach
)

// --- Bottom Nav ---
val NavActive = PrimaryPurple
val NavInactive = Color(0xFF9CA3AF)
val NavBackground = SurfaceWhite

// --- Dividers & Borders ---
val DividerColor = Color(0xFFE5E7EB)
val BorderLight = Color(0xFFE5E7EB)
val BorderMedium = Color(0xFFD1D5DB)

// --- Shadows ---
val ShadowColor = Color(0x1A5B21B6)          // Purple-tinted shadow
val ShadowColorNeutral = Color(0x0F000000)

// --- Convenience aliases used across screens ---
val SuccessGreen = ColorSuccess              // alias for OrderSuccessScreen etc.

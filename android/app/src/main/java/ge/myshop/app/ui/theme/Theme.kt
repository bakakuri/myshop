package ge.myshop.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ============================================================
// MYSHOP Color Scheme — purple/gold with lavender surfaces
// ============================================================

private val MyShopColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = PrimaryPurpleLight,
    onPrimaryContainer = PrimaryPurpleDark,

    secondary = PrimaryPurpleMedium,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = PrimaryPurpleDark,

    tertiary = AccentGold,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF78350F),

    background = BackgroundLight,
    onBackground = TextPrimary,

    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,

    outline = BorderLight,
    outlineVariant = Color(0xFFEDE9FE),

    error = Color(0xFFEF4444),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),

    inverseSurface = PrimaryPurpleDark,
    inverseOnSurface = Color.White,
    inversePrimary = PrimaryPurpleLight,

    scrim = Color.Black,
)

// ============================================================
// Extended Colors — semantic tokens for MYSHOP-specific uses
// ============================================================

data class MyShopExtendedColors(
    val discount: Color,
    val onDiscount: Color,
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val rating: Color,
    val price: Color,
    val priceOld: Color,
    val priceDiscount: Color,
    val badgeRed: Color,
    val chatBubbleCustomer: Color,
    val chatBubbleStaff: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val shimmer: Color,
    val shimmerHighlight: Color,
    val categoryCard1: Color,
    val categoryCard2: Color,
    val categoryCard3: Color,
    val categoryCard4: Color,
    val categoryCard5: Color,
    val categoryCard6: Color,
    val statusNew: Color,
    val statusAgreement: Color,
    val statusAgreed: Color,
    val statusPreparing: Color,
    val statusShipped: Color,
    val statusDelivered: Color,
    val statusCancelled: Color,
)

val MyShopLightExtendedColors = MyShopExtendedColors(
    discount = ColorDiscount,
    onDiscount = Color.White,
    success = ColorSuccess,
    onSuccess = Color.White,
    warning = AccentGold,
    onWarning = Color.White,
    rating = ColorRating,
    price = TextPrice,
    priceOld = TextPriceOld,
    priceDiscount = ColorDiscount,
    badgeRed = ColorBadgeRed,
    chatBubbleCustomer = PrimaryPurple,
    chatBubbleStaff = Color.White,
    gradientStart = PrimaryPurple,
    gradientEnd = PrimaryPurpleMedium,
    shimmer = Color(0xFFE8E0F0),
    shimmerHighlight = Color(0xFFF5F0FF),
    categoryCard1 = CategoryPastelPurple,
    categoryCard2 = CategoryPastelBlue,
    categoryCard3 = CategoryPastelGreen,
    categoryCard4 = CategoryPastelOrange,
    categoryCard5 = CategoryPastelPink,
    categoryCard6 = CategoryPastelYellow,
    statusNew = Color(0xFF3B82F6),
    statusAgreement = Color(0xFFF59E0B),
    statusAgreed = Color(0xFF8B5CF6),
    statusPreparing = Color(0xFF6366F1),
    statusShipped = Color(0xFF0EA5E9),
    statusDelivered = Color(0xFF10B981),
    statusCancelled = Color(0xFFEF4444),
)

val LocalMyShopExtendedColors = staticCompositionLocalOf { MyShopLightExtendedColors }

// ============================================================
// MaterialTheme extension for easy access
// ============================================================

val MaterialTheme.myShopColors: MyShopExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalMyShopExtendedColors.current

val MaterialTheme.spacing: MyShopSpacing
    @Composable
    @ReadOnlyComposable
    get() = LocalMyShopSpacing.current

// ============================================================
// MyShopTheme composable
// ============================================================

@Composable
fun MyShopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // MYSHOP is light-only by design — the purple-on-white palette
    // is the brand identity. Dark mode is not planned for v1.
    val colorScheme = MyShopColorScheme
    val extendedColors = MyShopLightExtendedColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Purple status bar matching the app header
            @Suppress("DEPRECATION")
            window.statusBarColor = PrimaryPurple.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            // Navigation bar: white to match bottom nav
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.White.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    CompositionLocalProvider(
        LocalMyShopExtendedColors provides extendedColors,
        LocalMyShopSpacing provides MyShopSpacing(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MyShopTypography,
            shapes = MyShopShapes,
            content = content,
        )
    }
}

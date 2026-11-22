package ge.myshop.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ge.myshop.app.R

// ============================================================
// MYSHOP Typography — Georgian-first font system
// Primary: BPG Arial / FiraGO for excellent Georgian glyph support
// Fallback: system default
// ============================================================

// Font family with Georgian support
// Fonts are loaded from res/font/
val MyShopFontFamily = FontFamily(
    Font(R.font.firago_regular, FontWeight.Normal),
    Font(R.font.firago_medium, FontWeight.Medium),
    Font(R.font.firago_semibold, FontWeight.SemiBold),
    Font(R.font.firago_bold, FontWeight.Bold),
    Font(R.font.firago_extrabold, FontWeight.ExtraBold),
)

val MyShopTypography = Typography(
    // Display — large hero text
    displayLarge = TextStyle(
        fontFamily = MyShopFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = MyShopFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = MyShopFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),

    // Headlines
    headlineLarge = TextStyle(
        fontFamily = MyShopFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = MyShopFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = MyShopFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
    ),

    // Titles
    titleLarge = TextStyle(
        fontFamily = MyShopFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = MyShopFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = MyShopFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),

    // Body
    bodyLarge = TextStyle(
        fontFamily = MyShopFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = MyShopFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = MyShopFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
    ),

    // Labels
    labelLarge = TextStyle(
        fontFamily = MyShopFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = MyShopFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = MyShopFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
    ),
)

// Extra text styles
val PriceTextStyle = TextStyle(
    fontFamily = MyShopFontFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 20.sp,
    color = TextPrice,
)

val PriceLargeTextStyle = TextStyle(
    fontFamily = MyShopFontFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 28.sp,
    color = TextPrice,
)

val PriceOldTextStyle = TextStyle(
    fontFamily = MyShopFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    color = TextPriceOld,
)

val LogoTextStyle = TextStyle(
    fontFamily = MyShopFontFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 24.sp,
)

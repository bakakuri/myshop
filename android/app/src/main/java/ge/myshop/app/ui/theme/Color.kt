package ge.myshop.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================
// MYSHOP design tokens — measured from the 8 reference screens
// ============================================================

object MsColor {
    // Brand purple (violet-indigo, as in header / buttons / prices)
    val Purple900 = Color(0xFF2A0B86)
    val Purple800 = Color(0xFF3A12A8)   // prices, headings accent
    val Purple700 = Color(0xFF4A1DD0)   // button gradient start
    val Purple600 = Color(0xFF5B2BE0)   // primary
    val Purple500 = Color(0xFF6D3BF2)   // button gradient end, active icons
    val Purple400 = Color(0xFF8B5CF6)
    val Purple300 = Color(0xFFB9A3FF)
    val Purple200 = Color(0xFFDCD2FF)   // borders on lavender
    val Purple100 = Color(0xFFEDE8FF)   // lavender fill (inputs, steppers, chips)
    val Purple50 = Color(0xFFF6F3FF)    // app background

    // Header
    val HeaderTop = Color(0xFF3410A6)
    val HeaderMid = Color(0xFF5226DC)
    val HeaderLow = Color(0xFF7A52F4)
    val HeaderButton = Color(0xFF2E0F8F)  // dark circular header buttons

    // Gold (logo "Shop", CTA pills)
    val Gold = Color(0xFFFFC233)
    val GoldDeep = Color(0xFFF59E0B)
    val GoldLight = Color(0xFFFFD966)

    // Text
    val Ink = Color(0xFF1B1446)          // primary text
    val InkSoft = Color(0xFF4B4673)      // secondary text
    val InkMuted = Color(0xFF8E8AAE)     // muted / old price
    val OnPurple = Color.White

    // Surfaces
    val Background = Purple50
    val Surface = Color.White
    val CardBorder = Color(0xFFECE6FD)
    val Divider = Color(0xFFE9E4FB)

    // Status / accents
    val Green = Color(0xFF16A34A)
    val GreenSoft = Color(0xFFE3F8EA)
    val GreenBorder = Color(0xFFB8EBC9)
    val Pink = Color(0xFFF0388A)         // discount pill (cart)
    val Red = Color(0xFFEF2D4B)          // discount pill (detail) / badges
    val Badge = Color(0xFFEF3340)
    val Star = Color(0xFFFFB400)
    val Orange = Color(0xFFFF8A00)
    val Blue = Color(0xFF2F6BFF)

    // Category pastel cards (references: categories grid)
    val Pastels = listOf(
        Color(0xFFE6E4FF), // lavender-blue
        Color(0xFFFCE4F3), // pink
        Color(0xFFFFEFD6), // peach
        Color(0xFFDDF5EA), // mint
        Color(0xFFDDEBFF), // sky
        Color(0xFFFFE7DD), // apricot
        Color(0xFFFBE0F0), // rose
        Color(0xFFEAE3FF), // lilac
    )
}

object MsBrush {
    val Header = Brush.verticalGradient(
        0.00f to MsColor.HeaderTop,
        0.55f to MsColor.HeaderMid,
        0.82f to MsColor.HeaderLow,
        1.00f to MsColor.Background,
    )
    val PrimaryButton = Brush.horizontalGradient(listOf(MsColor.Purple700, MsColor.Purple500))
    val PrimaryButtonPressed = Brush.horizontalGradient(listOf(MsColor.Purple800, MsColor.Purple600))
    val Gold = Brush.verticalGradient(listOf(MsColor.GoldLight, MsColor.GoldDeep))
    val LogoShop = Brush.verticalGradient(listOf(Color(0xFFFFDE59), Color(0xFFF7A21B)))
    val HeroBanner = Brush.linearGradient(listOf(Color(0xFF4317C9), Color(0xFF7B3FF2), Color(0xFF9B5CFF)))
    val PromoBlue = Brush.linearGradient(listOf(Color(0xFF2050E8), Color(0xFF4B2BD8)))
    val PromoPink = Brush.linearGradient(listOf(Color(0xFFE23E9A), Color(0xFF8E2BE0)))
    val ImageWell = Brush.linearGradient(listOf(Color(0xFFF1ECFF), Color(0xFFFFFFFF), Color(0xFFEDE6FF)))
    val Lavender = Brush.verticalGradient(listOf(Color(0xFFF3EFFF), Color(0xFFE9E2FF)))
    val Indicator = Brush.horizontalGradient(listOf(MsColor.Purple700, MsColor.Purple400))
}

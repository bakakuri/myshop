package ge.myshop.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ge.myshop.app.R

// Poppins (Latin, digits) merged with Noto Sans Georgian (Georgian, ₾) — see android/fonts-license
val MyShopFont = FontFamily(
    Font(R.font.myshop_regular, FontWeight.Normal),
    Font(R.font.myshop_medium, FontWeight.Medium),
    Font(R.font.myshop_semibold, FontWeight.SemiBold),
    Font(R.font.myshop_bold, FontWeight.Bold),
    Font(R.font.myshop_extrabold, FontWeight.ExtraBold),
)

private fun style(size: Int, weight: FontWeight, line: Int = (size * 1.3f).toInt()) = TextStyle(
    fontFamily = MyShopFont,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    color = MsColor.Ink,
)

val MyShopTypography = Typography(
    displayLarge = style(32, FontWeight.ExtraBold),     // big prices
    displayMedium = style(28, FontWeight.ExtraBold),
    displaySmall = style(24, FontWeight.ExtraBold),
    headlineLarge = style(26, FontWeight.ExtraBold),    // screen hero titles
    headlineMedium = style(22, FontWeight.ExtraBold),
    headlineSmall = style(19, FontWeight.ExtraBold),
    titleLarge = style(18, FontWeight.Bold),            // section titles
    titleMedium = style(16, FontWeight.Bold),           // card titles
    titleSmall = style(14, FontWeight.Bold),
    bodyLarge = style(16, FontWeight.Medium),
    bodyMedium = style(14, FontWeight.Medium),
    bodySmall = style(12, FontWeight.Medium),
    labelLarge = style(15, FontWeight.Bold),            // buttons
    labelMedium = style(12, FontWeight.SemiBold),
    labelSmall = style(11, FontWeight.SemiBold),
)

package ge.myshop.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// The MYSHOP identity is light purple/white only (per the reference screens).
private val MyShopColorScheme = lightColorScheme(
    primary = MsColor.Purple600,
    onPrimary = MsColor.OnPurple,
    primaryContainer = MsColor.Purple100,
    onPrimaryContainer = MsColor.Purple800,
    secondary = MsColor.Purple400,
    onSecondary = MsColor.OnPurple,
    secondaryContainer = MsColor.Purple100,
    onSecondaryContainer = MsColor.Purple800,
    tertiary = MsColor.GoldDeep,
    onTertiary = MsColor.Ink,
    background = MsColor.Background,
    onBackground = MsColor.Ink,
    surface = MsColor.Surface,
    onSurface = MsColor.Ink,
    surfaceVariant = MsColor.Purple100,
    onSurfaceVariant = MsColor.InkSoft,
    surfaceContainer = MsColor.Surface,
    surfaceContainerLow = MsColor.Surface,
    surfaceContainerHigh = MsColor.Surface,
    surfaceContainerHighest = MsColor.Purple100,
    outline = MsColor.Purple200,
    outlineVariant = MsColor.Divider,
    error = MsColor.Red,
    onError = MsColor.OnPurple,
)

object MsShape {
    val Card = RoundedCornerShape(22.dp)
    val CardSmall = RoundedCornerShape(18.dp)
    val Image = RoundedCornerShape(16.dp)
    val Pill = RoundedCornerShape(50)
    val Badge = RoundedCornerShape(12.dp)
    val Sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
}

@Composable
fun MyShopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MyShopColorScheme,
        typography = MyShopTypography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(10.dp),
            small = RoundedCornerShape(14.dp),
            medium = RoundedCornerShape(18.dp),
            large = RoundedCornerShape(22.dp),
            extraLarge = RoundedCornerShape(28.dp),
        ),
        content = content,
    )
}

package ge.myshop.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import ge.myshop.app.domain.model.Product
import ge.myshop.app.ui.theme.MsBrush
import ge.myshop.app.ui.theme.MsColor
import ge.myshop.app.ui.theme.MsShape
import ge.myshop.app.ui.theme.MyShopFont
import java.util.Locale
import kotlin.math.roundToInt

// ============================================================
// Text helpers
// ============================================================

fun msText(size: TextUnit, weight: FontWeight = FontWeight.Medium, color: Color = MsColor.Ink, lineHeight: TextUnit = TextUnit.Unspecified) =
    TextStyle(fontFamily = MyShopFont, fontWeight = weight, fontSize = size, color = color, lineHeight = lineHeight)

/** 2299.0 → "2,299 ₾", 12.5 → "12.50 ₾" */
fun formatGel(amount: Double): String {
    val whole = amount % 1.0 == 0.0
    val num = if (whole) String.format(Locale.US, "%,d", amount.toLong()) else String.format(Locale.US, "%,.2f", amount)
    return "$num ₾"
}

/** "2026-11-21T10:00:00+00:00" → "21.11.2026" */
fun formatDate(iso: String?): String {
    if (iso.isNullOrBlank() || iso.length < 10) return ""
    val parts = iso.substring(0, 10).split("-")
    return if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}" else iso.take(10)
}

/** "2026-11-21T09:42:13Z" → "09:42" (UTC as stored; good enough for chat bubbles) */
fun formatTime(iso: String?): String = iso?.substringAfter("T", "")?.take(5) ?: ""

// ============================================================
// Surfaces
// ============================================================

fun Modifier.msShadow(shape: Shape, elevation: Dp = 8.dp, strength: Float = 1f): Modifier = shadow(
    elevation = elevation,
    shape = shape,
    ambientColor = MsColor.Purple600.copy(alpha = 0.10f * strength),
    spotColor = MsColor.Purple600.copy(alpha = 0.20f * strength),
)

/** White rounded card with a faint lavender border and soft purple shadow. */
@Composable
fun MsCard(
    modifier: Modifier = Modifier,
    shape: Shape = MsShape.Card,
    background: Brush = SolidColor(Color.White),
    border: Color = MsColor.CardBorder,
    elevation: Dp = 8.dp,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, spring(stiffness = 600f), label = "cardPress")
    Column(
        modifier
            .scale(scale)
            .msShadow(shape, elevation)
            .clip(shape)
            .background(background)
            .border(1.dp, border, shape)
            .then(
                if (onClick != null) Modifier.clickable(interactionSource = interaction, indication = androidx.compose.material3.ripple(), onClick = onClick)
                else Modifier,
            )
            .padding(contentPadding),
        content = content,
    )
}

// ============================================================
// Buttons
// ============================================================

/** Full-width gradient pill (reference: "კალათაში დამატება", "შეკვეთის მოთხოვნის გაგზავნა"). */
@Composable
fun MsPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = 58.dp,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, spring(stiffness = 700f), label = "btnPress")
    Box(
        modifier
            .scale(scale)
            .height(height)
            .alpha(if (enabled) 1f else 0.55f)
            .msShadow(MsShape.Pill, 12.dp, 1.4f)
            .clip(MsShape.Pill)
            .background(if (pressed) MsBrush.PrimaryButtonPressed else MsBrush.PrimaryButton)
            .clickable(
                interactionSource = interaction,
                indication = androidx.compose.material3.ripple(color = Color.White),
                enabled = enabled && !loading,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.5.dp, modifier = Modifier.size(24.dp))
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon?.let { Icon(it, null, tint = Color.White, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(12.dp)) }
                Text(text, style = msText(16.sp, FontWeight.Bold, Color.White), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                trailingIcon?.let { Spacer(Modifier.width(12.dp)); Icon(it, null, tint = Color.White, modifier = Modifier.size(24.dp)) }
            }
        }
    }
}

/** White pill with purple outline (reference: "გაზიარება", "კალათის გასუფთავება"). */
@Composable
fun MsOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 54.dp,
    color: Color = MsColor.Purple700,
) {
    Box(
        modifier
            .height(height)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(MsShape.Pill)
            .background(Color.White)
            .border(1.8.dp, color, MsShape.Pill)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon?.let { Icon(it, null, tint = color, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(10.dp)) }
            Text(text, style = msText(15.sp, FontWeight.Bold, color), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Gold CTA pill on banners ("იხილე შეთავაზებები →", "დეტალები →"). */
@Composable
fun MsGoldPill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, small: Boolean = false) {
    Row(
        modifier
            .height(if (small) 36.dp else 46.dp)
            .msShadow(MsShape.Pill, 6.dp)
            .clip(MsShape.Pill)
            .background(MsBrush.Gold)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = if (small) 14.dp else 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = msText(if (small) 13.sp else 15.sp, FontWeight.Bold, MsColor.Purple900), maxLines = 1)
        Spacer(Modifier.width(6.dp))
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MsColor.Purple900, modifier = Modifier.size(if (small) 16.dp else 18.dp))
    }
}

/** Small lavender pill with arrow ("ყველას ნახვა →", "შეკვეთის დეტალები →"). */
@Composable
fun MsSoftPill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, withArrow: Boolean = true) {
    Row(
        modifier
            .height(36.dp)
            .clip(MsShape.Pill)
            .background(MsColor.Purple100)
            .border(1.dp, MsColor.Purple200, MsShape.Pill)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = msText(13.sp, FontWeight.Bold, MsColor.Purple700), maxLines = 1)
        if (withArrow) {
            Spacer(Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MsColor.Purple700, modifier = Modifier.size(16.dp))
        }
    }
}

/** Filled or outlined toolbar chip ("ფილტრი", "დაბარისხება ▾"). */
@Composable
fun MsChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    leading: ImageVector? = null,
    trailing: ImageVector? = null,
) {
    Row(
        modifier
            .height(44.dp)
            .msShadow(MsShape.Pill, if (selected) 6.dp else 3.dp)
            .clip(MsShape.Pill)
            .background(if (selected) MsBrush.PrimaryButton else SolidColor(Color.White))
            .border(1.dp, if (selected) Color.Transparent else MsColor.CardBorder, MsShape.Pill)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val c = if (selected) Color.White else MsColor.Ink
        leading?.let { Icon(it, null, tint = if (selected) Color.White else MsColor.Purple700, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)) }
        Text(text, style = msText(13.5.sp, FontWeight.Bold, c), maxLines = 1)
        trailing?.let { Spacer(Modifier.width(6.dp)); Icon(it, null, tint = c, modifier = Modifier.size(20.dp)) }
    }
}

/** Round icon button on a white disc (favourite heart, etc.). */
@Composable
fun MsIconDisc(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MsColor.Purple600,
    size: Dp = 38.dp,
    background: Color = Color.White.copy(alpha = 0.92f),
) {
    Box(
        modifier
            .size(size)
            .msShadow(CircleShape, 4.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(role = Role.Button, onClickLabel = contentDescription, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(size * 0.58f))
    }
}

@Composable
fun FavoriteToggle(isFavorite: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier, size: Dp = 38.dp) {
    val s by animateFloatAsState(if (isFavorite) 1.12f else 1f, spring(dampingRatio = 0.35f, stiffness = 500f), label = "fav")
    MsIconDisc(
        icon = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
        contentDescription = if (isFavorite) "რჩეულებიდან წაშლა" else "რჩეულებში დამატება",
        onClick = onToggle,
        modifier = modifier.scale(s),
        size = size,
    )
}

// ============================================================
// Badges, price, rating, stock
// ============================================================

/** Purple "-20%" badge on product images. */
@Composable
fun DiscountBadge(percent: Int, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MsBrush.PrimaryButton)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text("-$percent%", style = msText(13.sp, FontWeight.Bold, Color.White))
    }
}

/** Pink/red "-20%" pill next to prices. */
@Composable
fun DiscountPill(percent: Int, modifier: Modifier = Modifier, color: Color = MsColor.Pink) {
    Box(
        modifier
            .clip(MsShape.Pill)
            .background(color)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text("-$percent%", style = msText(13.sp, FontWeight.Bold, Color.White))
    }
}

@Composable
fun PriceRow(
    price: Double,
    oldPrice: Double?,
    modifier: Modifier = Modifier,
    priceSize: TextUnit = 19.sp,
    stacked: Boolean = false,
) {
    val showOld = oldPrice != null && oldPrice > price
    val priceText = @Composable {
        Text(formatGel(price), style = msText(priceSize, FontWeight.ExtraBold, MsColor.Purple800), maxLines = 1, softWrap = false)
    }
    val oldText = @Composable {
        if (showOld) Text(
            formatGel(oldPrice!!),
            style = msText((priceSize.value * 0.62f).sp, FontWeight.Medium, MsColor.InkMuted).copy(textDecoration = TextDecoration.LineThrough),
            maxLines = 1,
            softWrap = false,
        )
    }
    if (stacked) {
        Column(modifier) { priceText(); oldText() }
    } else {
        Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { priceText(); oldText() }
    }
}

@Composable
fun RatingRow(rating: Double, reviewCount: Int, modifier: Modifier = Modifier, starSize: Dp = 15.dp, textSize: TextUnit = 12.sp) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        val filled = rating.roundToInt().coerceIn(0, 5)
        repeat(5) { i ->
            Icon(
                Icons.Rounded.Star,
                contentDescription = null,
                tint = if (reviewCount > 0 && i < filled) MsColor.Star else MsColor.Purple200,
                modifier = Modifier.size(starSize),
            )
        }
        Spacer(Modifier.width(5.dp))
        Text(
            if (reviewCount > 0) String.format(Locale.US, "%.1f (%,d შეფასება)", rating, reviewCount) else "შეფასება ჯერ არ არის",
            style = msText(textSize, FontWeight.Medium, MsColor.InkSoft),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun StockLabel(inStock: Boolean, modifier: Modifier = Modifier, size: TextUnit = 13.sp) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Rounded.CheckCircle,
            null,
            tint = if (inStock) MsColor.Green else MsColor.InkMuted,
            modifier = Modifier.size((size.value + 5).dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(if (inStock) "მარაგშია" else "არ არის მარაგში", style = msText(size, FontWeight.Bold, if (inStock) MsColor.Green else MsColor.InkMuted))
    }
}

// ============================================================
// Quantity stepper  ( −  1  + )
// ============================================================

@Composable
fun QuantityStepper(
    quantity: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 50.dp,
    canDecrement: Boolean = true,
    canIncrement: Boolean = true,
) {
    Row(
        modifier
            .height(height)
            .clip(MsShape.Pill)
            .background(MsColor.Purple100)
            .border(1.dp, MsColor.Purple200, MsShape.Pill)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton(Icons.Rounded.Remove, "შემცირება", canDecrement, onDecrement, height - 8.dp)
        Box(
            Modifier
                .height(height - 8.dp)
                .width(height * 0.86f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Text(quantity.toString(), style = msText(17.sp, FontWeight.Bold, MsColor.Ink))
        }
        StepperButton(Icons.Rounded.Add, "გაზრდა", canIncrement, onIncrement, height - 8.dp)
    }
}

@Composable
private fun StepperButton(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit, size: Dp) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, label, tint = if (enabled) MsColor.Purple800 else MsColor.Purple300, modifier = Modifier.size(26.dp))
    }
}

// ============================================================
// Section header & info strip
// ============================================================

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    @androidx.annotation.DrawableRes art: Int? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        art?.let { Art3D(it, Modifier.size(32.dp)); Spacer(Modifier.width(8.dp)) }
        Text(title, style = msText(19.sp, FontWeight.ExtraBold), modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (actionText != null && onAction != null) MsSoftPill(actionText, onAction)
    }
}

/** Lavender strip: "🚚 მიწოდება: 1-3 დღე | 🛡 უსაფრთხო შეკვეთა" */
@Composable
fun InfoStrip(items: List<Pair<ImageVector, String>>, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MsBrush.Lavender)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { i, (icon, text) ->
            if (i > 0) Box(Modifier.padding(horizontal = 8.dp).width(2.dp).height(26.dp).background(MsColor.Purple700))
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(icon, null, tint = MsColor.Purple700, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(text, style = msText(13.sp, FontWeight.SemiBold, MsColor.Ink), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ============================================================
// Input field (lavender pill with leading icon) — checkout, auth, profile
// ============================================================

@Composable
fun MsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    enabled: Boolean = true,
) {
    Column(modifier.animateContentSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .clip(RoundedCornerShape(27.dp))
                .background(MsColor.Purple100.copy(alpha = 0.75f))
                .border(1.3.dp, if (error != null) MsColor.Red else MsColor.Purple200, RoundedCornerShape(27.dp))
                .padding(horizontal = 18.dp, vertical = if (singleLine) 0.dp else 14.dp),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
        ) {
            leadingIcon?.let {
                Icon(it, null, tint = MsColor.Purple700, modifier = Modifier.size(24.dp).padding(top = if (singleLine) 0.dp else 2.dp))
                Spacer(Modifier.width(14.dp))
            }
            Box(Modifier.weight(1f).padding(vertical = if (singleLine) 15.dp else 0.dp), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) Text(placeholder, style = msText(15.sp, FontWeight.Medium, MsColor.InkSoft))
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    singleLine = singleLine,
                    minLines = minLines,
                    textStyle = msText(15.sp, FontWeight.Medium, MsColor.Ink),
                    cursorBrush = SolidColor(MsColor.Purple600),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            trailing?.invoke()
        }
        if (error != null) {
            Text(error, style = msText(12.sp, FontWeight.SemiBold, MsColor.Red), modifier = Modifier.padding(start = 18.dp, top = 4.dp))
        }
    }
}

// ============================================================
// Images
// ============================================================

/** Product / content image with lavender well, loading shimmer and graceful fallback. */
@Composable
fun MsImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallbackIcon: ImageVector = Icons.Rounded.Image,
    shape: Shape = MsShape.Image,
) {
    Box(modifier.clip(shape).background(MsBrush.ImageWell), contentAlignment = Alignment.Center) {
        if (url.isNullOrBlank()) {
            Icon(fallbackIcon, contentDescription, tint = MsColor.Purple300, modifier = Modifier.fillMaxSize(0.42f))
        } else {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
                loading = { ShimmerBox(Modifier.fillMaxSize()) },
                error = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(fallbackIcon, contentDescription, tint = MsColor.Purple300, modifier = Modifier.fillMaxSize(0.42f))
                    }
                },
            )
        }
    }
}

@Composable
fun ShimmerBox(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(0.dp)) {
    val t = rememberInfiniteTransition(label = "shimmer")
    val a by t.animateFloat(0.45f, 1f, infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse), label = "shimmerAlpha")
    Box(modifier.clip(shape).alpha(a).background(MsColor.Purple100))
}

// ============================================================
// Product card (reference screens 1 & 3)
// ============================================================

@Composable
fun ProductCard(
    product: Product,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MsCard(modifier, onClick = onClick, shape = MsShape.Card) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1.12f)
                .padding(8.dp),
        ) {
            MsImage(
                url = product.primaryImage,
                contentDescription = product.nameKa,
                modifier = Modifier.fillMaxSize(),
                fallbackIcon = Icons.Rounded.ShoppingCart,
            )
            if (product.hasDiscount) DiscountBadge(product.discountPercent, Modifier.align(Alignment.TopStart).padding(6.dp))
            FavoriteToggle(isFavorite, onToggleFavorite, Modifier.align(Alignment.TopEnd).padding(6.dp), size = 34.dp)
        }
        Column(Modifier.padding(start = 12.dp, end = 10.dp, bottom = 12.dp)) {
            Text(product.nameKa, style = msText(15.sp, FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                product.shortDescriptionKa ?: product.brand ?: " ",
                style = msText(12.5.sp, FontWeight.Medium, MsColor.InkSoft),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(5.dp))
            RatingRow(product.averageRating, product.reviewCount, starSize = 13.dp, textSize = 10.5.sp)
            Spacer(Modifier.height(8.dp))
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val compact = maxWidth < 168.dp
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    PriceRow(
                        price = product.price,
                        oldPrice = product.compareAtPrice,
                        priceSize = if (formatGel(product.price).length > 8) 16.sp else 18.sp,
                        stacked = true,
                        modifier = Modifier.weight(1f),
                    )
                    AddToCartPill(onAddToCart, compact = compact, enabled = product.isAvailable)
                }
            }
        }
    }
}

@Composable
fun AddToCartPill(onClick: () -> Unit, compact: Boolean = false, enabled: Boolean = true) {
    Row(
        Modifier
            .height(36.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .clip(MsShape.Pill)
            .background(MsBrush.PrimaryButton)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = "კალათაში დამატება", onClick = onClick)
            .padding(horizontal = if (compact) 10.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.ShoppingCart, "კალათაში დამატება", tint = Color.White, modifier = Modifier.size(18.dp))
        if (!compact) {
            Spacer(Modifier.width(5.dp))
            Text("კალათაში", style = msText(11.5.sp, FontWeight.Bold, Color.White), maxLines = 1)
        }
        Spacer(Modifier.width(3.dp))
        Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(15.dp))
    }
}

@Composable
fun ProductCardSkeleton(modifier: Modifier = Modifier) {
    MsCard(modifier) {
        ShimmerBox(Modifier.fillMaxWidth().aspectRatio(1.12f).padding(8.dp), MsShape.Image)
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ShimmerBox(Modifier.fillMaxWidth(0.8f).height(14.dp), MsShape.Pill)
            ShimmerBox(Modifier.fillMaxWidth(0.5f).height(10.dp), MsShape.Pill)
            ShimmerBox(Modifier.fillMaxWidth(0.6f).height(18.dp), MsShape.Pill)
        }
    }
}

// ============================================================
// Empty / error / loading states
// ============================================================

@Composable
fun EmptyState(
    @androidx.annotation.DrawableRes art: Int,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(132.dp).clip(CircleShape).background(MsBrush.Lavender).border(1.dp, MsColor.Purple200, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Art3D(art, Modifier.size(92.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text(title, style = msText(18.sp, FontWeight.ExtraBold), textAlign = TextAlign.Center)
        subtitle?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, style = msText(14.sp, FontWeight.Medium, MsColor.InkSoft), textAlign = TextAlign.Center)
        }
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            MsPrimaryButton(actionText, onAction, height = 52.dp)
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    EmptyState(
        art = ge.myshop.app.R.drawable.td_warning,
        title = "ვერ ჩაიტვირთა",
        subtitle = message,
        actionText = "ხელახლა ცდა",
        onAction = onRetry,
        modifier = modifier,
    )
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MsColor.Purple600, strokeWidth = 3.dp)
    }
}

/** 3D art in a soft lavender rounded square (shortcuts, stats, list rows). */
@Composable
fun ArtTile(@androidx.annotation.DrawableRes art: Int, modifier: Modifier = Modifier, size: Dp = 48.dp) {
    Box(modifier.size(size).clip(RoundedCornerShape(size * 0.3f)).background(MsBrush.Lavender), contentAlignment = Alignment.Center) {
        Art3D(art, Modifier.size(size * 0.74f))
    }
}

/** Row helper: icon in a soft lavender rounded square. */
@Composable
fun IconTile(icon: ImageVector, modifier: Modifier = Modifier, size: Dp = 46.dp, tint: Color = MsColor.Purple600, background: Brush = MsBrush.Lavender) {
    Box(modifier.size(size).clip(RoundedCornerShape(14.dp)).background(background), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(size * 0.56f))
    }
}


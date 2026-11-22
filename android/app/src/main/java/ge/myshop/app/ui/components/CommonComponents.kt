package ge.myshop.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import ge.myshop.app.ui.theme.AccentGold
import ge.myshop.app.ui.theme.BadgeShape
import ge.myshop.app.ui.theme.PillShape
import ge.myshop.app.ui.theme.PrimaryPurple
import ge.myshop.app.ui.theme.PrimaryPurpleDark
import ge.myshop.app.ui.theme.myShopColors
import ge.myshop.app.ui.theme.spacing

// ============================================================
// MYSHOP Shared UI Components
// ============================================================

// ── Primary Button ────────────────────────────────────────────

@Composable
fun MyShopButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = PillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryPurple,
            disabledContainerColor = PrimaryPurple.copy(alpha = 0.4f),
        ),
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.spacing.buttonPaddingH,
            vertical = MaterialTheme.spacing.buttonPaddingV,
        ),
        modifier = modifier,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            leadingIcon?.let {
                Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        }
    }
}

// ── Secondary / Outlined Button ───────────────────────────────

@Composable
fun MyShopOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = PillShape,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryPurple),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = PrimaryPurple,
        ),
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.spacing.buttonPaddingH,
            vertical = MaterialTheme.spacing.buttonPaddingV,
        ),
        modifier = modifier,
    ) {
        leadingIcon?.let {
            Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

// ── Text Field ────────────────────────────────────────────────

@Composable
fun MyShopTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurface.copy(0.4f)) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            enabled = enabled,
            singleLine = singleLine,
            maxLines = maxLines,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            shape = MaterialTheme.shapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                focusedLabelColor = PrimaryPurple,
                cursorColor = PrimaryPurple,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            )
        }
    }
}

// ── Price Display ─────────────────────────────────────────────

@Composable
fun PriceDisplay(
    price: Double,
    compareAtPrice: Double? = null,
    modifier: Modifier = Modifier,
    priceFontSize: Int = 18,
    showCurrency: Boolean = true,
) {
    val currency = if (showCurrency) " ₾" else ""
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "%.2f$currency".format(price),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = priceFontSize.sp,
                color = MaterialTheme.myShopColors.price,
            ),
        )
        if (compareAtPrice != null && compareAtPrice > price) {
            Text(
                text = "%.2f$currency".format(compareAtPrice),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.myShopColors.priceOld,
                    textDecoration = TextDecoration.LineThrough,
                ),
            )
        }
    }
}

// ── Discount Badge ────────────────────────────────────────────

@Composable
fun DiscountBadge(
    discountPercent: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(BadgeShape)
            .background(MaterialTheme.myShopColors.discount)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "-$discountPercent%",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
            ),
        )
    }
}

// ── Rating Bar ────────────────────────────────────────────────

@Composable
fun RatingBar(
    rating: Double,
    reviewCount: Int = 0,
    modifier: Modifier = Modifier,
    showCount: Boolean = true,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = AccentGold,
                modifier = Modifier.size(14.dp),
            )
        }
        if (showCount && reviewCount > 0) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = "($reviewCount)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
            )
        }
    }
}

// ── Skeleton Loader ───────────────────────────────────────────

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_x",
    )
    val shimmerColors = listOf(
        MaterialTheme.myShopColors.shimmer,
        MaterialTheme.myShopColors.shimmerHighlight,
        MaterialTheme.myShopColors.shimmer,
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 500f, 0f),
        end = Offset(translateAnim, 0f),
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush),
    )
}

// ── Empty State ───────────────────────────────────────────────

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String = "",
    action: Pair<String, () -> Unit>? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryPurple.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
        )
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
            )
        }
        action?.let { (label, onClick) ->
            MyShopButton(text = label, onClick = onClick)
        }
    }
}

/** Emoji variant used by most screens: `EmptyState(icon = "🛒", title = ...)`. */
@Composable
fun EmptyState(
    icon: String,
    title: String,
    subtitle: String = "",
    action: Pair<String, () -> Unit>? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = icon, fontSize = 64.sp)
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
            textAlign = TextAlign.Center,
        )
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                textAlign = TextAlign.Center,
            )
        }
        action?.let { (label, onClick) ->
            MyShopButton(text = label, onClick = onClick)
        }
    }
}

// ── Error State ────────────────────────────────────────────────

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displaySmall,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
        )
        MyShopButton(text = "ხელახლა ცდა", onClick = onRetry)
    }
}

// ── Section Header ─────────────────────────────────────────────

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        if (actionLabel != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryPurple,
                )
            }
        }
    }
}

package ge.myshop.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ge.myshop.app.domain.model.Product
import ge.myshop.app.ui.theme.ColorDiscount
import ge.myshop.app.ui.theme.ProductImageShape
import ge.myshop.app.ui.theme.myShopColors
import ge.myshop.app.ui.theme.spacing

// ============================================================
// Product Card — grid display version
// ============================================================

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavoriteToggle: ((String) -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .width(MaterialTheme.spacing.productCardWidth)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            // Image
            Box {
                AsyncImage(
                    model = product.primaryImage,
                    contentDescription = product.nameKa,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(ProductImageShape),
                )

                // Discount badge
                if (product.hasDiscount) {
                    DiscountBadge(
                        discountPercent = product.discountPercent,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                    )
                }

                // Favorite button
                if (onFavoriteToggle != null) {
                    IconButton(
                        onClick = { onFavoriteToggle(product.id) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(32.dp),
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite
                                         else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) ColorDiscount else Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                // Out of stock overlay
                if (!product.isAvailable) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(0.4f))
                            .clip(ProductImageShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "არ არის",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            // Info
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.nameKa,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(Modifier.height(6.dp))

                if (product.averageRating > 0) {
                    RatingBar(rating = product.averageRating, reviewCount = product.reviewCount)
                    Spacer(Modifier.height(4.dp))
                }

                PriceDisplay(
                    price = product.price,
                    compareAtPrice = product.compareAtPrice,
                    priceFontSize = 15,
                )
            }
        }
    }
}

// ── Skeleton Product Card ──────────────────────────────────────

@Composable
fun ProductCardSkeleton(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.width(160.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = ProductImageShape,
            )
            Column(modifier = Modifier.padding(10.dp)) {
                ShimmerBox(modifier = Modifier.fillMaxWidth().height(14.dp))
                Spacer(Modifier.height(4.dp))
                ShimmerBox(modifier = Modifier.width(80.dp).height(14.dp))
                Spacer(Modifier.height(6.dp))
                ShimmerBox(modifier = Modifier.width(60.dp).height(18.dp))
            }
        }
    }
}

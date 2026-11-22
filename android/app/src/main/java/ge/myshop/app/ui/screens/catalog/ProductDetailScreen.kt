package ge.myshop.app.ui.screens.catalog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import ge.myshop.app.domain.model.ProductVariant
import ge.myshop.app.ui.components.DiscountBadge
import ge.myshop.app.ui.components.ErrorState
import ge.myshop.app.ui.components.MyShopButton
import ge.myshop.app.ui.components.PriceDisplay
import ge.myshop.app.ui.components.RatingBar
import ge.myshop.app.ui.components.ScreenHeader
import ge.myshop.app.ui.components.SectionHeader
import ge.myshop.app.ui.components.ShimmerBox
import ge.myshop.app.ui.theme.ColorDiscount
import ge.myshop.app.ui.theme.PrimaryPurple
import ge.myshop.app.ui.theme.spacing

@Composable
fun ProductDetailScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(productId) { viewModel.loadProduct(productId) }

    LaunchedEffect(uiState.event) {
        uiState.event?.let { event ->
            when (event) {
                is ProductDetailEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is ProductDetailEvent.NavigateToLogin -> onNavigateToLogin()
                is ProductDetailEvent.NavigateToCart -> onNavigateToCart()
            }
            viewModel.clearEvent()
        }
    }

    Scaffold(
        topBar = {
            ScreenHeader(
                title = uiState.product?.nameKa?.take(20) ?: "",
                onBack = onNavigateBack,
                actions = {
                    Row {
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (uiState.isFavorite) Icons.Filled.Favorite
                                             else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (uiState.isFavorite) ColorDiscount else Color.White,
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            uiState.product?.let { product ->
                Surface(shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Quantity selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilledIconButton(
                                onClick = { viewModel.decrementQty() },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = PrimaryPurple.copy(0.1f),
                                    contentColor = PrimaryPurple,
                                ),
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = "−", Modifier.size(16.dp))
                            }
                            Text(
                                text = uiState.quantity.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            FilledIconButton(
                                onClick = { viewModel.incrementQty() },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = PrimaryPurple,
                                    contentColor = Color.White,
                                ),
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "+", Modifier.size(16.dp))
                            }
                        }

                        MyShopButton(
                            text = "კალათაში დამატება",
                            onClick = { viewModel.addToCart() },
                            enabled = product.isAvailable,
                            isLoading = uiState.isAddingToCart,
                            leadingIcon = Icons.Filled.ShoppingCart,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
    ) { padding ->
        if (uiState.isLoading) {
            ProductDetailSkeleton(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        if (uiState.error != null) {
            ErrorState(
                message = uiState.error!!,
                onRetry = { viewModel.loadProduct(productId) },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            return@Scaffold
        }

        val product = uiState.product ?: return@Scaffold

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            // Image gallery
            item {
                ImageGallery(
                    images = product.allImages,
                    hasDiscount = product.hasDiscount,
                    discountPercent = product.discountPercent,
                )
            }

            // Product info
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = product.nameKa,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))

                    if (product.averageRating > 0) {
                        RatingBar(rating = product.averageRating, reviewCount = product.reviewCount)
                        Spacer(Modifier.height(8.dp))
                    }

                    PriceDisplay(
                        price = product.price + (uiState.selectedVariant?.priceAdjustment ?: 0.0),
                        compareAtPrice = product.compareAtPrice?.let { it + (uiState.selectedVariant?.priceAdjustment ?: 0.0) },
                        priceFontSize = 24,
                    )

                    if (!product.isAvailable) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "მარაგი ამოიწურა",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // Variants
            if (product.variants.isNotEmpty()) {
                item {
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "ვარიანტები",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(product.variants) { variant ->
                            VariantChip(
                                variant = variant,
                                isSelected = uiState.selectedVariant?.id == variant.id,
                                onClick = { viewModel.selectVariant(variant) },
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            // Description
            if (!product.descriptionKa.isNullOrBlank()) {
                item {
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "აღწერა",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = product.descriptionKa,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.8f),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }

            // Attributes
            if (product.attributes.isNotEmpty()) {
                item {
                    Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                    Text(
                        text = "მახასიათებლები",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    product.attributes.forEach { attr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = attr.attributeNameKa,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                            )
                            Text(
                                text = attr.attributeValueKa,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageGallery(
    images: List<String>,
    hasDiscount: Boolean,
    discountPercent: Int,
) {
    val pagerState = rememberPagerState(pageCount = { images.ifEmpty { listOf("") }.size })

    Box {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        ) { page ->
            AsyncImage(
                model = images.getOrNull(page),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)),
            )
        }

        if (hasDiscount) {
            DiscountBadge(
                discountPercent = discountPercent,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
            )
        }

        // Dots
        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                images.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .size(if (pagerState.currentPage == index) 20.dp else 6.dp, 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) PrimaryPurple
                                else Color.White.copy(0.7f)
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun VariantChip(
    variant: ProductVariant,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(if (isSelected) PrimaryPurple else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (isSelected) PrimaryPurple else MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.small,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = variant.variantNameKa,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun ProductDetailSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().aspectRatio(1f))
        Column(modifier = Modifier.padding(16.dp)) {
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.8f).height(24.dp))
            Spacer(Modifier.height(12.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f).height(32.dp))
            Spacer(Modifier.height(16.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(80.dp))
        }
    }
}

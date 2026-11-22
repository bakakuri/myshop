package ge.myshop.app.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import ge.myshop.app.domain.model.Banner
import ge.myshop.app.domain.model.Category
import ge.myshop.app.domain.model.Product
import ge.myshop.app.ui.components.AppHeader
import ge.myshop.app.ui.components.ErrorState
import ge.myshop.app.ui.components.HomeAppHeader
import ge.myshop.app.ui.components.ProductCard
import ge.myshop.app.ui.components.ProductCardSkeleton
import ge.myshop.app.ui.components.SectionHeader
import ge.myshop.app.ui.components.ShimmerBox
import ge.myshop.app.ui.theme.BannerShape
import ge.myshop.app.ui.theme.CategoryCardShape
import ge.myshop.app.ui.theme.PrimaryPurple
import ge.myshop.app.ui.theme.PrimaryPurpleMedium
import ge.myshop.app.ui.theme.spacing
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onNavigateToCategories: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToProductList: (categoryId: String?, title: String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToCart: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        HomeAppHeader(
            cartCount = uiState.cartCount,
            notificationCount = uiState.notificationCount,
            onSearchClick = onNavigateToSearch,
            onCartClick = onNavigateToCart,
            onNotificationClick = onNavigateToNotifications,
        )

        if (uiState.error != null) {
            ErrorState(
                message = uiState.error!!,
                onRetry = viewModel::loadAll,
                modifier = Modifier.fillMaxSize().weight(1f),
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            // Hero Banners
            item {
                if (uiState.isLoading) {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        shape = BannerShape,
                    )
                } else if (uiState.banners.isNotEmpty()) {
                    HeroBannerPager(
                        banners = uiState.banners,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
            }

            // Category Shortcuts
            if (uiState.topCategories.isNotEmpty() || uiState.isLoading) {
                item {
                    Spacer(Modifier.height(8.dp))
                    SectionHeader(
                        title = "კატეგორიები",
                        actionLabel = "ყველა",
                        onActionClick = onNavigateToCategories,
                    )
                    Spacer(Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (uiState.isLoading) {
                            items(6) {
                                CategoryShortcutSkeleton()
                            }
                        } else {
                            items(uiState.topCategories) { category ->
                                CategoryShortcut(
                                    category = category,
                                    onClick = {
                                        onNavigateToProductList(category.id, category.nameKa)
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // Popular Products
            item {
                Spacer(Modifier.height(24.dp))
                SectionHeader(
                    title = "პოპულარული",
                    actionLabel = "ყველა",
                    onActionClick = { onNavigateToProductList(null, "პოპულარული") },
                )
                Spacer(Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (uiState.isLoading) {
                        items(4) { ProductCardSkeleton() }
                    } else {
                        items(uiState.popularProducts) { product ->
                            ProductCard(
                                product = product,
                                onClick = { onNavigateToProductDetail(product.id) },
                            )
                        }
                    }
                }
            }

            // Discounted Products
            if (uiState.discountedProducts.isNotEmpty() || uiState.isLoading) {
                item {
                    Spacer(Modifier.height(24.dp))
                    SectionHeader(
                        title = "ფასდაკლება",
                        actionLabel = "ყველა",
                        onActionClick = { onNavigateToProductList(null, "ფასდაკლება") },
                    )
                    Spacer(Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (uiState.isLoading) {
                            items(4) { ProductCardSkeleton() }
                        } else {
                            items(uiState.discountedProducts) { product ->
                                ProductCard(
                                    product = product,
                                    onClick = { onNavigateToProductDetail(product.id) },
                                )
                            }
                        }
                    }
                }
            }

            // New Arrivals
            item {
                Spacer(Modifier.height(24.dp))
                SectionHeader(
                    title = "სიახლეები",
                    actionLabel = "ყველა",
                    onActionClick = { onNavigateToProductList(null, "სიახლეები") },
                )
                Spacer(Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (uiState.isLoading) {
                        items(4) { ProductCardSkeleton() }
                    } else {
                        items(uiState.newProducts) { product ->
                            ProductCard(
                                product = product,
                                onClick = { onNavigateToProductDetail(product.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Hero Banner Pager ──────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HeroBannerPager(
    banners: List<Banner>,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { banners.size })

    // Auto-scroll
    LaunchedEffect(banners.size) {
        while (true) {
            delay(4000)
            val next = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(next)
        }
    }

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            BannerItem(banner = banners[page])
        }

        // Dots indicator
        if (banners.size > 1) {
            Spacer(Modifier.height(8.dp))
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                banners.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (pagerState.currentPage == index) 20.dp else 6.dp, 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) PrimaryPurple
                                else PrimaryPurple.copy(0.3f)
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun BannerItem(banner: Banner) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(BannerShape),
    ) {
        AsyncImage(
            model = banner.imageUrl,
            contentDescription = banner.titleKa,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // Gradient overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(0.5f)),
                        startY = 0.4f,
                    )
                ),
        )
        // Text overlay
        if (banner.titleKa != null || banner.subtitleKa != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
            ) {
                banner.titleKa?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                    )
                }
                banner.subtitleKa?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// ── Category Shortcut ──────────────────────────────────────────

@Composable
private fun CategoryShortcut(
    category: Category,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(70.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CategoryCardShape)
                .background(PrimaryPurple.copy(0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            if (category.iconUrl != null) {
                AsyncImage(
                    model = category.iconUrl,
                    contentDescription = category.nameKa,
                    modifier = Modifier.size(36.dp),
                )
            } else {
                Text(
                    text = category.nameKa.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = PrimaryPurple,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = category.nameKa,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CategoryShortcutSkeleton() {
    Column(
        modifier = Modifier.width(70.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ShimmerBox(
            modifier = Modifier.size(60.dp),
            shape = CategoryCardShape,
        )
        Spacer(Modifier.height(6.dp))
        ShimmerBox(modifier = Modifier.width(50.dp).height(12.dp))
    }
}

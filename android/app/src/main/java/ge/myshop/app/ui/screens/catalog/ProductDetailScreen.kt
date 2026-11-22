package ge.myshop.app.ui.screens.catalog

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.domain.model.Product
import ge.myshop.app.domain.model.ProductAttribute
import ge.myshop.app.domain.model.ProductReview
import ge.myshop.app.ui.common.HandleShopEvents
import ge.myshop.app.ui.common.LocalApp
import ge.myshop.app.ui.components.DiscountBadge
import ge.myshop.app.ui.components.DiscountPill
import ge.myshop.app.ui.components.ErrorState
import ge.myshop.app.ui.components.FavoriteToggle
import ge.myshop.app.ui.components.HeaderAction
import ge.myshop.app.ui.components.LoadingState
import ge.myshop.app.ui.components.MsCard
import ge.myshop.app.ui.components.MsImage
import ge.myshop.app.ui.components.MsOutlinedButton
import ge.myshop.app.ui.components.MsPrimaryButton
import ge.myshop.app.ui.components.MyShopHeader
import ge.myshop.app.ui.components.ProductCard
import ge.myshop.app.ui.components.QuantityStepper
import ge.myshop.app.ui.components.RatingRow
import ge.myshop.app.ui.components.SectionHeader
import ge.myshop.app.ui.components.StockLabel
import ge.myshop.app.ui.components.formatDate
import ge.myshop.app.ui.components.formatGel
import ge.myshop.app.ui.components.msShadow
import ge.myshop.app.ui.components.msText
import ge.myshop.app.ui.theme.MsBrush
import ge.myshop.app.ui.theme.MsColor
import ge.myshop.app.ui.theme.MsShape
import kotlinx.coroutines.launch

@Composable
fun ProductDetailScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToLogin: () -> Unit = {},
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(productId) { viewModel.loadProduct(productId) }
    val state by viewModel.uiState.collectAsState()
    val favorites by viewModel.favoriteIds.collectAsState()
    val app = LocalApp.current
    HandleShopEvents(viewModel.events, app)
    val context = LocalContext.current

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        MyShopHeader(
            leading = HeaderAction(Icons.AutoMirrored.Rounded.ArrowBack, "უკან", onClick = onNavigateBack),
            actions = listOf(HeaderAction(Icons.Rounded.ShoppingCart, "კალათა", badge = app.badges.cart, onClick = onNavigateToCart)),
        )
        val product = state.product
        when {
            state.isLoading && product == null -> LoadingState()
            product == null -> ErrorState(state.error ?: "პროდუქტი ვერ მოიძებნა", onRetry = { viewModel.loadProduct(productId, force = true) })
            else -> {
                // ── Top: gallery (left) + key info (right) — reference 4
                Row(Modifier.padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Gallery(product, isFavorite = product.id in favorites, onToggleFavorite = { viewModel.toggleFavorite(product.id) })
                    }
                    Column(Modifier.weight(1f)) {
                        Text(product.nameKa, style = msText(23.sp, FontWeight.ExtraBold, lineHeight = 27.sp), maxLines = 3, overflow = TextOverflow.Ellipsis)
                        (product.shortDescriptionKa ?: product.brand)?.let {
                            Spacer(Modifier.height(2.dp))
                            Text(it, style = msText(13.sp, FontWeight.Medium, MsColor.InkSoft), maxLines = 2)
                        }
                        Spacer(Modifier.height(6.dp))
                        RatingRow(product.averageRating, product.reviewCount, starSize = 14.dp, textSize = 11.sp)
                        Spacer(Modifier.height(10.dp))
                        Text(formatGel(state.unitPrice), style = msText(28.sp, FontWeight.ExtraBold, MsColor.Purple800))
                        state.oldPrice?.takeIf { it > state.unitPrice }?.let { old ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    formatGel(old),
                                    style = msText(14.sp, FontWeight.Medium, MsColor.InkMuted).copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                                )
                                Spacer(Modifier.width(8.dp))
                                DiscountPill(product.discountPercent, color = MsColor.Red)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        product.attributes.sortedBy { it.sortOrder }.take(4).forEach { SpecChip(it) }
                        Spacer(Modifier.height(4.dp))
                        StockLabel(state.inStock)
                    }
                }

                // ── Variants
                if (product.variants.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    VariantPicker(product, state.selectedVariant?.id, onSelect = viewModel::selectVariant)
                }

                // ── Delivery + actions
                Spacer(Modifier.height(14.dp))
                Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocalShipping, null, tint = MsColor.Purple600, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("მიწოდება ${app.store.deliveryEstimate}", style = msText(14.sp, FontWeight.SemiBold, MsColor.InkSoft))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuantityStepper(
                            quantity = state.quantity,
                            onDecrement = viewModel::decrementQty,
                            onIncrement = viewModel::incrementQty,
                            canDecrement = state.quantity > 1,
                            canIncrement = state.quantity < state.maxQuantity,
                            height = 56.dp,
                        )
                        MsPrimaryButton(
                            text = "კალათაში დამატება",
                            onClick = viewModel::addSelectedToCart,
                            icon = Icons.Rounded.ShoppingCart,
                            enabled = state.inStock,
                            modifier = Modifier.weight(1f),
                            height = 56.dp,
                        )
                    }
                    MsOutlinedButton(
                        text = "გაზიარება",
                        icon = Icons.Rounded.Share,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val text = "${product.nameKa} — ${formatGel(state.unitPrice)}\nMyShop"
                            context.startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text),
                                    "გაზიარება",
                                ),
                            )
                        },
                    )
                    TrustRow()
                }

                // ── Tabs: description / specs / reviews
                Spacer(Modifier.height(18.dp))
                DetailTabs(product, state.reviews)

                // ── Related
                if (state.related.isNotEmpty()) {
                    Spacer(Modifier.height(18.dp))
                    SectionHeader("მსგავსი პროდუქტები", Modifier.padding(horizontal = 14.dp))
                    Spacer(Modifier.height(10.dp))
                    LazyRow(contentPadding = PaddingValues(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(state.related, key = { _, p -> p.id }) { _, p ->
                            ProductCard(
                                product = p,
                                isFavorite = p.id in favorites,
                                onClick = { onNavigateToProductDetail(p.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(p.id) },
                                onAddToCart = { viewModel.addToCart(p.id) },
                                modifier = Modifier.width(186.dp).padding(vertical = 6.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.navigationBarsPadding().height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Gallery(product: Product, isFavorite: Boolean, onToggleFavorite: () -> Unit) {
    val images = product.allImages
    val pager = rememberPagerState { images.size.coerceAtLeast(1) }
    val scope = rememberCoroutineScope()
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .msShadow(MsShape.Card, 8.dp)
            .clip(MsShape.Card)
            .background(MsBrush.ImageWell)
            .border(1.dp, MsColor.CardBorder, MsShape.Card),
    ) {
        if (images.isEmpty()) {
            MsImage(null, product.nameKa, Modifier.fillMaxSize().padding(10.dp), fallbackIcon = Icons.Rounded.ShoppingCart)
        } else {
            HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
                MsImage(images[page], product.nameKa, Modifier.fillMaxSize().padding(10.dp))
            }
        }
        if (product.hasDiscount) DiscountBadge(product.discountPercent, Modifier.align(Alignment.TopStart).padding(14.dp))
        FavoriteToggle(isFavorite, onToggleFavorite, Modifier.align(Alignment.TopEnd).padding(12.dp), size = 36.dp)
        if (images.size > 1) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp)
                    .clip(MsShape.Pill)
                    .background(Color.White.copy(alpha = 0.92f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text("${pager.currentPage + 1}/${images.size}", style = msText(12.sp, FontWeight.Bold, MsColor.InkSoft))
            }
        }
    }
    if (images.size > 1) {
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            images.take(4).forEachIndexed { i, url ->
                val selected = pager.currentPage == i
                Box(
                    Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(if (selected) 2.dp else 1.dp, if (selected) MsColor.Purple600 else MsColor.CardBorder, RoundedCornerShape(12.dp))
                        .clickable(role = Role.Button, onClickLabel = "სურათი ${i + 1}") { scope.launch { pager.animateScrollToPage(i) } },
                ) {
                    MsImage(url, null, Modifier.fillMaxSize().padding(3.dp), shape = RoundedCornerShape(9.dp))
                }
            }
        }
    }
}

private fun specIcon(name: String): ImageVector {
    val n = name.lowercase()
    return when {
        "ფერ" in n -> Icons.Rounded.Palette
        "მეხსიერ" in n || "ram" in n || "პროცესორ" in n -> Icons.Rounded.Memory
        "ეკრან" in n || "დისპლე" in n -> Icons.Rounded.Smartphone
        "კამერ" in n -> Icons.Rounded.PhotoCamera
        "ბატარე" in n || "აკუმულ" in n -> Icons.Rounded.BatteryFull
        "ზომ" in n || "წონ" in n -> Icons.Rounded.Straighten
        else -> Icons.Rounded.Tune
    }
}

@Composable
private fun SpecChip(attr: ProductAttribute) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MsColor.Purple100.copy(alpha = 0.8f))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(specIcon(attr.attributeNameKa), null, tint = MsColor.Purple600, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(attr.attributeValueKa, style = msText(12.5.sp, FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(attr.attributeNameKa, style = msText(10.sp, FontWeight.Medium, MsColor.InkSoft), maxLines = 1)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VariantPicker(product: Product, selectedId: String?, onSelect: (ge.myshop.app.domain.model.ProductVariant) -> Unit) {
    Column(Modifier.padding(horizontal = 14.dp)) {
        Text("ვარიანტი", style = msText(15.sp, FontWeight.Bold))
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            product.variants.sortedBy { it.sortOrder }.forEach { v ->
                val selected = v.id == selectedId
                val available = v.isAvailable && v.stockQuantity > 0
                Box(
                    Modifier
                        .clip(MsShape.Pill)
                        .background(if (selected) MsBrush.PrimaryButton else androidx.compose.ui.graphics.SolidColor(Color.White))
                        .border(1.3.dp, if (selected) Color.Transparent else MsColor.Purple200, MsShape.Pill)
                        .clickable(enabled = available, role = Role.RadioButton) { onSelect(v) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    val label = if (v.priceAdjustment != 0.0) "${v.variantNameKa} (${if (v.priceAdjustment > 0) "+" else ""}${formatGel(v.priceAdjustment)})" else v.variantNameKa
                    Text(
                        label,
                        style = msText(13.5.sp, FontWeight.Bold, if (selected) Color.White else if (available) MsColor.Ink else MsColor.InkMuted),
                    )
                }
            }
        }
    }
}

/** Three small trust cards (reference 4, under the gallery). */
@Composable
private fun TrustRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            Icons.Rounded.VerifiedUser to "უსაფრთხო\nშეკვეთა",
            Icons.Rounded.Autorenew to "მარტივი\nდაბრუნება",
            Icons.Rounded.HeadsetMic to "მხარდაჭერა\nჩათში",
        ).forEach { (icon, text) ->
            MsCard(Modifier.weight(1f), shape = MsShape.CardSmall, elevation = 4.dp) {
                Column(Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(icon, null, tint = MsColor.Purple600, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.height(6.dp))
                    Text(text, style = msText(11.sp, FontWeight.Bold, lineHeight = 14.sp), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun DetailTabs(product: Product, reviews: List<ProductReview>) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("აღწერა", "მახასიათებლები", "მიმოხილვები (${reviews.size})")
    Column(Modifier.padding(horizontal = 14.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(MsShape.Pill)
                .background(Color.White)
                .border(1.dp, MsColor.CardBorder, MsShape.Pill)
                .padding(4.dp),
        ) {
            tabs.forEachIndexed { i, t ->
                val selected = i == tab
                Box(
                    Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(MsShape.Pill)
                        .background(if (selected) MsBrush.PrimaryButton else androidx.compose.ui.graphics.SolidColor(Color.Transparent))
                        .clickable(role = Role.Tab) { tab = i },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(t, style = msText(12.sp, FontWeight.Bold, if (selected) Color.White else MsColor.Ink), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        AnimatedContent(targetState = tab, label = "detailTab") { current ->
            MsCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                when (current) {
                    0 -> Text(
                        product.descriptionKa?.takeIf { it.isNotBlank() } ?: "აღწერა ჯერ არ დამატებულა.",
                        style = msText(14.5.sp, FontWeight.Medium, MsColor.InkSoft, lineHeight = 22.sp),
                    )
                    1 -> if (product.attributes.isEmpty()) {
                        Text("მახასიათებლები ჯერ არ დამატებულა.", style = msText(14.sp, FontWeight.Medium, MsColor.InkSoft))
                    } else {
                        product.attributes.sortedBy { it.sortOrder }.forEachIndexed { i, a ->
                            if (i > 0) Box(Modifier.fillMaxWidth().padding(vertical = 8.dp).height(1.dp).background(MsColor.Divider))
                            Row {
                                Text(a.attributeNameKa, style = msText(14.sp, FontWeight.Medium, MsColor.InkSoft), modifier = Modifier.weight(1f))
                                Text(a.attributeValueKa, style = msText(14.sp, FontWeight.Bold), textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    else -> if (reviews.isEmpty()) {
                        Text("ჯერ არავის დაუწერია მიმოხილვა.", style = msText(14.sp, FontWeight.Medium, MsColor.InkSoft))
                    } else {
                        reviews.forEachIndexed { i, r ->
                            if (i > 0) Box(Modifier.fillMaxWidth().padding(vertical = 12.dp).height(1.dp).background(MsColor.Divider))
                            ReviewItem(r)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewItem(r: ProductReview) {
    val name = r.reviewerName?.takeIf { it.isNotBlank() } ?: "მომხმარებელი"
    Row(verticalAlignment = Alignment.Top) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(MsBrush.PrimaryButton), contentAlignment = Alignment.Center) {
            Text(name.first().uppercase(), style = msText(16.sp, FontWeight.Bold, Color.White))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, style = msText(14.sp, FontWeight.Bold), modifier = Modifier.weight(1f))
                Text(formatDate(r.createdAt), style = msText(11.sp, FontWeight.Medium, MsColor.InkMuted))
            }
            Row {
                repeat(5) { i -> Icon(Icons.Rounded.Star, null, tint = if (i < r.rating) MsColor.Star else MsColor.Purple200, modifier = Modifier.size(14.dp)) }
            }
            r.comment?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = msText(13.5.sp, FontWeight.Medium, MsColor.InkSoft, lineHeight = 20.sp))
            }
        }
    }
}

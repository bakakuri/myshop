package ge.myshop.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Kitchen
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Textsms
import androidx.compose.material.icons.rounded.Toys
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ge.myshop.app.R
import ge.myshop.app.domain.model.Banner
import ge.myshop.app.domain.model.Category
import ge.myshop.app.ui.theme.MsBrush
import ge.myshop.app.ui.theme.MsColor
import ge.myshop.app.ui.theme.MsShape

// ============================================================
// Category visuals
// ============================================================

fun categoryIcon(slug: String): ImageVector = when (slug) {
    "electronics" -> Icons.Rounded.Devices
    "clothing" -> Icons.Rounded.Checkroom
    "home-appliances" -> Icons.Rounded.Kitchen
    "beauty" -> Icons.Rounded.Spa
    "sports" -> Icons.Rounded.SportsSoccer
    "toys" -> Icons.Rounded.Toys
    "accessories" -> Icons.Rounded.Watch
    "home-kitchen" -> Icons.Rounded.Restaurant
    else -> Icons.Rounded.Category
}

fun productCountLabel(n: Long): String = when {
    n <= 0 -> "მალე დაემატება"
    n < 10 -> "$n პროდუქტი"
    else -> "${(n / 10) * 10}+ პროდუქტი"
}

/** Pastel category card (reference 2). */
@Composable
fun CategoryTile(category: Category, index: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val pastel = MsColor.Pastels[index % MsColor.Pastels.size]
    MsCard(
        modifier = modifier.height(168.dp),
        background = Brush.linearGradient(listOf(pastel, Color.White.copy(alpha = 0.85f).compositeOver(pastel))),
        border = Color.White,
        onClick = onClick,
    ) {
        Box(Modifier.fillMaxSize()) {
            if (!category.imageUrl.isNullOrBlank()) {
                MsImage(
                    url = category.imageUrl,
                    contentDescription = category.nameKa,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().height(104.dp).padding(8.dp),
                    shape = RoundedCornerShape(14.dp),
                )
            } else {
                Icon(
                    categoryIcon(category.slug),
                    contentDescription = null,
                    tint = MsColor.Purple600.copy(alpha = 0.9f),
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 14.dp, end = 14.dp).size(70.dp),
                )
                Icon(
                    categoryIcon(category.slug),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.align(Alignment.TopStart).offset(x = (-14).dp, y = (-10).dp).size(92.dp).rotate(-14f),
                )
            }
            Column(Modifier.align(Alignment.BottomStart).padding(start = 14.dp, bottom = 14.dp, end = 56.dp)) {
                Text(category.nameKa, style = msText(15.sp, FontWeight.ExtraBold), maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(productCountLabel(category.productCount), style = msText(12.sp, FontWeight.Medium, MsColor.InkSoft))
            }
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MsBrush.PrimaryButton),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}

/** White square shortcut with image/icon and label (reference 1, row under the hero). */
@Composable
fun CategoryShortcut(category: Category, onClick: () -> Unit, modifier: Modifier = Modifier) {
    MsCard(modifier.width(88.dp), shape = MsShape.CardSmall, elevation = 5.dp, onClick = onClick) {
        Column(Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (!category.imageUrl.isNullOrBlank()) {
                MsImage(category.imageUrl, null, Modifier.size(58.dp), contentScale = ContentScale.Fit, shape = RoundedCornerShape(12.dp))
            } else {
                IconTile(categoryIcon(category.slug), size = 58.dp)
            }
            Spacer(Modifier.height(8.dp))
            Text(category.nameKa, style = msText(11.5.sp, FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ============================================================
// Banners
// ============================================================

private fun highlightBrand(title: String) = buildAnnotatedString {
    val key = "MyShop"
    val i = title.indexOf(key)
    if (i < 0) { append(title); return@buildAnnotatedString }
    append(title.substring(0, i))
    withStyle(SpanStyle(color = MsColor.Gold)) {
        // keep a Georgian suffix glued to the brand ("MyShop-ში!")
        val end = title.indexOfAny(charArrayOf(' ', '\n'), i).let { if (it < 0) title.length else it }
        append(title.substring(i, end))
        append(title.substring(end))
    }
}

/** Home hero (reference 1). Uses the banner artwork when the admin uploaded one. */
@Composable
fun HeroBanner(banner: Banner?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val title = banner?.titleKa ?: "უკეთესი შეთავაზებები MyShop-ში!"
    val cta = banner?.ctaTextKa ?: "იხილე შეთავაზებები"
    val image = banner?.displayImage
    Box(
        modifier
            .fillMaxWidth()
            .height(196.dp)
            .msShadow(RoundedCornerShape(26.dp), 12.dp, 1.3f)
            .clip(RoundedCornerShape(26.dp))
            .background(MsBrush.HeroBanner)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        if (!image.isNullOrBlank()) {
            MsImage(image, title, Modifier.fillMaxSize(), shape = RoundedCornerShape(0.dp))
        } else {
            // Decorative glow + illustrations
            Box(Modifier.size(220.dp).offset(x = (-60).dp, y = 20.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)))
            Image(painterResource(R.drawable.ill_bags), null, Modifier.align(Alignment.BottomStart).padding(start = 10.dp, bottom = 8.dp).size(width = 150.dp, height = 125.dp))
            Image(painterResource(R.drawable.ill_percent), null, Modifier.align(Alignment.TopEnd).padding(top = 12.dp, end = 12.dp).size(46.dp).rotate(14f))
            Image(painterResource(R.drawable.ill_gift), null, Modifier.align(Alignment.BottomEnd).offset(x = 8.dp, y = 10.dp).size(78.dp))
            Column(
                Modifier.align(Alignment.CenterEnd).fillMaxWidth(0.58f).padding(end = 18.dp),
            ) {
                Text(
                    highlightBrand(title),
                    style = msText(21.sp, FontWeight.ExtraBold, Color.White, lineHeight = 27.sp),
                    maxLines = 3,
                )
                Spacer(Modifier.height(14.dp))
                MsGoldPill(cta, onClick, small = true)
            }
        }
    }
}

/** Two-up promo card (reference 1: delivery / secure order). */
@Composable
fun PromoCard(
    title: String,
    subtitle: String,
    brush: Brush,
    @DrawableRes illustration: Int,
    modifier: Modifier = Modifier,
    cta: String? = null,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .height(150.dp)
            .msShadow(RoundedCornerShape(22.dp), 8.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(brush)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Image(
            painterResource(illustration),
            null,
            Modifier.align(Alignment.BottomEnd).padding(end = 6.dp, bottom = 10.dp).size(78.dp).alpha(0.96f),
        )
        Column(Modifier.fillMaxHeight().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(title, style = msText(15.5.sp, FontWeight.ExtraBold, Color.White, lineHeight = 19.sp), maxLines = 2)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = msText(11.5.sp, FontWeight.Medium, Color.White.copy(alpha = 0.92f), lineHeight = 15.sp), maxLines = 3, modifier = Modifier.fillMaxWidth(0.72f))
            }
            if (cta != null) MsGoldPill(cta, onClick, small = true)
        }
    }
}

/** "უფასო მიწოდება თბილისში!" banner (reference 2 bottom). */
@Composable
fun FreeDeliveryBanner(threshold: Double, city: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(128.dp)
            .msShadow(RoundedCornerShape(24.dp), 10.dp, 1.2f)
            .clip(RoundedCornerShape(24.dp))
            .background(MsBrush.HeroBanner)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Image(painterResource(R.drawable.ill_bags), null, Modifier.align(Alignment.BottomStart).padding(start = 6.dp, bottom = 6.dp).size(width = 104.dp, height = 88.dp))
        Image(painterResource(R.drawable.ill_truck), null, Modifier.align(Alignment.TopEnd).padding(top = 10.dp, end = 10.dp).size(width = 96.dp, height = 64.dp))
        Column(Modifier.align(Alignment.CenterStart).padding(start = 118.dp, end = 12.dp)) {
            Text(
                buildAnnotatedString {
                    append("უფასო მიწოდება\n")
                    withStyle(SpanStyle(color = MsColor.Gold)) { append("${cityLocative(city)}!") }
                },
                style = msText(17.sp, FontWeight.ExtraBold, Color.White, lineHeight = 21.sp),
            )
            Spacer(Modifier.height(4.dp))
            Text("შეკვეთისას ${formatGel(threshold)}-ზე მეტი", style = msText(11.5.sp, FontWeight.Medium, Color.White))
        }
    }
}

/** თბილისი → თბილისში (works for the common -ი ending). */
fun cityLocative(city: String): String = if (city.endsWith("ი")) city.dropLast(1) + "ში" else "$city-ში"

/** Category header banner on the product list (reference 3). */
@Composable
fun CategoryHeroBanner(title: String, subtitle: String?, iconSlug: String?, onBack: (() -> Unit)?, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .height(112.dp)
            .msShadow(RoundedCornerShape(24.dp), 10.dp, 1.2f)
            .clip(RoundedCornerShape(24.dp))
            .background(MsBrush.HeroBanner)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(78.dp).clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(iconSlug?.let { categoryIcon(it) } ?: Icons.Rounded.GridView, null, tint = Color.White, modifier = Modifier.size(46.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = msText(20.sp, FontWeight.ExtraBold, Color.White), maxLines = 1, overflow = TextOverflow.Ellipsis)
            subtitle?.let { Text(it, style = msText(14.sp, FontWeight.Medium, Color.White.copy(alpha = 0.9f))) }
        }
        if (onBack != null) {
            Row(
                Modifier
                    .clip(MsShape.Pill)
                    .background(MsColor.HeaderButton.copy(alpha = 0.7f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), MsShape.Pill)
                    .clickable(role = Role.Button, onClick = onBack)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("კატეგორიები", style = msText(12.sp, FontWeight.Bold, Color.White))
            }
        }
    }
}

// ============================================================
// Menu sheet (hamburger on home / cart) and info dialog
// ============================================================

data class MenuEntry(val icon: ImageVector, val title: String, val subtitle: String, val route: String)

val mainMenuEntries = listOf(
    MenuEntry(Icons.Rounded.GridView, "კატეგორიები", "ყველა პროდუქტი კატეგორიების მიხედვით", "categories"),
    MenuEntry(Icons.Rounded.Receipt, "ჩემი შეკვეთები", "ისტორია და სტატუსი", "orders"),
    MenuEntry(Icons.Rounded.Favorite, "რჩეული პროდუქტები", "შენახული პროდუქტები", "favorites"),
    MenuEntry(Icons.Rounded.LocationOn, "ჩემი მისამართები", "მიწოდების მისამართები", "addresses"),
    MenuEntry(Icons.Rounded.Textsms, "ჩათი", "მიმოწერა ადმინისტრატორთან", "chats"),
    MenuEntry(Icons.Rounded.Notifications, "შეტყობინებები", "სტატუსები და სიახლეები", "notifications"),
    MenuEntry(Icons.Rounded.Person, "პროფილი", "ანგარიში და პარამეტრები", "profile"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuSheet(onDismiss: () -> Unit, onNavigate: (String) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = MsShape.Sheet,
        containerColor = Color.White,
    ) {
        Column(Modifier.navigationBarsPadding().padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
            MyShopMenuHeader()
            Spacer(Modifier.height(12.dp))
            mainMenuEntries.forEach { e ->
                MenuRow(e.icon, e.title, e.subtitle, onClick = { onDismiss(); onNavigate(e.route) })
            }
        }
    }
}

@Composable
private fun MyShopMenuHeader() {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(MsBrush.HeroBanner).padding(14.dp),
        contentAlignment = Alignment.CenterStart,
    ) { MyShopLogo() }
}

/** List row with filled purple icon, title, subtitle and chevron (reference 8 menu). */
@Composable
fun MenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: Int = 0,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MsColor.Purple600, modifier = Modifier.size(34.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = msText(15.sp, FontWeight.ExtraBold), maxLines = 1)
            subtitle?.let { Text(it, style = msText(12.sp, FontWeight.Medium, MsColor.InkSoft), maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
        if (badge > 0) { CountBadge(badge, Modifier.size(24.dp)); Spacer(Modifier.width(8.dp)) }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = MsColor.Purple600, modifier = Modifier.size(28.dp))
    }
}

@Composable
fun InfoDialog(title: String, lines: List<String>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = MsShape.Card,
        title = { Text(title, style = msText(19.sp, FontWeight.ExtraBold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                lines.forEach { line ->
                    Row {
                        Box(Modifier.padding(top = 7.dp).size(7.dp).clip(CircleShape).background(MsColor.Purple500))
                        Spacer(Modifier.width(10.dp))
                        Text(line, style = msText(14.sp, FontWeight.Medium, MsColor.InkSoft, lineHeight = 20.sp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("გასაგებია", style = msText(15.sp, FontWeight.Bold, MsColor.Purple600)) }
        },
    )
}

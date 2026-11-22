package ge.myshop.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import ge.myshop.app.R

// ============================================================
// 3D illustrations (transparent PNGs, Microsoft Fluent UI Emoji — MIT,
// see android/asset-licenses). Rendered with high-quality filtering so
// the 256px sources stay smooth when scaled.
// ============================================================

@Composable
fun Art3D(
    @DrawableRes id: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Image(
        bitmap = ImageBitmap.imageResource(id),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
        filterQuality = FilterQuality.High,
    )
}

/** Category slug → 3D art (pastel tiles, shortcuts, list banner). */
@DrawableRes
fun categoryArt(slug: String?): Int = when (slug) {
    "electronics" -> R.drawable.td_phone
    "clothing" -> R.drawable.td_tshirt
    "home-appliances" -> R.drawable.td_tv
    "beauty" -> R.drawable.td_lipstick
    "sports" -> R.drawable.td_ball
    "toys" -> R.drawable.td_teddy
    "accessories" -> R.drawable.td_watch
    "home-kitchen" -> R.drawable.td_cooking
    else -> R.drawable.td_package
}

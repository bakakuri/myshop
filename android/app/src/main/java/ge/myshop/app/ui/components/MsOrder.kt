package ge.myshop.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ge.myshop.app.domain.model.OrderStatus
import ge.myshop.app.ui.theme.MsBrush
import ge.myshop.app.ui.theme.MsColor
import ge.myshop.app.ui.theme.MsShape

/** Background / foreground colours for an order status pill. */
fun statusColors(status: OrderStatus): Pair<Color, Color> = when (status) {
    OrderStatus.NEW -> Color(0xFFE0EBFF) to Color(0xFF2257D6)
    OrderStatus.AWAITING_AGREEMENT -> Color(0xFFFFF1D6) to Color(0xFFB86E00)
    OrderStatus.TERMS_AGREED -> MsColor.Purple100 to MsColor.Purple700
    OrderStatus.PREPARING -> Color(0xFFE6E3FF) to Color(0xFF4338CA)
    OrderStatus.SHIPPED -> Color(0xFFDDF2FF) to Color(0xFF0369A1)
    OrderStatus.DELIVERED -> MsColor.GreenSoft to MsColor.Green
    OrderStatus.CANCELLED -> Color(0xFFFFE2E6) to MsColor.Red
}

@Composable
fun OrderStatusPill(status: OrderStatus, modifier: Modifier = Modifier) {
    val (bg, fg) = statusColors(status)
    Box(modifier.clip(MsShape.Pill).background(bg).padding(horizontal = 12.dp, vertical = 5.dp)) {
        Text(status.labelKa(), style = msText(12.sp, FontWeight.Bold, fg), maxLines = 1)
    }
}

private data class Step(val label: String, val icon: ImageVector)

private val steps = listOf(
    Step("მიღებული", Icons.Rounded.Receipt),
    Step("შეთანხმება", Icons.Rounded.Forum),
    Step("მზადდება", Icons.Rounded.Archive),
    Step("გზაშია", Icons.Rounded.LocalShipping),
    Step("მიტანილი", Icons.Rounded.Home),
)

private fun stepIndex(status: OrderStatus): Int = when (status) {
    OrderStatus.NEW -> 0
    OrderStatus.AWAITING_AGREEMENT -> 1
    OrderStatus.TERMS_AGREED -> 1
    OrderStatus.PREPARING -> 2
    OrderStatus.SHIPPED -> 3
    OrderStatus.DELIVERED -> 4
    OrderStatus.CANCELLED -> -1
}

/** Five-step progress (reference 7 "შეკვეთის სტატუსი"). */
@Composable
fun OrderStatusStepper(status: OrderStatus, modifier: Modifier = Modifier) {
    if (status == OrderStatus.CANCELLED) {
        Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Cancel, null, tint = MsColor.Red, modifier = Modifier.size(26.dp))
            Spacer(Modifier.size(8.dp))
            Text("შეკვეთა გაუქმებულია", style = msText(15.sp, FontWeight.Bold, MsColor.Red))
        }
        return
    }
    val current = stepIndex(status)
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        steps.forEachIndexed { i, step ->
            val done = i < current || (i == current && status == OrderStatus.DELIVERED)
            val active = i == current
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    // left connector
                    Box(Modifier.weight(1f).height(3.dp).background(if (i == 0) Color.Transparent else if (i <= current) MsColor.Purple500 else MsColor.Purple200))
                    Box(
                        Modifier
                            .size(if (active) 38.dp else 32.dp)
                            .clip(CircleShape)
                            .then(
                                when {
                                    done -> Modifier.background(MsBrush.PrimaryButton)
                                    active -> Modifier.background(Color.White).border(2.5.dp, MsColor.Purple500, CircleShape)
                                    else -> Modifier.background(MsColor.Purple100)
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (done) Icons.Rounded.Check else step.icon,
                            null,
                            tint = when { done -> Color.White; active -> MsColor.Purple600; else -> MsColor.Purple300 },
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    // right connector
                    Box(Modifier.weight(1f).height(3.dp).background(if (i == steps.lastIndex) Color.Transparent else if (i < current) MsColor.Purple500 else MsColor.Purple200))
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    step.label,
                    style = msText(10.5.sp, if (active) FontWeight.ExtraBold else FontWeight.SemiBold, if (i <= current) MsColor.Ink else MsColor.InkMuted),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Order row card: thumbnail, number, date, status, item count and total. */
@Composable
fun OrderSummaryCard(order: ge.myshop.app.domain.model.Order, onClick: () -> Unit, modifier: Modifier = Modifier) {
    MsCard(modifier.fillMaxWidth(), onClick = onClick) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            MsImage(
                url = order.items.firstOrNull()?.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                fallbackIcon = Icons.Rounded.Receipt,
            )
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text("შეკვეთა № ${order.orderNumber}", style = msText(15.sp, FontWeight.ExtraBold), maxLines = 1)
                Text(
                    "${formatDate(order.createdAt)} · ${order.items.sumOf { it.quantity }} პროდუქტი",
                    style = msText(12.sp, FontWeight.Medium, MsColor.InkSoft),
                    maxLines = 1,
                )
                Spacer(Modifier.size(6.dp))
                OrderStatusPill(order.status)
            }
            Spacer(Modifier.size(8.dp))
            Text(formatGel(order.total), style = msText(17.sp, FontWeight.ExtraBold, MsColor.Purple800))
        }
    }
}

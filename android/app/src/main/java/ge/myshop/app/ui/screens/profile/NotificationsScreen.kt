package ge.myshop.app.ui.screens.profile

import ge.myshop.app.ui.components.ArtTile
import ge.myshop.app.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Announcement
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Textsms
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.domain.model.AppNotification
import ge.myshop.app.domain.model.NotificationType
import ge.myshop.app.ui.common.LocalApp
import ge.myshop.app.ui.components.EmptyState
import ge.myshop.app.ui.components.ErrorState
import ge.myshop.app.ui.components.HeaderAction
import ge.myshop.app.ui.components.IconTile
import ge.myshop.app.ui.components.LoadingState
import ge.myshop.app.ui.components.MsCard
import ge.myshop.app.ui.components.MyShopPageHeader
import ge.myshop.app.ui.components.formatDate
import ge.myshop.app.ui.components.formatTime
import ge.myshop.app.ui.components.msText
import ge.myshop.app.ui.theme.MsColor

@androidx.annotation.DrawableRes
private fun artFor(type: NotificationType): Int = when (type) {
    NotificationType.ORDER_NEW -> R.drawable.td_receipt
    NotificationType.ORDER_STATUS_UPDATE -> R.drawable.td_truck
    NotificationType.NEW_MESSAGE -> R.drawable.td_chat
    NotificationType.ANNOUNCEMENT -> R.drawable.td_megaphone
    NotificationType.LOW_STOCK -> R.drawable.td_package
}

@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    onOpenOrder: (String) -> Unit = {},
    onOpenChat: (String) -> Unit = {},
    onOpenProduct: (String) -> Unit = {},
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val app = LocalApp.current
    LaunchedEffect(state.unread) { app.refreshBadges() }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            MyShopPageHeader(
                "შეტყობინებები",
                onBack = onNavigateBack,
                subtitle = if (state.unread > 0) "${state.unread} წაუკითხავი" else "ყველა წაკითხულია",
                actions = if (state.unread > 0) listOf(HeaderAction(Icons.Rounded.DoneAll, "ყველას წაკითხულად მონიშვნა", onClick = viewModel::markAllRead)) else emptyList(),
            )
        }
        when {
            !state.isLoggedIn -> item { EmptyState(R.drawable.td_lock, "შედი ანგარიშზე", subtitle = "შეტყობინებები ანგარიშზეა მიბმული", actionText = "შესვლა", onAction = app.openLogin) }
            state.isLoading -> item { LoadingState() }
            state.error != null -> item { ErrorState(state.error!!, onRetry = viewModel::load) }
            state.notifications.isEmpty() -> item { EmptyState(R.drawable.td_bell, "შეტყობინებები არ არის", subtitle = "შეკვეთის სტატუსები და ჩათის პასუხები აქ გამოჩნდება") }
            else -> items(state.notifications, key = { it.id }) { n ->
                NotificationRow(
                    n,
                    onClick = {
                        if (!n.isRead) viewModel.markRead(n.id)
                        when {
                            n.relatedConversationId != null -> onOpenChat(n.relatedConversationId)
                            n.relatedOrderId != null -> onOpenOrder(n.relatedOrderId)
                            n.relatedProductId != null -> onOpenProduct(n.relatedProductId)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 14.dp),
                )
            }
        }
    }
}

@Composable
private fun NotificationRow(n: AppNotification, onClick: () -> Unit, modifier: Modifier = Modifier) {
    MsCard(modifier.fillMaxWidth(), border = if (n.isRead) MsColor.CardBorder else MsColor.Purple300, onClick = onClick) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            ArtTile(artFor(n.type))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(n.titleKa, style = msText(15.sp, if (n.isRead) FontWeight.Bold else FontWeight.ExtraBold), modifier = Modifier.weight(1f))
                    if (!n.isRead) Box(Modifier.size(9.dp).clip(CircleShape).background(MsColor.Purple500))
                }
                n.bodyKa?.let { Text(it, style = msText(13.sp, FontWeight.Medium, MsColor.InkSoft, lineHeight = 18.sp)) }
                Text("${formatDate(n.createdAt)} ${formatTime(n.createdAt)}", style = msText(11.sp, FontWeight.Medium, MsColor.InkMuted), modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

package ge.myshop.app.ui.screens.chat

import ge.myshop.app.ui.components.Art3D
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
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Textsms
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.domain.model.Conversation
import ge.myshop.app.domain.model.MessageSender
import ge.myshop.app.ui.common.LocalApp
import ge.myshop.app.ui.components.CountBadge
import ge.myshop.app.ui.components.EmptyState
import ge.myshop.app.ui.components.ErrorState
import ge.myshop.app.ui.components.LoadingState
import ge.myshop.app.ui.components.MsCard
import ge.myshop.app.ui.components.MyShopHeader
import ge.myshop.app.ui.components.OrderStatusPill
import ge.myshop.app.ui.components.formatDate
import ge.myshop.app.ui.components.formatTime
import ge.myshop.app.ui.components.msText
import ge.myshop.app.ui.theme.MsBrush
import ge.myshop.app.ui.theme.MsColor

@Composable
fun ChatsScreen(
    onNavigateToChat: (String) -> Unit,
    viewModel: ChatsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val app = LocalApp.current
    LaunchedEffect(Unit) { viewModel.load() }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            MyShopHeader(
                bottom = {
                    Column(Modifier.padding(start = 6.dp, bottom = 6.dp)) {
                        Text("ჩათი", style = msText(30.sp, FontWeight.ExtraBold, Color.White))
                        Text(
                            app.store.chatResponseTime ?: "მიმოწერა ადმინისტრატორთან",
                            style = msText(13.sp, FontWeight.Medium, Color.White.copy(alpha = 0.9f)),
                        )
                    }
                },
            )
        }
        when {
            !state.isLoggedIn -> item {
                EmptyState(R.drawable.td_lock, "შედი ანგარიშზე", subtitle = "ჩათი ხელმისაწვდომია შესვლის შემდეგ", actionText = "შესვლა", onAction = app.openLogin)
            }
            state.isLoading -> item { LoadingState() }
            state.error != null -> item { ErrorState(state.error!!, onRetry = viewModel::load) }
            state.conversations.isEmpty() -> item {
                EmptyState(
                    R.drawable.td_chat,
                    "ჯერ მიმოწერა არ გაქვს",
                    subtitle = "შეკვეთის გაგზავნის შემდეგ ადმინისტრატორთან ჩათი აქ გამოჩნდება",
                )
            }
            else -> items(state.conversations, key = { it.id }) { c ->
                ConversationRow(c, onClick = { onNavigateToChat(c.id) }, modifier = Modifier.padding(horizontal = 14.dp))
            }
        }
    }
}

@Composable
private fun ConversationRow(c: Conversation, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val last = c.lastMessage
    val unread = c.customerUnreadCount
    MsCard(modifier.fillMaxWidth(), onClick = onClick) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(MsBrush.Lavender), contentAlignment = Alignment.BottomCenter) {
                Art3D(R.drawable.td_admin, Modifier.size(50.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        c.orderNumber?.let { "შეკვეთა № $it" } ?: "ადმინისტრატორი",
                        style = msText(15.sp, FontWeight.ExtraBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    val at = c.lastMessageAt
                    Text(if (last != null) formatTime(at) else formatDate(at), style = msText(11.sp, FontWeight.Medium, MsColor.InkMuted))
                }
                Spacer(Modifier.size(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        when {
                            last == null -> "ახალი ჩათი"
                            last.senderType == MessageSender.CUSTOMER -> "შენ: ${last.content}"
                            else -> last.content
                        },
                        style = msText(13.sp, if (unread > 0) FontWeight.Bold else FontWeight.Medium, if (unread > 0) MsColor.Ink else MsColor.InkSoft),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (unread > 0) { Spacer(Modifier.width(8.dp)); CountBadge(unread) }
                }
                c.order?.status?.let { Spacer(Modifier.size(6.dp)); OrderStatusPill(it) }
            }
        }
    }
}

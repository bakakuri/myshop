package ge.myshop.app.ui.screens.chat

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Textsms
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.domain.model.Message
import ge.myshop.app.domain.model.MessageSender
import ge.myshop.app.domain.model.OrderRef
import ge.myshop.app.ui.common.LocalApp
import ge.myshop.app.ui.components.EmptyState
import ge.myshop.app.ui.components.ErrorState
import ge.myshop.app.ui.components.HeaderAction
import ge.myshop.app.ui.components.HeaderCircleButton
import ge.myshop.app.ui.components.IconTile
import ge.myshop.app.ui.components.LoadingState
import ge.myshop.app.ui.components.MsCard
import ge.myshop.app.ui.components.MsSoftPill
import ge.myshop.app.ui.components.OrderStatusStepper
import ge.myshop.app.ui.components.formatDate
import ge.myshop.app.ui.components.formatTime
import ge.myshop.app.ui.components.msShadow
import ge.myshop.app.ui.components.msText
import ge.myshop.app.ui.theme.MsBrush
import ge.myshop.app.ui.theme.MsColor

@Composable
fun ChatDetailScreen(
    conversationId: String,
    onNavigateBack: () -> Unit,
    onNavigateToOrderDetail: (String) -> Unit,
    viewModel: ChatDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(conversationId) { viewModel.loadConversation(conversationId) }
    val state by viewModel.uiState.collectAsState()
    val app = LocalApp.current
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(state.sendError) { state.sendError?.let { app.showMessage(it); viewModel.consumeSendError() } }
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(listState.layoutInfo.totalItemsCount.coerceAtLeast(1) - 1)
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        ChatHeader(
            subtitle = app.store.chatResponseTime ?: "MyShop მხარდაჭერა",
            onBack = onNavigateBack,
            onCall = app.store.phone?.let { phone ->
                { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.filter { it.isDigit() || it == '+' }}"))) }
            },
        )

        Box(Modifier.weight(1f)) {
            when {
                state.isLoading -> LoadingState()
                state.error != null -> ErrorState(state.error!!, onRetry = { viewModel.loadConversation(conversationId, force = true) })
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    state.order?.let { o -> item(key = "order") { OrderCard(o, onOpen = { o.id?.let(onNavigateToOrderDetail) }) } }
                    if (state.messages.isEmpty()) item(key = "empty") {
                        EmptyState(Icons.Rounded.Textsms, "მიწერე ადმინისტრატორს", subtitle = "კითხვები შეკვეთის, მიწოდების ან გადახდის შესახებ")
                    }
                    items(state.messages, key = { it.id }) { m -> MessageBubble(m) }
                    state.order?.status?.let { status ->
                        item(key = "status") {
                            MsCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                                Text("შეკვეთის სტატუსი", style = msText(16.sp, FontWeight.ExtraBold))
                                Spacer(Modifier.size(12.dp))
                                OrderStatusStepper(status)
                            }
                        }
                    }
                }
            }
        }

        ChatInput(
            value = state.draft,
            onValueChange = viewModel::updateDraft,
            onSend = viewModel::send,
            sending = state.isSending,
        )
    }
}

@Composable
private fun ChatHeader(subtitle: String, onBack: () -> Unit, onCall: (() -> Unit)?) {
    Box(Modifier.fillMaxWidth().background(MsBrush.Header)) {
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderCircleButton(HeaderAction(Icons.AutoMirrored.Rounded.ArrowBack, "უკან", onClick = onBack))
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(Color.White).border(2.dp, MsColor.Gold, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.HeadsetMic, null, tint = MsColor.Purple600, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("ადმინისტრატორი", style = msText(17.sp, FontWeight.ExtraBold, Color.White), maxLines = 1)
                Text(subtitle, style = msText(11.5.sp, FontWeight.Medium, Color.White.copy(alpha = 0.88f)), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (onCall != null) HeaderCircleButton(HeaderAction(Icons.Rounded.Call, "დარეკვა", onClick = onCall))
        }
    }
}

@Composable
private fun OrderCard(order: OrderRef, onOpen: () -> Unit) {
    MsCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconTile(Icons.Rounded.Receipt, size = 48.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("შეკვეთა № ${order.orderNumber ?: ""}", style = msText(15.sp, FontWeight.ExtraBold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(formatDate(order.createdAt), style = msText(12.sp, FontWeight.Medium, MsColor.InkSoft))
            }
            if (order.id != null) MsSoftPill("დეტალები", onOpen)
        }
    }
}

@Composable
private fun MessageBubble(m: Message) {
    val mine = m.senderType == MessageSender.CUSTOMER
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!mine) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(MsBrush.PrimaryButton), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.HeadsetMic, null, tint = Color.White, modifier = Modifier.size(17.dp))
            }
            Spacer(Modifier.width(8.dp))
        }
        val shape = if (mine) RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp) else RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp)
        Column(
            Modifier
                .widthIn(max = 290.dp)
                .msShadow(shape, 3.dp)
                .clip(shape)
                .background(if (mine) MsColor.Purple100 else Color.White)
                .border(1.dp, if (mine) MsColor.Purple200 else MsColor.CardBorder, shape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            if (!mine) Text(m.senderName ?: "ადმინისტრატორი", style = msText(12.sp, FontWeight.ExtraBold, MsColor.Purple700))
            Text(m.content, style = msText(14.5.sp, FontWeight.Medium, MsColor.Ink, lineHeight = 20.sp))
            Row(Modifier.align(Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                Text(formatTime(m.createdAt), style = msText(10.5.sp, FontWeight.Medium, MsColor.InkMuted))
                if (mine) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        if (m.isRead) Icons.Rounded.DoneAll else Icons.Rounded.Done,
                        if (m.isRead) "წაკითხულია" else "გაგზავნილია",
                        tint = if (m.isRead) MsColor.Purple600 else MsColor.InkMuted,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInput(value: String, onValueChange: (String) -> Unit, onSend: () -> Unit, sending: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MsColor.Background)
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .weight(1f)
                .heightIn(min = 52.dp)
                .msShadow(RoundedCornerShape(26.dp), 6.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Color.White)
                .border(1.dp, MsColor.CardBorder, RoundedCornerShape(26.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) Text("დაწერე შეტყობინება...", style = msText(15.sp, FontWeight.Medium, MsColor.InkMuted))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = msText(15.sp, FontWeight.Medium, MsColor.Ink),
                cursorBrush = SolidColor(MsColor.Purple600),
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.width(10.dp))
        val enabled = value.isNotBlank() && !sending
        Box(
            Modifier
                .size(52.dp)
                .msShadow(CircleShape, 8.dp, 1.4f)
                .clip(CircleShape)
                .background(if (enabled) MsBrush.PrimaryButton else SolidColor(MsColor.Purple300))
                .clickable(enabled = enabled, role = Role.Button, onClickLabel = "გაგზავნა", onClick = onSend),
            contentAlignment = Alignment.Center,
        ) {
            if (sending) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            else Icon(Icons.AutoMirrored.Rounded.Send, "გაგზავნა", tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }
}

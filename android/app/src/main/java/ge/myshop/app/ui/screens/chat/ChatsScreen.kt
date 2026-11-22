package ge.myshop.app.ui.screens.chat

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.domain.model.Conversation
import ge.myshop.app.ui.components.EmptyState
import ge.myshop.app.ui.components.ScreenHeader
import ge.myshop.app.ui.theme.PrimaryPurple
import java.time.format.DateTimeFormatter

@Composable
fun ChatsScreen(
    onChatClick: (conversationId: String) -> Unit,
    viewModel: ChatsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { ScreenHeader("შეტყობინებები") },
    ) { padding ->
        if (uiState.conversations.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = "💬",
                    title = "ჩატი არ გაქვს",
                    subtitle = "შეკვეთის განთავსების შემდეგ ჩვენ გიკავშირდებით",
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 80.dp,
                ),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(uiState.conversations, key = { it.id }) { conversation ->
                    ConversationItem(
                        conversation = conversation,
                        onClick = { onChatClick(conversation.id) },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun ConversationItem(conversation: Conversation, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Shop avatar
        Surface(
            modifier = Modifier.size(48.dp).clip(CircleShape),
            color = PrimaryPurple,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("M", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "შეკვეთა #${conversation.orderNumber ?: conversation.id.take(6)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (conversation.customerUnreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                )
                conversation.lastMessageAt?.let { ts ->
                    Text(
                        text = ts.substringBefore("T").let {
                            try { it } catch (e: Exception) { "" }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = conversation.lastMessage ?: "ჩატი",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (conversation.customerUnreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (conversation.customerUnreadCount > 0) {
                    Badge(containerColor = PrimaryPurple) {
                        Text(conversation.customerUnreadCount.toString(), color = Color.White)
                    }
                }
            }
        }
    }
}

package ge.myshop.app.data.repository

import ge.myshop.app.domain.model.Conversation
import ge.myshop.app.domain.model.Message
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.postgrest.query.Count

@Singleton
class ChatRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {

    // Active realtime channel per conversation
    private var activeChannel: RealtimeChannel? = null

    suspend fun getConversations(): Result<List<Conversation>> {
        return runCatching {
            val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not authenticated")
            // Get conversations where the user is a participant
            supabase.postgrest
                .from("conversations")
                .select(
                    Columns.raw(
                        """
                        *,
                        order:orders!order_id(order_number, status),
                        messages(id, conversation_id, sender_type, content, is_read, is_internal_note, created_at),
                        participants:conversation_participants!inner(user_id)
                        """.trimIndent()
                    )
                ) {
                    filter {
                        eq("participants.user_id", userId)
                    }
                    order("updated_at", Order.DESCENDING)
                }
                .decodeList<Conversation>()
        }
    }

    suspend fun getConversationByOrderId(orderId: String): Result<Conversation?> {
        return runCatching {
            supabase.postgrest
                .from("conversations")
                .select(Columns.ALL) {
                    filter { eq("order_id", orderId) }
                }
                .decodeList<Conversation>()
                .firstOrNull()
        }
    }

    suspend fun getMessages(conversationId: String, limit: Int = 50, offset: Int = 0): Result<List<Message>> {
        return runCatching {
            supabase.postgrest
                .from("messages")
                .select(
                    Columns.raw(
                        """
                        *,
                        sender:profiles!sender_id(full_name, avatar_url),
                        attachments:message_attachments(*)
                        """.trimIndent()
                    )
                ) {
                    filter {
                        eq("conversation_id", conversationId)
                        eq("is_internal_note", false) // Customers never see internal notes
                    }
                    order("created_at", Order.DESCENDING)
                    range(from = offset.toLong(), to = (offset + limit - 1).toLong())
                }
                .decodeList<Message>()
                .reversed() // Oldest first for display
        }
    }

    suspend fun sendMessage(conversationId: String, content: String): Result<Message> {
        return runCatching {
            val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not authenticated")
            supabase.postgrest
                .from("messages")
                .insert(
                    buildJsonObject {
                        put("conversation_id", conversationId)
                        put("sender_id", userId)
                        put("sender_type", "customer")
                        put("content", content)
                    }
                ) {
                    select(Columns.ALL)
                }
                .decodeSingle<Message>()
        }
    }

    suspend fun markConversationRead(conversationId: String): Result<Unit> {
        return runCatching {
            val params = buildJsonObject { put("p_conversation_id", conversationId) }
            supabase.postgrest.rpc("mark_conversation_read", params)
        }
    }

    // Subscribe to real-time messages for a conversation
    suspend fun subscribeToMessages(conversationId: String): Flow<Message> = flow {
        // Unsubscribe from any previous channel
        activeChannel?.unsubscribe()

        val channel = supabase.realtime.channel("chat-$conversationId")
        activeChannel = channel

        // Listen for new inserts in messages table for this conversation
        val changeFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "messages"
            filter("conversation_id", FilterOperator.EQ, conversationId)
        }

        channel.subscribe()

        changeFlow.collect { change ->
            val message = change.decodeRecord<Message>()
            // RLS already hides internal notes from customers; double-check client-side
            if (!message.isInternalNote) {
                emit(message)
            }
        }
    }

    suspend fun unsubscribe() {
        activeChannel?.unsubscribe()
        activeChannel = null
    }

    suspend fun getUnreadCount(): Result<Int> {
        return runCatching {
            val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not authenticated")
            supabase.postgrest
                .from("messages")
                .select(Columns.raw("id")) {
                    count(Count.EXACT)
                    filter {
                        eq("is_read", false)
                        eq("is_internal_note", false)
                        neq("sender_type", "customer")
                        // Participant check handled by RLS
                    }
                }
                .countOrNull()?.toInt() ?: 0
        }
    }
}

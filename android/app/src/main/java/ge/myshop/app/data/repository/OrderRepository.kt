package ge.myshop.app.data.repository

import ge.myshop.app.domain.model.CreateOrderItem
import ge.myshop.app.domain.model.Order
import ge.myshop.app.domain.model.OrderStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order as PgOrder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import ge.myshop.app.domain.model.CreateOrderResult

@Singleton
class OrderRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {

    // Create order via RPC — server validates prices, stock, auth
    suspend fun createOrder(
        customerName: String,
        customerPhone: String,
        customerEmail: String? = null,
        deliveryAddress: String? = null,
        deliveryCity: String? = null,
        deliveryNotes: String? = null,
        customerNote: String? = null,
        items: List<CreateOrderItem>,
        // Reuse the same key when retrying one checkout so the server never creates a duplicate order
        idempotencyKey: String = UUID.randomUUID().toString(),
    ): Result<CreateOrderResult> {
        return runCatching {
            val params = buildJsonObject {
                put("p_customer_name", customerName)
                put("p_customer_phone", customerPhone)
                customerEmail?.let { put("p_customer_email", it) }
                deliveryAddress?.let { put("p_delivery_address", it) }
                deliveryCity?.let { put("p_delivery_city", it) }
                deliveryNotes?.let { put("p_delivery_notes", it) }
                customerNote?.let { put("p_customer_note", it) }
                put("p_idempotency_key", idempotencyKey)
                put("p_items", Json.encodeToJsonElement(items))
            }
            supabase.postgrest
                .rpc("create_order", params)
                .decodeAs<CreateOrderResult>()
        }
    }

    suspend fun getOrders(): Result<List<Order>> {
        return runCatching {
            val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not authenticated")
            supabase.postgrest
                .from("orders")
                .select(
                    Columns.raw(
                        """
                        *,
                        items:order_items(*)
                        """.trimIndent()
                    )
                ) {
                    filter { eq("user_id", userId) }
                    order("created_at", PgOrder.DESCENDING)
                }
                .decodeList<Order>()
        }
    }

    suspend fun getOrder(orderId: String): Result<Order> {
        return runCatching {
            supabase.postgrest
                .from("orders")
                .select(
                    Columns.raw(
                        """
                        *,
                        items:order_items(*),
                        statusHistory:order_status_history(*),
                        conversations(id)
                        """.trimIndent()
                    )
                ) {
                    filter { eq("id", orderId) }
                }
                .decodeSingle<Order>()
        }
    }

    suspend fun getOrderByNumber(orderNumber: String): Result<Order> {
        return runCatching {
            supabase.postgrest
                .from("orders")
                .select(
                    Columns.raw("*, items:order_items(*), statusHistory:order_status_history(*), conversations(id)")
                ) {
                    filter { eq("order_number", orderNumber) }
                }
                .decodeSingle<Order>()
        }
    }
}

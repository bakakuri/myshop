package ge.myshop.app.data.repository

import ge.myshop.app.domain.model.Cart
import ge.myshop.app.domain.model.CartSummary
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {

    // Get or create cart via RPC (handles auth check server-side)
    private suspend fun getOrCreateCartId(): String {
        return supabase.postgrest
            .rpc("get_or_create_cart")
            .decodeAs<String>()
    }

    suspend fun getCartSummary(): Result<CartSummary> {
        return runCatching {
            supabase.postgrest
                .rpc("get_cart_summary")
                .decodeAs<CartSummary>()
        }
    }

    suspend fun upsertItem(productId: String, variantId: String? = null, quantity: Int): Result<Unit> {
        return runCatching {
            val params = buildJsonObject {
                put("p_product_id", productId)
                variantId?.let { put("p_variant_id", it) }
                put("p_quantity", quantity)
            }
            supabase.postgrest
                .rpc("upsert_cart_item", params)
        }
    }

    suspend fun removeItem(cartItemId: String): Result<Unit> {
        return runCatching {
            supabase.postgrest
                .from("cart_items")
                .delete {
                    filter { eq("id", cartItemId) }
                }
        }
    }

    suspend fun clearCart(): Result<Unit> {
        return runCatching {
            val cartId = getOrCreateCartId()
            supabase.postgrest
                .from("cart_items")
                .delete {
                    filter { eq("cart_id", cartId) }
                }
        }
    }

    suspend fun getItemCount(): Result<Int> {
        return runCatching {
            val summary = getCartSummary().getOrThrow()
            summary.itemCount
        }
    }
}

package ge.myshop.app.data.repository

import ge.myshop.app.domain.model.Favorite
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {

    suspend fun getFavorites(): Result<List<Favorite>> {
        return runCatching {
            supabase.postgrest
                .from("favorites")
                .select(
                    Columns.raw("*, product:products(*, images:product_images(*))")
                ) {
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Favorite>()
        }
    }

    suspend fun toggleFavorite(productId: String): Result<Boolean> {
        return runCatching {
            val params = buildJsonObject { put("p_product_id", productId) }
            // RPC returns true if added, false if removed
            supabase.postgrest
                .rpc("toggle_favorite", params)
                .decodeAs<Boolean>()
        }
    }

    suspend fun isFavorite(productId: String): Result<Boolean> {
        return runCatching {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@runCatching false
            val count = supabase.postgrest
                .from("favorites")
                .select(Columns.raw("count")) {
                    filter {
                        eq("user_id", userId)
                        eq("product_id", productId)
                    }
                }
                .decodeAs<Long>()
            count > 0L
        }
    }

    suspend fun getFavoriteProductIds(): Result<Set<String>> {
        return runCatching {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@runCatching emptySet()
            supabase.postgrest
                .from("favorites")
                .select(Columns.raw("product_id")) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<Map<String, String>>()
                .mapNotNull { it["product_id"] }
                .toSet()
        }
    }
}

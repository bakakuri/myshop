package ge.myshop.app.data.repository

import ge.myshop.app.domain.model.Address
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    private val userId get() = supabase.auth.currentUserOrNull()?.id

    suspend fun getAddresses(): Result<List<Address>> = runCatching {
        val uid = userId ?: return Result.success(emptyList())
        supabase.postgrest["addresses"]
            .select {
                filter { eq("user_id", uid) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList()
    }

    /** Inserts and returns the created row (user_id always comes from the session, never the form). */
    suspend fun addAddress(address: Address): Result<Address> = runCatching {
        val uid = userId ?: error("Not authenticated")
        supabase.postgrest["addresses"].insert(address.copy(userId = uid)) { select() }.decodeSingle<Address>()
    }

    /** Makes [id] the only default address of the signed-in user (RLS limits both updates to own rows). */
    suspend fun setDefault(id: String): Result<Unit> = runCatching {
        val uid = userId ?: error("Not authenticated")
        supabase.postgrest["addresses"].update({ set("is_default", false) }) { filter { eq("user_id", uid) } }
        supabase.postgrest["addresses"].update({ set("is_default", true) }) { filter { eq("id", id) } }
    }

    suspend fun deleteAddress(id: String): Result<Unit> = runCatching {
        supabase.postgrest["addresses"].delete {
            filter { eq("id", id) }
        }
    }
}

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

    suspend fun addAddress(address: Address): Result<Unit> = runCatching {
        val uid = userId ?: error("Not authenticated")
        supabase.postgrest["addresses"].insert(address.copy(userId = uid))
    }

    suspend fun deleteAddress(id: String): Result<Unit> = runCatching {
        supabase.postgrest["addresses"].delete {
            filter { eq("id", id) }
        }
    }
}

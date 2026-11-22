package ge.myshop.app.data.repository

import ge.myshop.app.domain.model.StoreSetting
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

/** Public store settings managed in the admin panel (store_settings is readable by everyone). */
data class StoreInfo(
    val storeName: String = "MyShop",
    val tagline: String = "შენი ონლაინ მაღაზია",
    val deliveryEnabled: Boolean = true,
    val deliveryEstimate: String = "1-3 დღეში",
    val freeDeliveryThreshold: Double? = null,
    val pickupEnabled: Boolean = false,
    val city: String = "თბილისი",
    val phone: String? = null,
    val email: String? = null,
    val workingHours: String? = null,
    val chatResponseTime: String? = null,
    val orderConfirmation: String? = null,
    val requireAddress: Boolean = false,
) {
    /** "1-3 დღეში" → "1-3 დღე" for compact strips */
    val deliveryShort: String get() = deliveryEstimate.removeSuffix("ში").trim()
}

@Singleton
class SettingsRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    suspend fun getStoreInfo(): Result<StoreInfo> = runCatching {
        val map = supabase.postgrest
            .from("store_settings")
            .select(Columns.list("id", "namespace", "key", "value")) {
                filter { isIn("namespace", listOf("brand", "delivery", "store_info", "chat", "order")) }
            }
            .decodeList<StoreSetting>()
            .associate { "${it.namespace}.${it.key}" to (it.value ?: "") }

        fun s(k: String) = map[k]?.takeIf { it.isNotBlank() }
        fun b(k: String, d: Boolean) = map[k]?.toBooleanStrictOrNull() ?: d

        StoreInfo(
            storeName = s("brand.store_name") ?: "MyShop",
            tagline = s("brand.tagline_ka") ?: "შენი ონლაინ მაღაზია",
            deliveryEnabled = b("delivery.enabled", true),
            deliveryEstimate = s("delivery.estimated_days_ka") ?: "1-3 დღეში",
            freeDeliveryThreshold = s("delivery.free_threshold")?.toDoubleOrNull(),
            pickupEnabled = b("delivery.pickup_enabled", false),
            city = s("store_info.city") ?: "თბილისი",
            phone = s("store_info.phone_primary"),
            email = s("store_info.email_support"),
            workingHours = s("store_info.working_hours_ka"),
            chatResponseTime = s("chat.response_time_ka"),
            orderConfirmation = s("order.confirmation_text_ka"),
            requireAddress = b("order.require_address", false),
        )
    }
}

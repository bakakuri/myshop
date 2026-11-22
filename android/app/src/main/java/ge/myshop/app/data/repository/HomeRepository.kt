package ge.myshop.app.data.repository

import ge.myshop.app.domain.model.AppNotification
import ge.myshop.app.domain.model.Banner
import ge.myshop.app.domain.model.HomeSection
import ge.myshop.app.domain.model.HomeSectionType
import ge.myshop.app.domain.model.Product
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import io.github.jan.supabase.postgrest.query.Count

@Singleton
class HomeRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {

    suspend fun getActiveBanners(): Result<List<Banner>> {
        return runCatching {
            supabase.postgrest
                .from("banners")
                .select(Columns.ALL) {
                    filter { eq("is_active", true) }
                    order("sort_order", Order.ASCENDING)
                }
                .decodeList<Banner>()
        }
    }

    suspend fun getHomeSections(): Result<List<HomeSection>> {
        return runCatching {
            supabase.postgrest
                .from("home_sections")
                .select(Columns.ALL) {
                    filter { eq("is_enabled", true) }
                    order("sort_order", Order.ASCENDING)
                }
                .decodeList<HomeSection>()
        }
    }

    suspend fun getNotifications(): Result<List<AppNotification>> {
        return runCatching {
            val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not authenticated")
            supabase.postgrest
                .from("notifications")
                .select(Columns.ALL) {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                    limit(50)
                }
                .decodeList<AppNotification>()
        }
    }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> {
        return runCatching {
            supabase.postgrest
                .from("notifications")
                .update({ set("is_read", true) }) {
                    filter { eq("id", notificationId) }
                }
        }
    }

    suspend fun markAllNotificationsRead(): Result<Unit> {
        return runCatching {
            val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not authenticated")
            supabase.postgrest
                .from("notifications")
                .update({ set("is_read", true) }) {
                    filter {
                        eq("user_id", userId)
                        eq("is_read", false)
                    }
                }
        }
    }

    suspend fun getUnreadNotificationCount(): Result<Int> {
        return runCatching {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@runCatching 0
            supabase.postgrest
                .from("notifications")
                .select(Columns.raw("id")) {
                    count(Count.EXACT)
                    filter {
                        eq("user_id", userId)
                        eq("is_read", false)
                    }
                }
                .countOrNull()?.toInt() ?: 0
        }
    }
}

package ge.myshop.app.data.repository

import ge.myshop.app.domain.model.UserProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.Phone
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    private val auth: Auth get() = supabase.auth

    // Current session state
    val currentUser: UserInfo? get() = auth.currentUserOrNull()
    val isLoggedIn: Boolean get() = currentUser != null

    // Session changes (Realtime updates)
    val sessionFlow = auth.sessionStatus

    suspend fun signUpWithEmail(email: String, password: String, fullName: String): Result<Unit> {
        return runCatching {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject { put("full_name", fullName) }
            }
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return runCatching {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }
    }

    suspend fun sendOtp(phone: String): Result<Unit> {
        return runCatching {
            auth.signInWith(Phone) {
                this.phone = phone
            }
        }
    }

    suspend fun verifyOtp(phone: String, token: String): Result<Unit> {
        return runCatching {
            auth.verifyPhoneOtp(type = OtpType.Phone.SMS, phone = phone, token = token)
        }
    }

    suspend fun signOut(): Result<Unit> {
        return runCatching {
            auth.signOut()
        }
    }

    suspend fun resetPasswordEmail(email: String): Result<Unit> {
        return runCatching {
            auth.resetPasswordForEmail(email)
        }
    }

    suspend fun getProfile(): Result<UserProfile> {
        return runCatching {
            val userId = currentUser?.id ?: error("Not authenticated")
            supabase.postgrest
                .from("profiles")
                .select(Columns.ALL) {
                    filter { eq("id", userId) }
                }
                .decodeSingle<UserProfile>()
                .copy(email = currentUser?.email) // email lives in auth.users, not profiles
        }
    }

    suspend fun updateProfile(fullName: String, phone: String?): Result<UserProfile> {
        return runCatching {
            val userId = currentUser?.id ?: error("Not authenticated")
            supabase.postgrest
                .from("profiles")
                .update({
                    set("full_name", fullName)
                    set("phone", phone)
                }) {
                    filter { eq("id", userId) }
                    select(Columns.ALL)
                }
                .decodeSingle<UserProfile>()
        }
    }
}

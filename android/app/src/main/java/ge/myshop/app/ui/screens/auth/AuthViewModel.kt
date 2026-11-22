package ge.myshop.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    /** Sign-up succeeded but Supabase requires the e-mail link to be confirmed first. */
    val confirmationSentTo: String? = null,
    val error: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "ელ-ფოსტა და პაროლი სავალდებულოა") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.signInWithEmail(email.trim(), password)
                .onSuccess { _uiState.update { it.copy(isLoading = false, isLoggedIn = true) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = friendly(e)) } }
        }
    }

    fun signUp(email: String, password: String, name: String) {
        when {
            name.isBlank() || email.isBlank() || password.isBlank() -> { _uiState.update { it.copy(error = "ყველა ველი სავალდებულოა") }; return }
            !email.trim().matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) -> { _uiState.update { it.copy(error = "ელ-ფოსტა არასწორია") }; return }
            password.length < 8 -> { _uiState.update { it.copy(error = "პაროლი მინიმუმ 8 სიმბოლო უნდა იყოს") }; return }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.signUpWithEmail(email.trim(), password, name.trim())
                .onSuccess {
                    // With e-mail confirmation on, Supabase creates the user but no session yet
                    val hasSession = authRepository.currentUser != null
                    _uiState.update {
                        it.copy(isLoading = false, isLoggedIn = hasSession, confirmationSentTo = if (hasSession) null else email.trim())
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = friendly(e)) } }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    /** Supabase returns English technical messages; show Georgian ones. */
    private fun friendly(e: Throwable): String {
        val m = (e.message ?: "").lowercase()
        return when {
            "invalid login" in m || "invalid credentials" in m -> "ელ-ფოსტა ან პაროლი არასწორია"
            "not confirmed" in m -> "ჯერ დაადასტურე ელ-ფოსტა — ბმული მეილზე გამოგიგზავნეთ"
            "already registered" in m || "already exists" in m -> "ეს ელ-ფოსტა უკვე რეგისტრირებულია — სცადე შესვლა"
            "password" in m && ("weak" in m || "least" in m) -> "პაროლი ძალიან სუსტია — გამოიყენე მინიმუმ 8 სიმბოლო"
            "rate limit" in m || "too many" in m -> "ძალიან ბევრი მცდელობა — სცადე რამდენიმე წუთში"
            "network" in m || "timeout" in m || "unable to resolve" in m || "failed to connect" in m -> "კავშირის შეცდომა — შეამოწმე ინტერნეტი"
            else -> "მოქმედება ვერ შესრულდა — სცადე ხელახლა"
        }
    }
}

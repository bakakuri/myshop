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
                .fold(
                    onSuccess = { _uiState.update { it.copy(isLoading = false, isLoggedIn = true) } },
                    onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "შეცდომა შესვლისას") } },
                )
        }
    }

    fun signUp(email: String, password: String, name: String) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _uiState.update { it.copy(error = "ყველა ველი სავალდებულოა") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(error = "პაროლი მინიმუმ 6 სიმბოლო უნდა იყოს") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.signUpWithEmail(email.trim(), password, name.trim())
                .fold(
                    onSuccess = { _uiState.update { it.copy(isLoading = false, isLoggedIn = true) } },
                    onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "შეცდომა რეგისტრაციისას") } },
                )
        }
    }

    fun sendOtp(phone: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.sendOtp(phone)
                .fold(
                    onSuccess = { _uiState.update { it.copy(isLoading = false) } },
                    onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "კოდის გაგზავნა ვერ მოხდა") } },
                )
        }
    }

    fun verifyOtp(phone: String, token: String) {
        if (token.isBlank()) {
            _uiState.update { it.copy(error = "კოდი სავალდებულოა") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.verifyOtp(phone, token)
                .fold(
                    onSuccess = { _uiState.update { it.copy(isLoading = false, isLoggedIn = true) } },
                    onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "კოდი არასწორია") } },
                )
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}

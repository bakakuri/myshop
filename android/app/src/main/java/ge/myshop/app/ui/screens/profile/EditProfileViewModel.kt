package ge.myshop.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.AuthRepository
import ge.myshop.app.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getProfile().onSuccess { p -> _uiState.update { it.copy(profile = p) } }
        }
    }

    fun save(fullName: String, phone: String) {
        if (fullName.isBlank()) {
            _uiState.update { it.copy(error = "სახელი სავალდებულოა") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.updateProfile(fullName = fullName, phone = phone.ifBlank { null })
                .fold(
                    onSuccess = { _uiState.update { it.copy(isLoading = false, saveSuccess = true) } },
                    onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "შეცდომა") } },
                )
        }
    }
}

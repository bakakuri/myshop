package ge.myshop.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.AuthRepository
import ge.myshop.app.data.repository.FavoritesRepository
import ge.myshop.app.data.repository.OrderRepository
import ge.myshop.app.domain.model.Order
import ge.myshop.app.domain.model.OrderStatus
import ge.myshop.app.domain.model.UserProfile
import ge.myshop.app.ui.common.ProductActions
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = true,
    val profile: UserProfile? = null,
    val orders: List<Order> = emptyList(),
    val favoriteCount: Int = 0,
) {
    val orderCount: Int get() = orders.size
    /** Sum of non-cancelled orders (informational; prices are set server-side). */
    val ordersTotal: Double get() = orders.filter { it.status != OrderStatus.CANCELLED }.sumOf { it.total }
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository,
    private val favoritesRepository: FavoritesRepository,
    private val session: ProductActions,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun load() {
        if (!session.isLoggedIn) {
            _uiState.value = ProfileUiState(isLoading = false, isLoggedIn = false)
            return
        }
        _uiState.update { it.copy(isLoading = it.profile == null, isLoggedIn = true) }
        viewModelScope.launch {
            val orders = async { orderRepository.getOrders().getOrDefault(emptyList()) }
            val favs = async { favoritesRepository.getFavoriteProductIds().getOrDefault(emptySet()) }
            val profile = authRepository.getProfile().getOrNull()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    profile = profile,
                    orders = orders.await().sortedByDescending { o -> o.createdAt },
                    favoriteCount = favs.await().size,
                )
            }
        }
    }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = ProfileUiState(isLoading = false, isLoggedIn = false)
            onDone()
        }
    }
}

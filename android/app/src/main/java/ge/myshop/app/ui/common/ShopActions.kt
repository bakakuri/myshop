package ge.myshop.app.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ge.myshop.app.data.repository.CartRepository
import ge.myshop.app.data.repository.FavoritesRepository
import ge.myshop.app.data.repository.StoreInfo
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================================
// App-wide controller (badges, snackbar, login redirect)
// ============================================================

data class AppBadges(val cart: Int = 0, val chat: Int = 0, val notifications: Int = 0)

class AppController(
    val badges: AppBadges,
    val store: StoreInfo,
    val refreshBadges: () -> Unit,
    val showMessage: (String) -> Unit,
    val openLogin: () -> Unit,
)

val LocalApp = compositionLocalOf {
    AppController(AppBadges(), StoreInfo(), refreshBadges = {}, showMessage = {}, openLogin = {})
}

sealed interface ShopEvent {
    data class Message(val text: String) : ShopEvent
    data object RequireLogin : ShopEvent
    data object CartChanged : ShopEvent
}

/** Collects a view model's [ShopEvent]s and routes them to the app controller. */
@Composable
fun HandleShopEvents(events: Flow<ShopEvent>, app: AppController) {
    LaunchedEffect(events) {
        events.collect { e ->
            when (e) {
                is ShopEvent.Message -> app.showMessage(e.text)
                ShopEvent.RequireLogin -> {
                    app.showMessage("გასაგრძელებლად შედით ანგარიშზე")
                    app.openLogin()
                }
                ShopEvent.CartChanged -> {
                    app.refreshBadges()
                    app.showMessage("კალათაში დაემატა")
                }
            }
        }
    }
}

// ============================================================
// Product actions shared by every screen that shows product cards
// ============================================================

class ProductActions @Inject constructor(
    private val supabase: SupabaseClient,
    private val cartRepository: CartRepository,
    private val favoritesRepository: FavoritesRepository,
) {
    val isLoggedIn: Boolean get() = supabase.auth.currentUserOrNull() != null

    suspend fun favoriteIds(): Set<String> =
        if (!isLoggedIn) emptySet() else favoritesRepository.getFavoriteProductIds().getOrDefault(emptySet())

    suspend fun toggleFavorite(productId: String): Result<Boolean> = favoritesRepository.toggleFavorite(productId)

    /** Adds [quantity] on top of what is already in the cart (the RPC itself sets an absolute quantity). */
    suspend fun addToCart(productId: String, variantId: String? = null, quantity: Int = 1): Result<Unit> {
        val current = cartRepository.getCartSummary().getOrNull()
            ?.items?.firstOrNull { it.productId == productId && it.variantId == variantId }
            ?.quantity ?: 0
        return cartRepository.upsertItem(productId = productId, variantId = variantId, quantity = current + quantity)
    }
}

/** Base for view models that render product cards (favourites + add to cart). */
abstract class ShopViewModel(protected val actions: ProductActions) : ViewModel() {

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _events = MutableSharedFlow<ShopEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<ShopEvent> = _events.asSharedFlow()

    protected fun emit(event: ShopEvent) { _events.tryEmit(event) }

    fun refreshFavorites() {
        viewModelScope.launch { _favoriteIds.value = actions.favoriteIds() }
    }

    open fun toggleFavorite(productId: String) {
        if (!actions.isLoggedIn) { emit(ShopEvent.RequireLogin); return }
        val wasFavorite = productId in _favoriteIds.value
        _favoriteIds.update { if (wasFavorite) it - productId else it + productId } // optimistic
        viewModelScope.launch {
            actions.toggleFavorite(productId)
                .onSuccess { fav -> _favoriteIds.update { if (fav) it + productId else it - productId } }
                .onFailure {
                    _favoriteIds.update { if (wasFavorite) it + productId else it - productId }
                    emit(ShopEvent.Message("რჩეულების განახლება ვერ მოხერხდა"))
                }
        }
    }

    fun addToCart(productId: String, variantId: String? = null, quantity: Int = 1) {
        if (!actions.isLoggedIn) { emit(ShopEvent.RequireLogin); return }
        viewModelScope.launch {
            actions.addToCart(productId, variantId, quantity)
                .onSuccess { emit(ShopEvent.CartChanged) }
                .onFailure { emit(ShopEvent.Message("კალათაში დამატება ვერ მოხერხდა")) }
        }
    }
}

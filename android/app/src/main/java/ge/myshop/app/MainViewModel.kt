package ge.myshop.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.CartRepository
import ge.myshop.app.data.repository.ChatRepository
import ge.myshop.app.data.repository.HomeRepository
import ge.myshop.app.data.repository.SettingsRepository
import ge.myshop.app.data.repository.StoreInfo
import ge.myshop.app.ui.common.AppBadges
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val supabase: SupabaseClient,
    private val cartRepository: CartRepository,
    private val chatRepository: ChatRepository,
    private val homeRepository: HomeRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _badges = MutableStateFlow(AppBadges())
    val badges: StateFlow<AppBadges> = _badges.asStateFlow()

    private val _store = MutableStateFlow(StoreInfo())
    val store: StateFlow<StoreInfo> = _store.asStateFlow()

    init {
        refreshBadges()
        viewModelScope.launch { settingsRepository.getStoreInfo().onSuccess { _store.value = it } }
    }

    /** Cart / unread chat / unread notification counters. Cheap; called on navigation. */
    fun refreshBadges() {
        if (supabase.auth.currentUserOrNull() == null) {
            _badges.value = AppBadges()
            return
        }
        viewModelScope.launch {
            launch { cartRepository.getItemCount().onSuccess { n -> _badges.update { it.copy(cart = n) } } }
            launch { chatRepository.getUnreadCount().onSuccess { n -> _badges.update { it.copy(chat = n) } } }
            launch { homeRepository.getUnreadNotificationCount().onSuccess { n -> _badges.update { it.copy(notifications = n) } } }
        }
    }
}

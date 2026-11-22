package ge.myshop.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.data.repository.AddressRepository
import ge.myshop.app.domain.model.Address
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddressesUiState(
    val addresses: List<Address> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
)

@HiltViewModel
class AddressesViewModel @Inject constructor(
    private val addressRepository: AddressRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddressesUiState())
    val uiState: StateFlow<AddressesUiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            addressRepository.getAddresses()
                .fold(
                    onSuccess = { addrs -> _uiState.update { it.copy(addresses = addrs, isLoading = false) } },
                    onFailure = { _uiState.update { it.copy(isLoading = false) } },
                )
        }
    }

    fun deleteAddress(id: String) {
        viewModelScope.launch {
            addressRepository.deleteAddress(id)
            load()
        }
    }

    fun showAddDialog() = _uiState.update { it.copy(showAddDialog = true) }
    fun hideAddDialog() = _uiState.update { it.copy(showAddDialog = false) }
}

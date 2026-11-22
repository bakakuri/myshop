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

data class AddressForm(
    val label: String = "",
    val recipientName: String = "",
    val phone: String = "",
    val city: String = "",
    val street: String = "",
    val building: String = "",
    val apartment: String = "",
    val notes: String = "",
    val makeDefault: Boolean = false,
    val showErrors: Boolean = false,
) {
    val nameError get() = if (showErrors && recipientName.isBlank()) "სავალდებულოა" else null
    val phoneError get() = if (showErrors && phone.count { it.isDigit() } < 9) "შეიყვანეთ სწორი ნომერი" else null
    val cityError get() = if (showErrors && city.isBlank()) "სავალდებულოა" else null
    val streetError get() = if (showErrors && street.isBlank()) "სავალდებულოა" else null
    val isValid get() = recipientName.isNotBlank() && phone.count { it.isDigit() } >= 9 && city.isNotBlank() && street.isNotBlank()
}

data class AddressesUiState(
    val addresses: List<Address> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val form: AddressForm? = null,   // non-null = add sheet open
    val isSaving: Boolean = false,
)

@HiltViewModel
class AddressesViewModel @Inject constructor(
    private val addressRepository: AddressRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddressesUiState())
    val uiState: StateFlow<AddressesUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            addressRepository.getAddresses()
                .onSuccess { list -> _uiState.update { it.copy(addresses = list.sortedByDescending { a -> a.isDefault }, isLoading = false, error = null) } }
                .onFailure { _uiState.update { it.copy(isLoading = false, error = "მისამართების ჩატვირთვა ვერ მოხერხდა.") } }
        }
    }

    fun openForm() = _uiState.update { it.copy(form = AddressForm(makeDefault = it.addresses.isEmpty())) }
    fun closeForm() = _uiState.update { it.copy(form = null) }
    fun updateForm(transform: (AddressForm) -> AddressForm) = _uiState.update { s -> s.copy(form = s.form?.let(transform)) }

    fun save() {
        val f = _uiState.value.form ?: return
        if (!f.isValid) { updateForm { it.copy(showErrors = true) }; return }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val address = Address(
                userId = "", // set from the session in the repository
                label = f.label.trim().ifBlank { null },
                recipientName = f.recipientName.trim(),
                phone = f.phone.trim(),
                city = f.city.trim(),
                street = f.street.trim(),
                building = f.building.trim().ifBlank { null },
                apartment = f.apartment.trim().ifBlank { null },
                notes = f.notes.trim().ifBlank { null },
                isDefault = false,
            )
            addressRepository.addAddress(address)
                .onSuccess { created ->
                    if (f.makeDefault && created.id.isNotBlank()) addressRepository.setDefault(created.id)
                    _uiState.update { it.copy(isSaving = false, form = null) }
                    load()
                }
                .onFailure { _uiState.update { it.copy(isSaving = false, error = "მისამართის შენახვა ვერ მოხერხდა.") } }
        }
    }

    fun setDefault(id: String) {
        viewModelScope.launch { addressRepository.setDefault(id); load() }
    }

    fun deleteAddress(id: String) {
        _uiState.update { s -> s.copy(addresses = s.addresses.filterNot { it.id == id }) }
        viewModelScope.launch { addressRepository.deleteAddress(id); load() }
    }
}

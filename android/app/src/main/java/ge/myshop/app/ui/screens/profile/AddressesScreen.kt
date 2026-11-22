package ge.myshop.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.domain.model.Address
import ge.myshop.app.ui.components.EmptyState
import ge.myshop.app.ui.components.ErrorState
import ge.myshop.app.ui.components.IconTile
import ge.myshop.app.ui.components.LoadingState
import ge.myshop.app.ui.components.MsCard
import ge.myshop.app.ui.components.MsIconDisc
import ge.myshop.app.ui.components.MsPrimaryButton
import ge.myshop.app.ui.components.MsSoftPill
import ge.myshop.app.ui.components.MsTextField
import ge.myshop.app.ui.components.MyShopPageHeader
import ge.myshop.app.ui.components.msText
import ge.myshop.app.ui.theme.MsColor
import ge.myshop.app.ui.theme.MsShape

@Composable
fun AddressesScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddressesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var deleting by remember { mutableStateOf<Address?>(null) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { MyShopPageHeader("მისამართები", onBack = onNavigateBack, subtitle = "მიწოდების მისამართები") }
        when {
            state.isLoading -> item { LoadingState() }
            state.error != null && state.addresses.isEmpty() -> item { ErrorState(state.error!!, onRetry = viewModel::load) }
            state.addresses.isEmpty() -> item {
                EmptyState(Icons.Rounded.LocationOn, "მისამართი ჯერ არ გაქვს", subtitle = "დაამატე, რომ შეკვეთისას ავტომატურად შეივსოს")
            }
            else -> items(state.addresses, key = { it.id }) { a ->
                AddressCard(a, onDelete = { deleting = a }, onMakeDefault = { viewModel.setDefault(a.id) }, modifier = Modifier.padding(horizontal = 14.dp))
            }
        }
        item {
            MsPrimaryButton(
                "ახალი მისამართის დამატება",
                onClick = viewModel::openForm,
                icon = Icons.Rounded.Add,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            )
        }
    }

    state.form?.let { form -> AddressSheet(form, state.isSaving, viewModel::updateForm, viewModel::save, viewModel::closeForm) }

    deleting?.let { a ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            containerColor = Color.White,
            title = { Text("მისამართის წაშლა", style = msText(19.sp, FontWeight.ExtraBold)) },
            text = { Text("${a.addressLine}, ${a.city} — წავშალოთ?", style = msText(14.sp, FontWeight.Medium, MsColor.InkSoft)) },
            confirmButton = { TextButton(onClick = { viewModel.deleteAddress(a.id); deleting = null }) { Text("წაშლა", style = msText(15.sp, FontWeight.Bold, MsColor.Red)) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("გაუქმება", style = msText(15.sp, FontWeight.Bold, MsColor.Purple600)) } },
        )
    }
}

@Composable
private fun AddressCard(a: Address, onDelete: () -> Unit, onMakeDefault: () -> Unit, modifier: Modifier = Modifier) {
    MsCard(modifier.fillMaxWidth(), border = if (a.isDefault) MsColor.Purple300 else MsColor.CardBorder) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            IconTile(if (a.isDefault) Icons.Rounded.Home else Icons.Rounded.LocationOn)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(a.label ?: "მისამართი", style = msText(16.sp, FontWeight.ExtraBold), modifier = Modifier.weight(1f, fill = false))
                    if (a.isDefault) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "ძირითადი",
                            style = msText(11.sp, FontWeight.Bold, MsColor.Purple700),
                            modifier = Modifier
                                .clip(MsShape.Pill)
                                .background(MsColor.Purple100)
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                        )
                    }
                }
                Text("${a.recipientName} · ${a.phone}", style = msText(13.sp, FontWeight.SemiBold, MsColor.InkSoft))
                Text("${a.addressLine}, ${a.city}", style = msText(13.sp, FontWeight.Medium, MsColor.InkSoft))
                a.notes?.let { Text(it, style = msText(12.sp, FontWeight.Medium, MsColor.InkMuted)) }
                if (!a.isDefault) {
                    Spacer(Modifier.height(10.dp))
                    MsSoftPill("ძირითადად მონიშვნა", onMakeDefault, withArrow = false)
                }
            }
            MsIconDisc(Icons.Rounded.DeleteOutline, "წაშლა", onDelete, size = 36.dp, background = MsColor.Purple100)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddressSheet(
    form: AddressForm,
    saving: Boolean,
    update: ((AddressForm) -> AddressForm) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = MsShape.Sheet,
        containerColor = Color.White,
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("ახალი მისამართი", style = msText(22.sp, FontWeight.ExtraBold))
            MsTextField(form.label, { v -> update { it.copy(label = v.take(40)) } }, "დასახელება (მაგ. სახლი, სამსახური)", leadingIcon = Icons.Rounded.Bookmark)
            MsTextField(form.recipientName, { v -> update { it.copy(recipientName = v.take(120)) } }, "მიმღების სახელი და გვარი", leadingIcon = Icons.Rounded.Person, error = form.nameError)
            MsTextField(form.phone, { v -> update { it.copy(phone = v.filter { c -> c.isDigit() || c in "+ " }.take(20)) } }, "ტელეფონი", leadingIcon = Icons.Rounded.Phone, error = form.phoneError, keyboardType = KeyboardType.Phone)
            MsTextField(form.city, { v -> update { it.copy(city = v.take(60)) } }, "ქალაქი", leadingIcon = Icons.Rounded.LocationCity, error = form.cityError)
            MsTextField(form.street, { v -> update { it.copy(street = v.take(160)) } }, "ქუჩა", leadingIcon = Icons.Rounded.LocationOn, error = form.streetError)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MsTextField(form.building, { v -> update { it.copy(building = v.take(20)) } }, "კორპუსი/სახლი", Modifier.weight(1f), leadingIcon = Icons.Rounded.Apartment)
                MsTextField(form.apartment, { v -> update { it.copy(apartment = v.take(20)) } }, "ბინა", Modifier.weight(1f))
            }
            MsTextField(form.notes, { v -> update { it.copy(notes = v.take(300)) } }, "შენიშვნა კურიერისთვის", leadingIcon = Icons.Rounded.Notes, singleLine = false, minLines = 2)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("ძირითად მისამართად მონიშვნა", style = msText(15.sp, FontWeight.SemiBold), modifier = Modifier.weight(1f))
                Switch(
                    checked = form.makeDefault,
                    onCheckedChange = { v -> update { it.copy(makeDefault = v) } },
                    colors = SwitchDefaults.colors(checkedTrackColor = MsColor.Purple600, uncheckedTrackColor = MsColor.Purple100, uncheckedBorderColor = MsColor.Purple200, uncheckedThumbColor = MsColor.Purple300),
                )
            }
            Spacer(Modifier.height(4.dp))
            MsPrimaryButton("შენახვა", onSave, loading = saving, modifier = Modifier.fillMaxWidth())
        }
    }
}

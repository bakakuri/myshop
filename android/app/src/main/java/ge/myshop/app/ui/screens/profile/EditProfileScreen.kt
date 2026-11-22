package ge.myshop.app.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.ui.common.LocalApp
import ge.myshop.app.ui.components.MsCard
import ge.myshop.app.ui.components.MsPrimaryButton
import ge.myshop.app.ui.components.MsTextField
import ge.myshop.app.ui.components.MyShopPageHeader
import ge.myshop.app.ui.components.msText
import ge.myshop.app.ui.theme.MsColor

@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val app = LocalApp.current
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var prefilled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.profile) {
        val p = state.profile
        if (p != null && !prefilled) {
            name = p.fullName.orEmpty(); phone = p.phone.orEmpty(); prefilled = true
        }
    }
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) { app.showMessage("პროფილი შენახულია"); onNavigateBack() }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding()) {
        MyShopPageHeader("პროფილის რედაქტირება", onBack = onNavigateBack)
        MsCard(Modifier.padding(14.dp).fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("პირადი ინფორმაცია", style = msText(17.sp, FontWeight.ExtraBold))
                MsTextField(name, { name = it.take(120) }, "სახელი და გვარი", leadingIcon = Icons.Rounded.Person, error = state.error)
                MsTextField(phone, { phone = it.filter { c -> c.isDigit() || c in "+ " }.take(20) }, "ტელეფონი", leadingIcon = Icons.Rounded.Phone, keyboardType = KeyboardType.Phone)
                state.profile?.email?.let { email ->
                    MsTextField(email, {}, "ელ-ფოსტა", leadingIcon = Icons.Rounded.Email, enabled = false)
                    Text("ელ-ფოსტის შეცვლა აპიდან შეუძლებელია", style = msText(12.sp, FontWeight.Medium, MsColor.InkMuted), modifier = Modifier.padding(start = 18.dp))
                }
                MsPrimaryButton(
                    "შენახვა",
                    onClick = { viewModel.save(name.trim(), phone.trim()) },
                    icon = Icons.Rounded.Check,
                    loading = state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

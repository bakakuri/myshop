package ge.myshop.app.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.ui.components.MyShopButton
import ge.myshop.app.ui.components.MyShopTextField
import ge.myshop.app.ui.components.ScreenHeader

@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var name by rememberSaveable { mutableStateOf(uiState.profile?.fullName ?: "") }
    var phone by rememberSaveable { mutableStateOf(uiState.profile?.phone ?: "") }

    LaunchedEffect(uiState.profile) {
        uiState.profile?.let {
            if (name.isBlank()) name = it.fullName ?: ""
            if (phone.isBlank()) phone = it.phone ?: ""
        }
    }
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbar.showSnackbar("პროფილი განახლდა")
            onNavigateBack()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbar.showSnackbar(it) }
    }

    Scaffold(
        topBar = { ScreenHeader("პროფილის რედაქტირება", onBack = onNavigateBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            MyShopTextField(
                value = name,
                onValueChange = { name = it },
                label = "სახელი გვარი",
                leadingIcon = { Icon(Icons.Filled.Person, null) },
            )
            uiState.profile?.email?.let { email ->
                MyShopTextField(
                    value = email,
                    onValueChange = {},
                    label = "ელ-ფოსტა",
                    leadingIcon = { Icon(Icons.Filled.Email, null) },
                    enabled = false,
                )
            }
            MyShopTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "ტელეფონი (სურვილისამებრ)",
                leadingIcon = { Icon(Icons.Filled.Phone, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )
            MyShopButton(
                text = "შენახვა",
                onClick = { viewModel.save(name, phone) },
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

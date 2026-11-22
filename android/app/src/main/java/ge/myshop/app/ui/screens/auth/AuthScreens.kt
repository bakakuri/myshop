package ge.myshop.app.ui.screens.auth

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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.ui.components.MyShopButton
import ge.myshop.app.ui.components.MyShopOutlinedButton
import ge.myshop.app.ui.components.MyShopTextField
import ge.myshop.app.ui.components.ScreenHeader
import ge.myshop.app.ui.theme.LogoTextStyle
import ge.myshop.app.ui.theme.PrimaryPurple

// ============================================================
// Auth Screens: Login, Register, OTP Verify
// ============================================================

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToPhoneAuth: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoginSuccess()
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))
            Text(text = "MYSHOP", style = LogoTextStyle, color = PrimaryPurple)
            Text(text = "შესვლა", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            MyShopTextField(
                value = email,
                onValueChange = { email = it },
                label = "ელ-ფოსტა",
                leadingIcon = { Icon(Icons.Filled.Email, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            MyShopTextField(
                value = password,
                onValueChange = { password = it },
                label = "პაროლი",
                leadingIcon = { Icon(Icons.Filled.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            MyShopButton(
                text = "შესვლა",
                onClick = { viewModel.signIn(email, password) },
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
            MyShopOutlinedButton(
                text = "ტელეფონით შესვლა",
                onClick = onNavigateToPhoneAuth,
                leadingIcon = Icons.Filled.Phone,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onNavigateToRegister) {
                Text("ანგარიში არ გაქვს? რეგისტრაცია", color = PrimaryPurple)
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onRegisterSuccess()
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        topBar = { ScreenHeader("რეგისტრაცია", onBack = onNavigateBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            MyShopTextField(value = name, onValueChange = { name = it }, label = "სახელი გვარი",
                leadingIcon = { Icon(Icons.Filled.Person, null) })
            MyShopTextField(value = email, onValueChange = { email = it }, label = "ელ-ფოსტა",
                leadingIcon = { Icon(Icons.Filled.Email, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            MyShopTextField(value = password, onValueChange = { password = it }, label = "პაროლი",
                leadingIcon = { Icon(Icons.Filled.Lock, null) },
                trailingIcon = { IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null) } },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
            MyShopButton(text = "რეგისტრაცია", onClick = { viewModel.signUp(email, password, name) },
                isLoading = uiState.isLoading, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun OtpVerifyScreen(
    phone: String,
    onVerified: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var otp by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState.isLoggedIn) { if (uiState.isLoggedIn) onVerified() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        topBar = { ScreenHeader("კოდის შეყვანა", onBack = onNavigateBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            Text("კოდი გამოვაგზავნეთ ნომერზე\n$phone",
                style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            MyShopTextField(value = otp, onValueChange = { otp = it }, label = "SMS კოდი",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            MyShopButton(text = "დადასტურება", onClick = { viewModel.verifyOtp(phone, otp) },
                isLoading = uiState.isLoading, modifier = Modifier.fillMaxWidth())
        }
    }
}

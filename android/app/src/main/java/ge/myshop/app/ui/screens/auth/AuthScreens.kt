package ge.myshop.app.ui.screens.auth

import ge.myshop.app.ui.components.Art3D
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.R
import ge.myshop.app.ui.common.LocalApp
import ge.myshop.app.ui.components.EmptyState
import ge.myshop.app.ui.components.HeaderAction
import ge.myshop.app.ui.components.MsCard
import ge.myshop.app.ui.components.MsOutlinedButton
import ge.myshop.app.ui.components.MsPrimaryButton
import ge.myshop.app.ui.components.MsTextField
import ge.myshop.app.ui.components.MyShopHeader
import ge.myshop.app.ui.components.msText
import ge.myshop.app.ui.theme.MsColor
import ge.myshop.app.ui.theme.MsShape

@Composable
private fun AuthFrame(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding(),
    ) {
        MyShopHeader(leading = HeaderAction(Icons.AutoMirrored.Rounded.ArrowBack, "უკან", onClick = onBack))
        MsCard(Modifier.padding(horizontal = 14.dp).fillMaxWidth(), contentPadding = PaddingValues(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = msText(24.sp, FontWeight.ExtraBold))
                    Spacer(Modifier.height(4.dp))
                    Text(subtitle, style = msText(13.5.sp, FontWeight.Medium, MsColor.InkSoft, lineHeight = 19.sp))
                }
                Art3D(R.drawable.td_bags, Modifier.size(92.dp))
            }
            Spacer(Modifier.height(18.dp))
            content()
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PasswordToggle(visible: Boolean, onToggle: () -> Unit) {
    Box(
        Modifier.size(36.dp).clip(MsShape.Pill).clickable(role = Role.Button, onClickLabel = if (visible) "დამალვა" else "ჩვენება", onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        Icon(if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, if (visible) "პაროლის დამალვა" else "პაროლის ჩვენება", tint = MsColor.Purple600)
    }
}

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateBack: () -> Unit = onLoginSuccess,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val app = LocalApp.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var show by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) { app.refreshBadges(); app.showMessage("კეთილი იყოს შენი დაბრუნება!"); onLoginSuccess() }
    }

    AuthFrame("შესვლა", "შედი ანგარიშზე — კალათა, შეკვეთები და ჩათი ერთ ადგილას", onNavigateBack) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MsTextField(email, { email = it.trim(); viewModel.clearError() }, "ელ-ფოსტა", leadingIcon = Icons.Rounded.Email, keyboardType = KeyboardType.Email)
            MsTextField(
                password,
                { password = it; viewModel.clearError() },
                "პაროლი",
                leadingIcon = Icons.Rounded.Lock,
                isPassword = !show,
                keyboardType = KeyboardType.Password,
                trailing = { PasswordToggle(show) { show = !show } },
                error = state.error,
            )
            Spacer(Modifier.height(4.dp))
            MsPrimaryButton("შესვლა", onClick = { viewModel.signIn(email, password) }, loading = state.isLoading, modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("არ გაქვს ანგარიში? ", style = msText(14.sp, FontWeight.Medium, MsColor.InkSoft))
                Text(
                    "რეგისტრაცია",
                    style = msText(14.sp, FontWeight.ExtraBold, MsColor.Purple600),
                    modifier = Modifier.clip(MsShape.Pill).clickable(role = Role.Button, onClick = onNavigateToRegister).padding(6.dp),
                )
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onGoToLogin: () -> Unit = onNavigateBack,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val app = LocalApp.current
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var show by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) { app.refreshBadges(); onRegisterSuccess() }
    }

    val sentTo = state.confirmationSentTo
    if (sentTo != null) {
        AuthFrame("შეამოწმე მეილი", "ანგარიში შეიქმნა", onNavigateBack) {
            EmptyState(
                R.drawable.td_envelope,
                "დადასტურების ბმული გაიგზავნა",
                subtitle = "$sentTo — გახსენი წერილი, დააჭირე ბმულს და შემდეგ შედი ანგარიშზე.",
            )
            MsPrimaryButton("შესვლა", onClick = onGoToLogin, modifier = Modifier.fillMaxWidth())
        }
        return
    }

    AuthFrame("რეგისტრაცია", "შექმენი ანგარიში და შეუკვეთე მარტივად", onNavigateBack) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MsTextField(name, { name = it.take(120); viewModel.clearError() }, "სახელი და გვარი", leadingIcon = Icons.Rounded.Person)
            MsTextField(email, { email = it.trim(); viewModel.clearError() }, "ელ-ფოსტა", leadingIcon = Icons.Rounded.Email, keyboardType = KeyboardType.Email)
            MsTextField(
                password,
                { password = it; viewModel.clearError() },
                "პაროლი (მინ. 8 სიმბოლო)",
                leadingIcon = Icons.Rounded.Lock,
                isPassword = !show,
                keyboardType = KeyboardType.Password,
                trailing = { PasswordToggle(show) { show = !show } },
                error = state.error,
            )
            Spacer(Modifier.height(4.dp))
            MsPrimaryButton("რეგისტრაცია", onClick = { viewModel.signUp(email, password, name) }, loading = state.isLoading, modifier = Modifier.fillMaxWidth())
            MsOutlinedButton("უკვე მაქვს ანგარიში", onClick = onGoToLogin, modifier = Modifier.fillMaxWidth())
            Text(
                "რეგისტრაციით ეთანხმები მაღაზიის წესებს. აპში ბარათით გადახდა არ ხდება.",
                style = msText(12.sp, FontWeight.Medium, MsColor.InkMuted),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

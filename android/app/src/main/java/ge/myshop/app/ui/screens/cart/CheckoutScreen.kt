package ge.myshop.app.ui.screens.cart

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.ui.components.MyShopButton
import ge.myshop.app.ui.components.MyShopTextField
import ge.myshop.app.ui.components.ScreenHeader

@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onOrderPlaced: (orderId: String) -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.event) {
        when (val event = uiState.event) {
            is CheckoutEvent.OrderPlaced -> {
                viewModel.clearEvent()
                onOrderPlaced(event.orderId)
            }
            is CheckoutEvent.ShowError -> {
                snackbarHostState.showSnackbar(event.message)
                viewModel.clearEvent()
            }
            null -> {}
        }
    }

    Scaffold(
        topBar = {
            ScreenHeader(title = "შეკვეთის გაფორმება", onBack = onNavigateBack)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Order total
                    uiState.cartSummary?.let { summary ->
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("სულ:", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "%.2f ₾".format(summary.subtotal),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "⚠ გადახდა შეთანხმდება ჩატში",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    MyShopButton(
                        text = "შეკვეთის განთავსება",
                        onClick = viewModel::placeOrder,
                        isLoading = uiState.isPlacing,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // Customer Info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "მიმღების მონაცემები",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(12.dp))

                    MyShopTextField(
                        value = uiState.customerName,
                        onValueChange = viewModel::updateName,
                        label = "სახელი გვარი *",
                        isError = uiState.nameError != null,
                        errorMessage = uiState.nameError,
                    )
                    Spacer(Modifier.height(10.dp))

                    MyShopTextField(
                        value = uiState.customerPhone,
                        onValueChange = viewModel::updatePhone,
                        label = "ტელეფონი *",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = uiState.phoneError != null,
                        errorMessage = uiState.phoneError,
                    )
                    Spacer(Modifier.height(10.dp))

                    MyShopTextField(
                        value = uiState.customerEmail,
                        onValueChange = viewModel::updateEmail,
                        label = "ელ-ფოსტა (არასავალდებულო)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                }
            }

            // Delivery Info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "მიწოდების ინფორმაცია",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(12.dp))

                    MyShopTextField(
                        value = uiState.city,
                        onValueChange = viewModel::updateCity,
                        label = "ქალაქი",
                    )
                    Spacer(Modifier.height(10.dp))

                    MyShopTextField(
                        value = uiState.address,
                        onValueChange = viewModel::updateAddress,
                        label = "მისამართი",
                        maxLines = 3,
                        singleLine = false,
                    )
                    Spacer(Modifier.height(10.dp))

                    MyShopTextField(
                        value = uiState.deliveryNotes,
                        onValueChange = viewModel::updateDeliveryNotes,
                        label = "დამატებითი ინსტრუქცია (არ. სავ.)",
                        maxLines = 2,
                        singleLine = false,
                    )
                }
            }

            // Note to store
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "შენიშვნა მაღაზიისთვის",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(12.dp))
                    MyShopTextField(
                        value = uiState.customerNote,
                        onValueChange = viewModel::updateNote,
                        label = "შენიშვნა (არ. სავ.)",
                        maxLines = 4,
                        singleLine = false,
                    )
                }
            }

            // Payment notice
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Text(
                    text = "💬 გადახდის პირობები შეთანხმდება ჩატის საშუალებით. ონლაინ გადახდა არ ხდება.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp),
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

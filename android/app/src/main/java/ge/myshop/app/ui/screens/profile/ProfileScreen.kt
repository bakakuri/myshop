package ge.myshop.app.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.ui.components.MyShopButton
import ge.myshop.app.ui.components.MyShopOutlinedButton
import ge.myshop.app.ui.components.ScreenHeader
import ge.myshop.app.ui.theme.PrimaryPurple

@Composable
fun ProfileScreen(
    onOrdersClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onAddressesClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { ScreenHeader("პროფილი") }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            if (uiState.profile == null) {
                // Guest state
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Surface(Modifier.size(80.dp).clip(CircleShape), color = Color(0xFFF3F0FF)) {
                        Icon(Icons.Filled.Person, null, tint = PrimaryPurple, modifier = Modifier.padding(20.dp))
                    }
                    Text("სტუმარი", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("შედი ანგარიშზე შეკვეთების სანახავად", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    MyShopButton(text = "შესვლა / რეგისტრაცია", onClick = onLoginClick, modifier = Modifier.fillMaxWidth())
                }
            } else {
                val profile = uiState.profile!!
                // Profile header
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(Modifier.size(80.dp).clip(CircleShape), color = PrimaryPurple) {
                        Text(
                            text = profile.fullName.firstOrNull()?.uppercase() ?: "U",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(20.dp),
                        )
                    }
                    Text(profile.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(profile.email ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    MyShopOutlinedButton(text = "პროფილის რედაქტირება", onClick = onEditProfileClick, modifier = Modifier.fillMaxWidth())
                }

                // Menu items
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    ProfileMenuItem(icon = Icons.Filled.ShoppingBag, label = "შეკვეთები", onClick = onOrdersClick)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuItem(icon = Icons.Filled.Favorite, label = "სასურველები", onClick = onFavoritesClick)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuItem(icon = Icons.Filled.LocationOn, label = "მისამართები", onClick = onAddressesClick)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuItem(icon = Icons.Filled.Notifications, label = "შეტყობინებები", onClick = onNotificationsClick)
                }

                Spacer(Modifier.height(16.dp))

                // Logout
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    ProfileMenuItem(
                        icon = Icons.Filled.Logout,
                        label = "გამოსვლა",
                        onClick = viewModel::signOut,
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = tint, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
    }
}

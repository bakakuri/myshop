package ge.myshop.app.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ge.myshop.app.domain.model.Address
import ge.myshop.app.ui.components.EmptyState
import ge.myshop.app.ui.components.ScreenHeader
import ge.myshop.app.ui.theme.PrimaryPurple

@Composable
fun AddressesScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddressesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { ScreenHeader("მისამართები", onBack = onNavigateBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showAddDialog, containerColor = PrimaryPurple) {
                Icon(Icons.Filled.Add, contentDescription = "დამატება")
            }
        },
    ) { padding ->
        if (uiState.addresses.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(icon = "📍", title = "მისამართი არ გაქვს", subtitle = "დაამატეთ მიწოდების მისამართი")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(uiState.addresses, key = { it.id }) { address ->
                    AddressCard(address = address, onDelete = { viewModel.deleteAddress(address.id) })
                }
            }
        }
    }
}

@Composable
private fun AddressCard(address: Address, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.LocationOn, null, tint = PrimaryPurple)
            Column(Modifier.weight(1f)) {
                address.label?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                Text("${address.city}, ${address.addressLine}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "წაშლა", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

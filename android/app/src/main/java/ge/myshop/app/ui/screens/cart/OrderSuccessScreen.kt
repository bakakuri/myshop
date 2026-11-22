package ge.myshop.app.ui.screens.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ge.myshop.app.ui.components.MyShopButton
import ge.myshop.app.ui.components.MyShopOutlinedButton
import ge.myshop.app.ui.theme.SuccessGreen

@Composable
fun OrderSuccessScreen(
    orderId: String,
    onGoHome: () -> Unit,
    onGoToChat: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier.size(96.dp),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "შეკვეთა მიღებულია!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "თქვენი შეკვეთა წარმატებით განთავსდა.\nჩვენ გიკავშირდებით ჩატის საშუალებით\nგადახდისა და მიწოდების პირობების შეთანხმებისთვის.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(40.dp))
            MyShopButton(
                text = "ჩატში გადასვლა",
                onClick = onGoToChat,
                leadingIcon = Icons.Filled.Chat,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            MyShopOutlinedButton(
                text = "მთავარ გვერდზე დაბრუნება",
                onClick = onGoHome,
                leadingIcon = Icons.Filled.Home,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

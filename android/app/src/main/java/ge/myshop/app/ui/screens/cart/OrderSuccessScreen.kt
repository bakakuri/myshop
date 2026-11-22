package ge.myshop.app.ui.screens.cart

import ge.myshop.app.ui.components.Art3D
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Textsms
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.myshop.app.R
import ge.myshop.app.data.repository.OrderRepository
import ge.myshop.app.domain.model.Order
import ge.myshop.app.ui.common.LocalApp
import ge.myshop.app.ui.components.HeaderAction
import ge.myshop.app.ui.components.MsCard
import ge.myshop.app.ui.components.MsOutlinedButton
import ge.myshop.app.ui.components.MsPrimaryButton
import ge.myshop.app.ui.components.MyShopHeader
import ge.myshop.app.ui.components.formatGel
import ge.myshop.app.ui.components.msText
import ge.myshop.app.ui.theme.MsColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderSuccessViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    orderRepository: OrderRepository,
) : ViewModel() {
    private val _order = MutableStateFlow<Order?>(null)
    val order: StateFlow<Order?> = _order

    init {
        val id = savedStateHandle.get<String>("orderId").orEmpty()
        if (id.isNotBlank()) viewModelScope.launch { _order.value = orderRepository.getOrder(id).getOrNull() }
    }
}

@Composable
fun OrderSuccessScreen(
    orderId: String,
    onGoHome: () -> Unit,
    onGoToChat: (String?) -> Unit,
    onViewOrder: (String) -> Unit = {},
    viewModel: OrderSuccessViewModel = hiltViewModel(),
) {
    val order by viewModel.order.collectAsState()
    val store = LocalApp.current.store

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        MyShopHeader(actions = listOf(HeaderAction(Icons.Rounded.Home, "მთავარი", onClick = onGoHome)))
        Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Success card (reference 6, bottom)
            MsCard(
                Modifier.fillMaxWidth(),
                background = androidx.compose.ui.graphics.SolidColor(MsColor.GreenSoft),
                border = MsColor.GreenBorder,
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(52.dp).clip(CircleShape).background(MsColor.Green), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("შეკვეთის მოთხოვნა გაიგზავნა!", style = msText(17.sp, FontWeight.ExtraBold, MsColor.Green))
                        Spacer(Modifier.height(2.dp))
                        Text(
                            store.orderConfirmation ?: "ადმინისტრატორი მალე დაგიკავშირდება ჩათში დეტალების დასაზუსტებლად.",
                            style = msText(13.sp, FontWeight.Medium, MsColor.InkSoft, lineHeight = 18.sp),
                        )
                    }
                }
            }

            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Art3D(R.drawable.td_package, Modifier.size(110.dp))
                    Art3D(R.drawable.td_party, Modifier.size(140.dp))
                }
            }

            order?.let { o ->
                MsCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row {
                            Text("შეკვეთის №", style = msText(14.sp, FontWeight.Medium, MsColor.InkSoft), modifier = Modifier.weight(1f))
                            Text(o.orderNumber, style = msText(15.sp, FontWeight.ExtraBold))
                        }
                        Row {
                            Text("პროდუქტები", style = msText(14.sp, FontWeight.Medium, MsColor.InkSoft), modifier = Modifier.weight(1f))
                            Text("${o.items.sumOf { it.quantity }} ც", style = msText(15.sp, FontWeight.Bold))
                        }
                        Row {
                            Text("ჯამი", style = msText(14.sp, FontWeight.Medium, MsColor.InkSoft), modifier = Modifier.weight(1f))
                            Text(formatGel(o.total), style = msText(18.sp, FontWeight.ExtraBold, MsColor.Purple800))
                        }
                    }
                }
            }

            MsPrimaryButton(
                "გახსენი ჩათი ადმინისტრატორთან",
                onClick = { onGoToChat(order?.conversationId) },
                icon = Icons.Rounded.Textsms,
                modifier = Modifier.fillMaxWidth(),
            )
            MsOutlinedButton(
                "შეკვეთის დეტალები",
                onClick = { onViewOrder(orderId) },
                icon = Icons.Rounded.Receipt,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "სტატუსის ცვლილებას შეტყობინებებში და ჩათში ნახავ.",
                style = msText(12.5.sp, FontWeight.Medium, MsColor.InkMuted),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            )
        }
    }
}

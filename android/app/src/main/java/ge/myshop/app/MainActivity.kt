package ge.myshop.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ge.myshop.app.ui.components.MyShopBottomNavigation
import ge.myshop.app.ui.navigation.NavGraph
import ge.myshop.app.ui.navigation.Screen
import ge.myshop.app.ui.theme.MyShopTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyShopTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MyShopApp()
                }
            }
        }
    }
}

@Composable
fun MyShopApp(viewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            MyShopBottomNavigation(
                navController = navController,
                cartCount = uiState.cartCount,
                chatCount = uiState.unreadChatCount,
            )
        },
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding).fillMaxSize()) {
            NavGraph(
                navController = navController,
                startDestination = Screen.Home.route,
            )
        }
    }
}

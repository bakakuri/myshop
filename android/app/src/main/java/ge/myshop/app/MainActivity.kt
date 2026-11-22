package ge.myshop.app

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ge.myshop.app.ui.common.AppController
import ge.myshop.app.ui.common.LocalApp
import ge.myshop.app.ui.components.MainTab
import ge.myshop.app.ui.components.MsBottomNav
import ge.myshop.app.ui.components.msText
import ge.myshop.app.ui.navigation.NavGraph
import ge.myshop.app.ui.navigation.Screen
import ge.myshop.app.ui.theme.MsColor
import ge.myshop.app.ui.theme.MsShape
import ge.myshop.app.ui.theme.MyShopTheme
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Purple header runs under the status bar → light status-bar icons
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
        )
        setContent {
            MyShopTheme { MyShopApp() }
        }
    }
}

/** Which bottom tab a route belongs to; null = no bottom bar (detail, search, auth). */
private fun tabFor(route: String?): MainTab? = when {
    route == null -> null
    route == Screen.Home.route -> MainTab.Home
    route == Screen.Categories.route || route.startsWith("products") -> MainTab.Categories
    route == Screen.Cart.route || route == Screen.Checkout.route || route.startsWith("order-success") -> MainTab.Cart
    route == Screen.Chats.route -> MainTab.Chat // conversation screen has its own input bar, no nav
    route == Screen.Profile.route || route.startsWith("orders") || route in setOf(
        Screen.Favorites.route, Screen.Addresses.route, Screen.Notifications.route, Screen.EditProfile.route,
    ) -> MainTab.Profile
    else -> null
}

private fun routeFor(tab: MainTab): String = when (tab) {
    MainTab.Home -> Screen.Home.route
    MainTab.Categories -> Screen.Categories.route
    MainTab.Cart -> Screen.Cart.route
    MainTab.Chat -> Screen.Chats.route
    MainTab.Profile -> Screen.Profile.route
}

fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun MyShopApp(viewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val badges by viewModel.badges.collectAsState()
    val store by viewModel.store.collectAsState()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val tab = tabFor(route)
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Keep badges fresh as the user moves around
    LaunchedEffect(route) { viewModel.refreshBadges() }

    val controller = AppController(
        badges = badges,
        store = store,
        refreshBadges = viewModel::refreshBadges,
        showMessage = { msg ->
            scope.launch {
                snackbar.currentSnackbarData?.dismiss()
                snackbar.showSnackbar(msg)
            }
        },
        openLogin = { navController.navigate(Screen.Login.route) { launchSingleTop = true } },
    )

    CompositionLocalProvider(LocalApp provides controller) {
        Scaffold(
            containerColor = MsColor.Background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {
                SnackbarHost(snackbar) { data ->
                    Snackbar(
                        shape = MsShape.Card,
                        containerColor = MsColor.Purple800,
                        contentColor = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    ) { Text(data.visuals.message, style = msText(14.sp, FontWeight.SemiBold, Color.White)) }
                }
            },
            bottomBar = {
                if (tab != null) {
                    MsBottomNav(
                        selected = tab,
                        cartCount = badges.cart,
                        chatCount = badges.chat,
                        onSelect = { navController.navigateToTab(routeFor(it)) },
                    )
                }
            },
        ) { inner ->
            Box(Modifier.fillMaxSize().padding(inner)) {
                NavGraph(navController = navController, startDestination = Screen.Home.route)
            }
        }
    }
}

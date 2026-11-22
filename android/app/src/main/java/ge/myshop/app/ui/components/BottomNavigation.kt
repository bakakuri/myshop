package ge.myshop.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import ge.myshop.app.ui.navigation.Screen
import ge.myshop.app.ui.navigation.bottomNavRoutes
import ge.myshop.app.ui.theme.PrimaryPurple

data class BottomNavItemData(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badge: Int = 0,
)

private val navItems = listOf(
    BottomNavItemData(Screen.Home.route, "მთავარი", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItemData(Screen.Categories.route, "კატეგ.", Icons.Filled.GridView, Icons.Outlined.GridView),
    BottomNavItemData(Screen.Cart.route, "კალათა", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    BottomNavItemData(Screen.Chats.route, "ჩატი", Icons.Filled.Chat, Icons.Outlined.Chat),
    BottomNavItemData(Screen.Profile.route, "პროფ.", Icons.Filled.Person, Icons.Outlined.Person),
)

@Composable
fun MyShopBottomNavigation(
    navController: NavHostController,
    cartCount: Int = 0,
    chatCount: Int = 0,
) {
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    // Only show bottom nav on top-level tab routes
    if (currentRoute !in bottomNavRoutes) return

    NavigationBar(containerColor = Color.White, tonalElevation = 8f.dp) {
        navItems.forEach { item ->
            val badge = when (item.route) {
                Screen.Cart.route -> cartCount
                Screen.Chats.route -> chatCount
                else -> 0
            }
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    BadgedBox(badge = {
                        if (badge > 0) {
                            Badge { Text(if (badge > 99) "99+" else badge.toString(), color = Color.White, fontSize = 9.sp) }
                        }
                    }) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                        )
                    }
                },
                label = { Text(item.label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryPurple,
                    selectedTextColor = PrimaryPurple,
                    indicatorColor = PrimaryPurple.copy(alpha = 0.1f),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                ),
            )
        }
    }
}

// Extension to allow dp on Float inside composable
private val Float.dp get() = androidx.compose.ui.unit.Dp(this)

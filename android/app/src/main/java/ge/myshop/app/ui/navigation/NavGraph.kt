package ge.myshop.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ge.myshop.app.ui.screens.auth.LoginScreen
import ge.myshop.app.ui.screens.auth.OtpVerifyScreen
import ge.myshop.app.ui.screens.auth.RegisterScreen
import ge.myshop.app.ui.screens.cart.CartScreen
import ge.myshop.app.ui.screens.cart.CheckoutScreen
import ge.myshop.app.ui.screens.cart.OrderSuccessScreen
import ge.myshop.app.ui.screens.catalog.CategoriesScreen
import ge.myshop.app.ui.screens.catalog.ProductDetailScreen
import ge.myshop.app.ui.screens.catalog.ProductListScreen
import ge.myshop.app.ui.screens.chat.ChatDetailScreen
import ge.myshop.app.ui.screens.chat.ChatsScreen
import ge.myshop.app.ui.screens.home.HomeScreen
import ge.myshop.app.ui.screens.orders.OrderDetailScreen
import ge.myshop.app.ui.screens.orders.OrderHistoryScreen
import ge.myshop.app.ui.screens.profile.AddressesScreen
import ge.myshop.app.ui.screens.profile.EditProfileScreen
import ge.myshop.app.ui.screens.profile.FavoritesScreen
import ge.myshop.app.ui.screens.profile.NotificationsScreen
import ge.myshop.app.ui.screens.profile.ProfileScreen
import ge.myshop.app.ui.screens.search.SearchScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route,
) {
    NavHost(navController = navController, startDestination = startDestination) {

        // ── Home ──────────────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                onNavigateToProductDetail = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                onNavigateToProductList = { id, title -> navController.navigate(Screen.ProductList.createRoute(id, title)) },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
            )
        }

        // ── Categories ────────────────────────────────────────
        composable(Screen.Categories.route) {
            CategoriesScreen(
                onCategoryClick = { id, name -> navController.navigate(Screen.ProductList.createRoute(id, name)) },
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
            )
        }

        // ── Product List ──────────────────────────────────────
        composable(
            route = Screen.ProductList.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("categoryName") { type = NavType.StringType; nullable = true; defaultValue = null },
            ),
        ) { backStack ->
            val categoryId = backStack.arguments?.getString("categoryId")
            val categoryName = backStack.arguments?.getString("categoryName")
            ProductListScreen(
                categoryId = categoryId,
                categoryName = categoryName,
                onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Product Detail ────────────────────────────────────
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
        ) { backStack ->
            ProductDetailScreen(
                productId = backStack.arguments?.getString("productId") ?: "",
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToProductDetail = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
            )
        }

        // ── Search ────────────────────────────────────────────
        composable(Screen.Search.route) {
            SearchScreen(
                onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Cart ──────────────────────────────────────────────
        composable(Screen.Cart.route) {
            CartScreen(
                onCheckoutClick = { navController.navigate(Screen.Checkout.route) },
                onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Checkout ──────────────────────────────────────────
        composable(Screen.Checkout.route) {
            CheckoutScreen(
                onNavigateBack = { navController.popBackStack() },
                onOrderPlaced = { orderId ->
                    navController.navigate(Screen.OrderSuccess.createRoute(orderId)) {
                        popUpTo(Screen.Cart.route) { inclusive = true }
                    }
                },
            )
        }

        // ── Order Success ─────────────────────────────────────
        composable(
            route = Screen.OrderSuccess.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) { backStack ->
            val orderId = backStack.arguments?.getString("orderId") ?: ""
            OrderSuccessScreen(
                orderId = orderId,
                onGoHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onGoToChat = { navController.navigate(Screen.Chats.route) },
            )
        }

        // ── Chats ─────────────────────────────────────────────
        composable(Screen.Chats.route) {
            ChatsScreen(
                onChatClick = { conversationId -> navController.navigate(Screen.ChatDetail.createRoute(conversationId)) },
            )
        }

        // ── Chat Detail ───────────────────────────────────────
        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType }),
        ) { backStack ->
            ChatDetailScreen(
                conversationId = backStack.arguments?.getString("conversationId") ?: "",
                onNavigateBack = { navController.popBackStack() },
                onNavigateToOrderDetail = { id -> navController.navigate(Screen.OrderDetail.createRoute(id)) },
            )
        }

        // ── Profile ───────────────────────────────────────────
        composable(Screen.Profile.route) {
            ProfileScreen(
                onOrdersClick = { navController.navigate(Screen.OrderHistory.route) },
                onFavoritesClick = { navController.navigate(Screen.Favorites.route) },
                onAddressesClick = { navController.navigate(Screen.Addresses.route) },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                onLoginClick = { navController.navigate(Screen.Login.route) },
            )
        }

        // ── Auth ──────────────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToPhoneAuth = { navController.navigate(Screen.OtpVerify.createRoute("")) },
                onLoginSuccess = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Screen.OtpVerify.route,
            arguments = listOf(navArgument("phone") { type = NavType.StringType; defaultValue = "" }),
        ) { backStack ->
            val phone = backStack.arguments?.getString("phone") ?: ""
            OtpVerifyScreen(
                phone = phone,
                onVerified = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Order History & Detail ────────────────────────────
        composable(Screen.OrderHistory.route) {
            OrderHistoryScreen(
                onOrderClick = { id -> navController.navigate(Screen.OrderDetail.createRoute(id)) },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) {
            OrderDetailScreen(
                orderId = it.arguments?.getString("orderId") ?: "",
                onChatClick = { convId -> navController.navigate(Screen.ChatDetail.createRoute(convId)) },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Favorites, Addresses, Notifications, Edit Profile ─
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Addresses.route) {
            AddressesScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

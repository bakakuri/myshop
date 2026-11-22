package ge.myshop.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ge.myshop.app.navigateToTab
import ge.myshop.app.ui.screens.auth.LoginScreen
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
fun NavGraph(navController: NavHostController, startDestination: String) {
    val toProduct: (String) -> Unit = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) }
    val toList: (String?, String?) -> Unit = { id, name -> navController.navigate(Screen.ProductList.createRoute(id, name)) }
    val toChat: (String) -> Unit = { id -> navController.navigate(Screen.ChatDetail.createRoute(id)) }
    val toOrder: (String) -> Unit = { id -> navController.navigate(Screen.OrderDetail.createRoute(id)) }
    val back: () -> Unit = { if (!navController.popBackStack()) navController.navigateToTab(Screen.Home.route) }
    val toRoute: (String) -> Unit = { route ->
        if (route in bottomNavRoutes) navController.navigateToTab(route) else navController.navigate(route)
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(tween(220)) },
        exitTransition = { fadeOut(tween(160)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { fadeOut(tween(160)) },
    ) {
        // ── Tabs
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCategories = { navController.navigateToTab(Screen.Categories.route) },
                onNavigateToProductDetail = toProduct,
                onNavigateToProductList = toList,
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToCart = { navController.navigateToTab(Screen.Cart.route) },
                onNavigateToRoute = toRoute,
            )
        }
        composable(Screen.Categories.route) {
            CategoriesScreen(
                onNavigateBack = back,
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToProductList = toList,
            )
        }
        composable(Screen.Cart.route) {
            CartScreen(
                onNavigateToCheckout = { navController.navigate(Screen.Checkout.route) },
                onNavigateToProductDetail = toProduct,
                onNavigateToHome = { navController.navigateToTab(Screen.Home.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToRoute = toRoute,
            )
        }
        composable(Screen.Chats.route) {
            ChatsScreen(onNavigateToChat = toChat)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onOrdersClick = { navController.navigate(Screen.OrderHistory.route) },
                onFavoritesClick = { navController.navigate(Screen.Favorites.route) },
                onAddressesClick = { navController.navigate(Screen.Addresses.route) },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onRegisterClick = { navController.navigate(Screen.Register.route) },
                onChatClick = { navController.navigateToTab(Screen.Chats.route) },
                onOrderClick = toOrder,
            )
        }

        // ── Catalog
        composable(
            route = Screen.ProductList.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType; defaultValue = "" },
                navArgument("categoryName") { type = NavType.StringType; defaultValue = "" },
            ),
        ) {
            ProductListScreen(
                onNavigateBack = back,
                onNavigateToProductDetail = toProduct,
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToCategories = { navController.navigateToTab(Screen.Categories.route) },
            )
        }
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
        ) { entry ->
            ProductDetailScreen(
                productId = entry.arguments?.getString("productId").orEmpty(),
                onNavigateBack = back,
                onNavigateToCart = { navController.navigateToTab(Screen.Cart.route) },
                onNavigateToProductDetail = toProduct,
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(
                onProductClick = toProduct,
                onNavigateBack = back,
                onCategoryClick = { id, name -> toList(id, name) },
            )
        }

        // ── Checkout
        composable(Screen.Checkout.route) {
            CheckoutScreen(
                onNavigateBack = back,
                onOrderPlaced = { orderId ->
                    navController.navigate(Screen.OrderSuccess.createRoute(orderId)) {
                        popUpTo(Screen.Cart.route) { inclusive = false }
                    }
                },
                onNavigateHome = { navController.navigateToTab(Screen.Home.route) },
            )
        }
        composable(
            route = Screen.OrderSuccess.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) { entry ->
            val orderId = entry.arguments?.getString("orderId").orEmpty()
            OrderSuccessScreen(
                orderId = orderId,
                onGoHome = { navController.navigateToTab(Screen.Home.route) },
                onGoToChat = { convId -> if (convId != null) toChat(convId) else navController.navigateToTab(Screen.Chats.route) },
                onViewOrder = toOrder,
            )
        }

        // ── Chat
        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType }),
        ) { entry ->
            ChatDetailScreen(
                conversationId = entry.arguments?.getString("conversationId").orEmpty(),
                onNavigateBack = back,
                onNavigateToOrderDetail = toOrder,
            )
        }

        // ── Auth
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) { launchSingleTop = true } },
                onLoginSuccess = back,
                onNavigateBack = back,
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = back,
                onRegisterSuccess = { navController.popBackStack(Screen.Login.route, inclusive = true); back() },
                onGoToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        // ── Profile sub-screens
        composable(Screen.OrderHistory.route) {
            OrderHistoryScreen(
                onNavigateBack = back,
                onOrderClick = toOrder,
                onStartShopping = { navController.navigateToTab(Screen.Home.route) },
            )
        }
        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) { entry ->
            OrderDetailScreen(
                orderId = entry.arguments?.getString("orderId").orEmpty(),
                onChatClick = toChat,
                onNavigateBack = back,
            )
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onProductClick = toProduct,
                onNavigateBack = back,
                onBrowse = { navController.navigateToTab(Screen.Home.route) },
            )
        }
        composable(Screen.Addresses.route) { AddressesScreen(onNavigateBack = back) }
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateBack = back,
                onOpenOrder = toOrder,
                onOpenChat = toChat,
                onOpenProduct = toProduct,
            )
        }
        composable(Screen.EditProfile.route) { EditProfileScreen(onNavigateBack = back) }
    }
}

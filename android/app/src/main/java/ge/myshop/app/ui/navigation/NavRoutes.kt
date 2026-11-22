package ge.myshop.app.ui.navigation

import java.net.URLEncoder

sealed class Screen(val route: String) {
    // Bottom nav tabs
    object Home : Screen("home")
    object Categories : Screen("categories")
    object Cart : Screen("cart")
    object Chats : Screen("chats")
    object Profile : Screen("profile")

    // Catalog
    object ProductList : Screen("products?categoryId={categoryId}&categoryName={categoryName}") {
        fun createRoute(categoryId: String?, name: String?) =
            "products?categoryId=${categoryId ?: ""}&categoryName=${name?.let { URLEncoder.encode(it, "UTF-8") } ?: ""}"
    }
    object ProductDetail : Screen("product/{productId}") {
        fun createRoute(id: String) = "product/$id"
    }
    object Search : Screen("search")

    // Cart & Orders
    object Checkout : Screen("checkout")
    object OrderSuccess : Screen("order-success/{orderId}") {
        fun createRoute(id: String) = "order-success/$id"
    }

    // Chat
    object ChatDetail : Screen("chat/{conversationId}") {
        fun createRoute(id: String) = "chat/$id"
    }

    // Auth
    object Login : Screen("login")
    object Register : Screen("register")
    object OtpVerify : Screen("otp/{phone}") {
        fun createRoute(phone: String) = "otp/${URLEncoder.encode(phone, "UTF-8")}"
    }

    // Profile sub-screens
    object OrderHistory : Screen("orders")
    object OrderDetail : Screen("orders/{orderId}") {
        fun createRoute(id: String) = "orders/$id"
    }
    object Favorites : Screen("favorites")
    object Addresses : Screen("addresses")
    object Notifications : Screen("notifications")
    object EditProfile : Screen("edit-profile")
}

val bottomNavRoutes = setOf(
    Screen.Home.route,
    Screen.Categories.route,
    Screen.Cart.route,
    Screen.Chats.route,
    Screen.Profile.route,
)

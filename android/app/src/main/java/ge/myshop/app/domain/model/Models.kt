package ge.myshop.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============================================================
// MYSHOP Domain Models — mirrors Supabase schema
// All @Serializable for Supabase Kotlin client (kotlinx.serialization)
// ============================================================

// ── Enums ────────────────────────────────────────────────────

@Serializable
enum class OrderStatus {
    @SerialName("new") NEW,
    @SerialName("awaiting_agreement") AWAITING_AGREEMENT,
    @SerialName("terms_agreed") TERMS_AGREED,
    @SerialName("preparing") PREPARING,
    @SerialName("shipped") SHIPPED,
    @SerialName("delivered") DELIVERED,
    @SerialName("cancelled") CANCELLED;

    fun labelKa(): String = when (this) {
        NEW -> "ახალი"
        AWAITING_AGREEMENT -> "შეთანხმების მოლოდინში"
        TERMS_AGREED -> "პირობები შეთანხმებულია"
        PREPARING -> "მზადდება"
        SHIPPED -> "გაგზავნილია"
        DELIVERED -> "მიწოდებულია"
        CANCELLED -> "გაუქმებული"
    }
}

@Serializable
enum class MessageSender {
    @SerialName("customer") CUSTOMER,
    @SerialName("staff") STAFF,
    @SerialName("system") SYSTEM,
}

@Serializable
enum class NotificationType {
    @SerialName("order_status") ORDER_STATUS,
    @SerialName("message") MESSAGE,
    @SerialName("promotion") PROMOTION,
    @SerialName("system") SYSTEM,
}

@Serializable
enum class BannerType {
    @SerialName("hero") HERO,
    @SerialName("promo") PROMO,
    @SerialName("category") CATEGORY,
}

@Serializable
enum class HomeSectionType {
    @SerialName("hero_banners") HERO_BANNERS,
    @SerialName("category_shortcuts") CATEGORY_SHORTCUTS,
    @SerialName("promo_cards") PROMO_CARDS,
    @SerialName("popular_products") POPULAR_PRODUCTS,
    @SerialName("discounted_products") DISCOUNTED_PRODUCTS,
    @SerialName("new_products") NEW_PRODUCTS,
}

// ── User / Profile ────────────────────────────────────────────

@Serializable
data class UserProfile(
    val id: String,
    @SerialName("full_name") val fullName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

// ── Category ──────────────────────────────────────────────────

@Serializable
data class Category(
    val id: String,
    @SerialName("parent_id") val parentId: String? = null,
    @SerialName("name_ka") val nameKa: String,
    @SerialName("slug") val slug: String,
    @SerialName("icon_url") val iconUrl: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true,
    val children: List<Category> = emptyList(), // populated client-side
)

// ── Product ───────────────────────────────────────────────────

@Serializable
data class ProductImage(
    val id: String,
    @SerialName("product_id") val productId: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_primary") val isPrimary: Boolean = false,
    @SerialName("alt_text_ka") val altTextKa: String? = null,
)

@Serializable
data class ProductAttribute(
    val id: String,
    @SerialName("product_id") val productId: String,
    @SerialName("attribute_name_ka") val attributeNameKa: String,
    @SerialName("attribute_value_ka") val attributeValueKa: String,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

@Serializable
data class ProductVariant(
    val id: String,
    @SerialName("product_id") val productId: String,
    @SerialName("variant_name_ka") val variantNameKa: String,
    val sku: String? = null,
    val price: Double,
    @SerialName("compare_at_price") val compareAtPrice: Double? = null,
    @SerialName("stock_quantity") val stockQuantity: Int = 0,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
)

@Serializable
data class Product(
    val id: String,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("name_ka") val nameKa: String,
    @SerialName("description_ka") val descriptionKa: String? = null,
    @SerialName("short_description_ka") val shortDescriptionKa: String? = null,
    val sku: String? = null,
    val price: Double,
    @SerialName("compare_at_price") val compareAtPrice: Double? = null,
    @SerialName("stock_quantity") val stockQuantity: Int = 0,
    @SerialName("is_published") val isPublished: Boolean = true,
    @SerialName("is_featured") val isFeatured: Boolean = false,
    @SerialName("average_rating") val averageRating: Double = 0.0,
    @SerialName("review_count") val reviewCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    // Joined
    val images: List<ProductImage> = emptyList(),
    val attributes: List<ProductAttribute> = emptyList(),
    val variants: List<ProductVariant> = emptyList(),
) {
    val primaryImage: String? get() = images.find { it.isPrimary }?.imageUrl ?: images.firstOrNull()?.imageUrl
    val hasDiscount: Boolean get() = compareAtPrice != null && compareAtPrice > price
    val discountPercent: Int get() {
        if (!hasDiscount) return 0
        return ((compareAtPrice!! - price) / compareAtPrice * 100).toInt()
    }
    val isAvailable: Boolean get() = isPublished && stockQuantity > 0
}

// ── Review ────────────────────────────────────────────────────

@Serializable
data class ProductReview(
    val id: String,
    @SerialName("product_id") val productId: String,
    @SerialName("user_id") val userId: String,
    val rating: Int,
    val comment: String? = null,
    @SerialName("is_verified_purchase") val isVerifiedPurchase: Boolean = false,
    @SerialName("created_at") val createdAt: String,
    // Joined
    @SerialName("reviewer_name") val reviewerName: String? = null,
)

// ── Cart ──────────────────────────────────────────────────────

@Serializable
data class CartItem(
    val id: String,
    @SerialName("cart_id") val cartId: String,
    @SerialName("product_id") val productId: String,
    @SerialName("variant_id") val variantId: String? = null,
    val quantity: Int,
    // Joined product info
    val product: Product? = null,
    val variant: ProductVariant? = null,
) {
    val unitPrice: Double get() = variant?.price ?: product?.price ?: 0.0
    val lineTotal: Double get() = unitPrice * quantity
}

@Serializable
data class Cart(
    val id: String,
    @SerialName("user_id") val userId: String,
    val items: List<CartItem> = emptyList(),
) {
    val itemCount: Int get() = items.sumOf { it.quantity }
    val subtotal: Double get() = items.sumOf { it.lineTotal }
}

// From RPC get_cart_summary
@Serializable
data class CartSummary(
    @SerialName("item_count") val itemCount: Int,
    val subtotal: Double,
    val items: List<CartItemSummary>,
)

@Serializable
data class CartItemSummary(
    @SerialName("cart_item_id") val cartItemId: String,
    @SerialName("product_id") val productId: String,
    @SerialName("product_name_ka") val productNameKa: String,
    @SerialName("image_url") val imageUrl: String?,
    val quantity: Int,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("line_total") val lineTotal: Double,
    val available: Boolean,
)

// ── Address ───────────────────────────────────────────────────

@Serializable
data class Address(
    val id: String,
    @SerialName("user_id") val userId: String,
    val label: String,
    @SerialName("recipient_name") val recipientName: String,
    val phone: String,
    val city: String,
    val address: String,
    val notes: String? = null,
    @SerialName("is_default") val isDefault: Boolean = false,
)

// ── Order ─────────────────────────────────────────────────────

@Serializable
data class OrderItem(
    val id: String,
    @SerialName("order_id") val orderId: String,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("variant_id") val variantId: String? = null,
    // Price snapshots (server sets these, immutable)
    @SerialName("product_name_ka") val productNameKa: String,
    @SerialName("product_sku") val productSku: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val quantity: Int,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("line_total") val lineTotal: Double,
)

@Serializable
data class OrderStatusHistory(
    val id: String,
    @SerialName("order_id") val orderId: String,
    val status: OrderStatus,
    val note: String? = null,
    @SerialName("changed_by") val changedBy: String? = null,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class Order(
    val id: String,
    @SerialName("order_number") val orderNumber: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("customer_name") val customerName: String,
    @SerialName("customer_phone") val customerPhone: String,
    @SerialName("customer_email") val customerEmail: String? = null,
    val status: OrderStatus,
    @SerialName("delivery_address") val deliveryAddress: String? = null,
    @SerialName("delivery_city") val deliveryCity: String? = null,
    @SerialName("delivery_notes") val deliveryNotes: String? = null,
    @SerialName("customer_note") val customerNote: String? = null,
    @SerialName("items_total") val itemsTotal: Double,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    // Joined
    val items: List<OrderItem> = emptyList(),
    val statusHistory: List<OrderStatusHistory> = emptyList(),
    @SerialName("conversation_id") val conversationId: String? = null,
)

// RPC input
@Serializable
data class CreateOrderItem(
    @SerialName("product_id") val productId: String,
    @SerialName("variant_id") val variantId: String? = null,
    val quantity: Int,
)

@Serializable
data class CreateOrderRequest(
    @SerialName("p_customer_name") val customerName: String,
    @SerialName("p_customer_phone") val customerPhone: String,
    @SerialName("p_customer_email") val customerEmail: String? = null,
    @SerialName("p_delivery_address") val deliveryAddress: String? = null,
    @SerialName("p_delivery_city") val deliveryCity: String? = null,
    @SerialName("p_delivery_notes") val deliveryNotes: String? = null,
    @SerialName("p_customer_note") val customerNote: String? = null,
    @SerialName("p_idempotency_key") val idempotencyKey: String,
    @SerialName("p_items") val items: List<CreateOrderItem>,
)

// ── Conversation / Chat ───────────────────────────────────────

@Serializable
data class Message(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("sender_id") val senderId: String? = null,
    @SerialName("sender_type") val senderType: MessageSender,
    val content: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("is_internal_note") val isInternalNote: Boolean = false,
    @SerialName("created_at") val createdAt: String,
    // Joined
    @SerialName("sender_name") val senderName: String? = null,
    @SerialName("sender_avatar_url") val senderAvatarUrl: String? = null,
    val attachments: List<MessageAttachment> = emptyList(),
)

@Serializable
data class MessageAttachment(
    val id: String,
    @SerialName("message_id") val messageId: String,
    @SerialName("file_url") val fileUrl: String,
    @SerialName("file_type") val fileType: String,
    @SerialName("file_name") val fileName: String? = null,
    @SerialName("file_size") val fileSize: Long? = null,
)

@Serializable
data class Conversation(
    val id: String,
    @SerialName("order_id") val orderId: String? = null,
    val subject: String? = null,
    @SerialName("is_resolved") val isResolved: Boolean = false,
    @SerialName("last_message_at") val lastMessageAt: String? = null,
    @SerialName("created_at") val createdAt: String,
    // Joined
    val messages: List<Message> = emptyList(),
    @SerialName("unread_count") val unreadCount: Int = 0,
    @SerialName("order_number") val orderNumber: String? = null,
)

// ── Notifications ─────────────────────────────────────────────

@Serializable
data class AppNotification(
    val id: String,
    @SerialName("user_id") val userId: String,
    val type: NotificationType,
    @SerialName("title_ka") val titleKa: String,
    @SerialName("body_ka") val bodyKa: String,
    @SerialName("deep_link") val deepLink: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String,
)

// ── Favorites ─────────────────────────────────────────────────

@Serializable
data class Favorite(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("product_id") val productId: String,
    @SerialName("created_at") val createdAt: String,
    val product: Product? = null,
)

// ── Home / Banners ────────────────────────────────────────────

@Serializable
data class Banner(
    val id: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("title_ka") val titleKa: String? = null,
    @SerialName("subtitle_ka") val subtitleKa: String? = null,
    @SerialName("cta_text_ka") val ctaTextKa: String? = null,
    @SerialName("deep_link") val deepLink: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true,
)

@Serializable
data class HomeSection(
    val id: String,
    @SerialName("section_type") val sectionType: HomeSectionType,
    @SerialName("title_ka") val titleKa: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true,
    // Items populated by join query
    val products: List<Product> = emptyList(),
    val banners: List<Banner> = emptyList(),
    val categories: List<Category> = emptyList(),
)

// ── Store Settings ────────────────────────────────────────────

@Serializable
data class StoreSetting(
    val id: String,
    val namespace: String,
    val key: String,
    val value: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

// ── Search / Filter ───────────────────────────────────────────

data class ProductFilter(
    val categoryId: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val inStockOnly: Boolean = false,
    val hasDiscount: Boolean = false,
    val minRating: Double? = null,
    val sortBy: SortOrder = SortOrder.POPULAR,
    val query: String? = null,
)

enum class SortOrder {
    POPULAR, NEWEST, PRICE_ASC, PRICE_DESC, RATING;

    fun labelKa(): String = when (this) {
        POPULAR -> "პოპულარული"
        NEWEST -> "ახალი"
        PRICE_ASC -> "იაფიდან ძვირამდე"
        PRICE_DESC -> "ძვირიდან იაფამდე"
        RATING -> "რეიტინგი"
    }
}

// ── UI State wrappers ─────────────────────────────────────────

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val cause: Throwable? = null) : UiState<Nothing>()
}

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class ShowError(val message: String) : UiEvent()
    object NavigateBack : UiEvent()
    data class Navigate(val route: String) : UiEvent()
}

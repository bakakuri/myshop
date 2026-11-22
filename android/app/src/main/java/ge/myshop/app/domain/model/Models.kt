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
}

// DB enum notification_type
@Serializable
enum class NotificationType {
    @SerialName("order_new") ORDER_NEW,
    @SerialName("order_status_update") ORDER_STATUS_UPDATE,
    @SerialName("new_message") NEW_MESSAGE,
    @SerialName("announcement") ANNOUNCEMENT,
    @SerialName("low_stock") LOW_STOCK,
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
    @SerialName("recommended_products") RECOMMENDED_PRODUCTS,
    @SerialName("custom") CUSTOM,
}

// ── User / Profile ────────────────────────────────────────────

@Serializable
data class UserProfile(
    val id: String,
    @SerialName("full_name") val fullName: String? = null,
    val phone: String? = null,
    val email: String? = null, // from auth user, not the profiles table
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val city: String? = null,
    val country: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

// ── Category ──────────────────────────────────────────────────

@Serializable
data class Category(
    val id: String,
    @SerialName("parent_id") val parentId: String? = null,
    @SerialName("name_ka") val nameKa: String,
    val slug: String = "",
    @SerialName("description_ka") val descriptionKa: String? = null,
    @SerialName("icon_url") val iconUrl: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_visible") val isActive: Boolean = true,
    val children: List<Category> = emptyList(), // populated client-side
    // Joined: products(count) — visible (published) products only, via RLS
    val products: List<CountRow> = emptyList(),
) {
    val productCount: Long get() = products.firstOrNull()?.count ?: 0
}

@Serializable
data class CountRow(val count: Long = 0)

// ── Product ───────────────────────────────────────────────────

@Serializable
data class ProductImage(
    val id: String,
    @SerialName("product_id") val productId: String,
    @SerialName("url") val imageUrl: String,
    @SerialName("alt_text") val altTextKa: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

@Serializable
data class ProductAttribute(
    val id: String,
    @SerialName("product_id") val productId: String,
    @SerialName("name_ka") val attributeNameKa: String,
    @SerialName("value_ka") val attributeValueKa: String,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

@Serializable
data class ProductVariant(
    val id: String,
    @SerialName("product_id") val productId: String,
    @SerialName("name_ka") val variantNameKa: String,
    val sku: String? = null,
    // Added to the product base price. The server recomputes all prices at order time.
    @SerialName("price_adjustment") val priceAdjustment: Double = 0.0,
    @SerialName("stock_quantity") val stockQuantity: Int = 0,
    @SerialName("is_available") val isAvailable: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

@Serializable
data class Product(
    val id: String,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("name_ka") val nameKa: String,
    val slug: String = "",
    @SerialName("description_ka") val descriptionKa: String? = null,
    @SerialName("short_description_ka") val shortDescriptionKa: String? = null,
    val brand: String? = null,
    val sku: String? = null,
    val price: Double,
    @SerialName("previous_price") val compareAtPrice: Double? = null,
    @SerialName("discount_percent") val discountPercentValue: Double? = null,
    @SerialName("stock_quantity") val stockQuantity: Int = 0,
    @SerialName("is_available") val availableFlag: Boolean = true,
    @SerialName("is_published") val isPublished: Boolean = true,
    @SerialName("is_featured") val isFeatured: Boolean = false,
    @SerialName("is_popular") val isPopular: Boolean = false,
    @SerialName("is_new") val isNew: Boolean = false,
    @SerialName("average_rating") val averageRating: Double = 0.0,
    @SerialName("review_count") val reviewCount: Int = 0,
    @SerialName("main_image_url") val mainImageUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    // Joined
    val images: List<ProductImage> = emptyList(),
    val attributes: List<ProductAttribute> = emptyList(),
    val variants: List<ProductVariant> = emptyList(),
) {
    val primaryImage: String?
        get() = mainImageUrl ?: images.minByOrNull { it.sortOrder }?.imageUrl
    val allImages: List<String>
        get() = (listOfNotNull(mainImageUrl) + images.sortedBy { it.sortOrder }.map { it.imageUrl }).distinct()
    val hasDiscount: Boolean get() = compareAtPrice != null && compareAtPrice > price
    val discountPercent: Int get() {
        if (!hasDiscount) return 0
        discountPercentValue?.let { if (it > 0) return it.toInt() }
        return ((compareAtPrice!! - price) / compareAtPrice * 100).toInt()
    }
    val isAvailable: Boolean get() = availableFlag && isPublished && stockQuantity > 0
}

// ── Review ────────────────────────────────────────────────────

@Serializable
data class ReviewerProfile(
    @SerialName("full_name") val fullName: String? = null,
)

@Serializable
data class ProductReview(
    val id: String,
    @SerialName("product_id") val productId: String,
    @SerialName("user_id") val userId: String,
    val rating: Int,
    val comment: String? = null,
    @SerialName("is_published") val isPublished: Boolean = true,
    @SerialName("created_at") val createdAt: String,
    // Joined: reviewer:profiles!user_id(full_name)
    val reviewer: ReviewerProfile? = null,
) {
    val reviewerName: String? get() = reviewer?.fullName
}

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
    val unitPrice: Double get() = (product?.price ?: 0.0) + (variant?.priceAdjustment ?: 0.0)
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

// From RPC get_cart_summary (prices computed server-side)
@Serializable
data class CartSummary(
    @SerialName("item_count") val itemCount: Int = 0,
    val subtotal: Double = 0.0,
    val items: List<CartItemSummary> = emptyList(),
)

@Serializable
data class CartItemSummary(
    @SerialName("cart_item_id") val cartItemId: String,
    @SerialName("product_id") val productId: String,
    @SerialName("product_name_ka") val productNameKa: String,
    @SerialName("image_url") val imageUrl: String? = null,
    val quantity: Int,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("previous_price") val previousPrice: Double? = null,
    @SerialName("line_total") val lineTotal: Double,
    @SerialName("is_available") val isAvailable: Boolean = true,
    @SerialName("in_stock") val inStock: Boolean = true,
    @SerialName("variant_id") val variantId: String? = null,
    @SerialName("variant_name_ka") val variantNameKa: String? = null,
)

// ── Address ───────────────────────────────────────────────────

@Serializable
data class Address(
    val id: String = "", // empty when inserting — not encoded, DB generates it
    @SerialName("user_id") val userId: String,
    val label: String? = null,
    @SerialName("full_name") val recipientName: String,
    val phone: String,
    val city: String,
    val district: String? = null,
    val street: String,
    val building: String? = null,
    val apartment: String? = null,
    @SerialName("zip_code") val zipCode: String? = null,
    val notes: String? = null,
    @SerialName("is_default") val isDefault: Boolean = false,
) {
    /** One-line street address, e.g. "ჭავჭავაძის 12, ბინა 5". */
    val addressLine: String
        get() = buildString {
            append(street)
            building?.takeIf { it.isNotBlank() }?.let { append(" ").append(it) }
            apartment?.takeIf { it.isNotBlank() }?.let { append(", ბინა ").append(it) }
            district?.takeIf { it.isNotBlank() }?.let { append(", ").append(it) }
        }
}

// ── Order ─────────────────────────────────────────────────────

@Serializable
data class OrderItem(
    val id: String,
    @SerialName("order_id") val orderId: String,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("variant_id") val variantId: String? = null,
    // Snapshots set by the server at order time (immutable)
    @SerialName("product_name_ka") val productNameKa: String,
    @SerialName("product_sku") val productSku: String? = null,
    @SerialName("variant_name_ka") val variantNameKa: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val quantity: Int,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("previous_price") val previousPrice: Double? = null,
    @SerialName("line_total") val lineTotal: Double,
)

@Serializable
data class OrderStatusHistory(
    val id: String,
    @SerialName("order_id") val orderId: String,
    val status: OrderStatus,
    val note: String? = null,
    @SerialName("changed_at") val createdAt: String,
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
    @SerialName("agreement_note") val agreementNote: String? = null, // staff → customer
    val subtotal: Double = 0.0,
    val total: Double = 0.0,
    val currency: String = "GEL",
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String? = null,
    // Joined
    val items: List<OrderItem> = emptyList(),
    val statusHistory: List<OrderStatusHistory> = emptyList(),
    val conversations: List<ConversationRef> = emptyList(),
) {
    val conversationId: String? get() = conversations.firstOrNull()?.id
}

@Serializable
data class ConversationRef(val id: String)

// Result of RPC create_order
@Serializable
data class CreateOrderResult(
    val success: Boolean = true,
    @SerialName("order_id") val orderId: String,
    @SerialName("order_number") val orderNumber: String,
    @SerialName("conversation_id") val conversationId: String? = null,
    val total: Double? = null,
    val currency: String? = null,
)

// RPC input
@Serializable
data class CreateOrderItem(
    @SerialName("product_id") val productId: String,
    @SerialName("variant_id") val variantId: String? = null,
    val quantity: Int,
)

// ── Conversation / Chat ───────────────────────────────────────

@Serializable
data class SenderProfile(
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

@Serializable
data class Message(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("sender_id") val senderId: String? = null,
    @SerialName("sender_type") val senderType: MessageSender,
    val content: String = "",
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("is_internal_note") val isInternalNote: Boolean = false,
    @SerialName("created_at") val createdAt: String,
    // Joined: sender:profiles!sender_id(full_name, avatar_url)
    val sender: SenderProfile? = null,
    val attachments: List<MessageAttachment> = emptyList(),
) {
    val senderName: String? get() = sender?.fullName
    val senderAvatarUrl: String? get() = sender?.avatarUrl
}

@Serializable
data class MessageAttachment(
    val id: String,
    @SerialName("message_id") val messageId: String,
    @SerialName("url") val fileUrl: String,
    @SerialName("file_type") val fileType: String,
    @SerialName("file_name") val fileName: String? = null,
    @SerialName("file_size") val fileSize: Long? = null,
)

@Serializable
data class OrderRef(
    val id: String? = null,
    @SerialName("order_number") val orderNumber: String? = null,
    val status: OrderStatus? = null,
    val total: Double? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class Conversation(
    val id: String,
    @SerialName("order_id") val orderId: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String? = null,
    // Joined: order:orders!order_id(order_number, status), messages(...)
    val order: OrderRef? = null,
    val messages: List<Message> = emptyList(),
) {
    val orderNumber: String? get() = order?.orderNumber
    val lastMessage: Message? get() = messages.maxByOrNull { it.createdAt }
    val lastMessageAt: String? get() = lastMessage?.createdAt ?: updatedAt
    /** Staff messages the customer hasn't read yet. */
    val customerUnreadCount: Int
        get() = messages.count { !it.isRead && it.senderType == MessageSender.STAFF }
}

// ── Notifications ─────────────────────────────────────────────

@Serializable
data class AppNotification(
    val id: String,
    @SerialName("user_id") val userId: String,
    val type: NotificationType,
    @SerialName("title_ka") val titleKa: String,
    @SerialName("body_ka") val bodyKa: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("related_order_id") val relatedOrderId: String? = null,
    @SerialName("related_conversation_id") val relatedConversationId: String? = null,
    @SerialName("related_product_id") val relatedProductId: String? = null,
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

@Serializable
data class ToggleFavoriteResult(val favorited: Boolean)

// ── Home / Banners ────────────────────────────────────────────

@Serializable
data class Banner(
    val id: String,
    val type: BannerType = BannerType.HERO,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("image_url_mobile") val imageUrlMobile: String? = null,
    @SerialName("title_ka") val titleKa: String? = null,
    @SerialName("subtitle_ka") val subtitleKa: String? = null,
    @SerialName("cta_label_ka") val ctaTextKa: String? = null,
    @SerialName("cta_url") val deepLink: String? = null,
    @SerialName("bg_color") val bgColor: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true,
) {
    val displayImage: String? get() = imageUrlMobile ?: imageUrl
}

@Serializable
data class HomeSection(
    val id: String,
    @SerialName("type") val sectionType: HomeSectionType,
    @SerialName("title_ka") val titleKa: String? = null,
    @SerialName("subtitle_ka") val subtitleKa: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_enabled") val isActive: Boolean = true,
    // Populated client-side by HomeViewModel
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

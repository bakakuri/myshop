package ge.myshop.app.data.repository

import ge.myshop.app.domain.model.Category
import ge.myshop.app.domain.model.Product
import ge.myshop.app.domain.model.ProductFilter
import ge.myshop.app.domain.model.ProductReview
import ge.myshop.app.domain.model.SortOrder
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {

    suspend fun getCategories(parentId: String? = null): Result<List<Category>> {
        return runCatching {
            supabase.postgrest
                .from("categories")
                .select(Columns.ALL) {
                    filter {
                        eq("is_visible", true)
                        if (parentId == null) {
                            exact("parent_id", null)
                        } else {
                            eq("parent_id", parentId)
                        }
                    }
                    order("sort_order", Order.ASCENDING)
                }
                .decodeList<Category>()
        }
    }

    suspend fun getCategory(id: String): Result<Category> {
        return runCatching {
            supabase.postgrest
                .from("categories")
                .select(Columns.ALL) { filter { eq("id", id) } }
                .decodeSingle<Category>()
        }
    }

    suspend fun getProducts(
        filter: ProductFilter = ProductFilter(),
        page: Int = 0,
        pageSize: Int = 20,
    ): Result<List<Product>> {
        return runCatching {
            supabase.postgrest
                .from("products")
                .select(
                    Columns.raw(
                        """
                        *,
                        images:product_images(*),
                        attributes:product_attributes(*),
                        variants:product_variants(*)
                        """.trimIndent()
                    )
                ) {
                    filter {
                        eq("is_published", true)
                        filter.categoryId?.let { eq("category_id", it) }
                        filter.minPrice?.let { gte("price", it) }
                        filter.maxPrice?.let { lte("price", it) }
                        if (filter.inStockOnly) gt("stock_quantity", 0)
                        if (filter.hasDiscount) gt("previous_price", 0)
                        filter.query?.let { ilike("name_ka", "%$it%") }
                    }
                    when (filter.sortBy) {
                        SortOrder.POPULAR -> order("review_count", Order.DESCENDING)
                        SortOrder.NEWEST -> order("created_at", Order.DESCENDING)
                        SortOrder.PRICE_ASC -> order("price", Order.ASCENDING)
                        SortOrder.PRICE_DESC -> order("price", Order.DESCENDING)
                        SortOrder.RATING -> order("average_rating", Order.DESCENDING)
                    }
                    range(from = (page * pageSize).toLong(), to = ((page + 1) * pageSize - 1).toLong())
                }
                .decodeList<Product>()
        }
    }

    suspend fun getProduct(id: String): Result<Product> {
        return runCatching {
            supabase.postgrest
                .from("products")
                .select(
                    Columns.raw(
                        """
                        *,
                        images:product_images(*),
                        attributes:product_attributes(*),
                        variants:product_variants(*)
                        """.trimIndent()
                    )
                ) {
                    filter { eq("id", id) }
                }
                .decodeSingle<Product>()
        }
    }

    suspend fun getFeaturedProducts(limit: Int = 10): Result<List<Product>> {
        return runCatching {
            supabase.postgrest
                .from("products")
                .select(
                    Columns.raw("*, images:product_images(*)")
                ) {
                    filter {
                        eq("is_published", true)
                        eq("is_featured", true)
                        gt("stock_quantity", 0)
                    }
                    order("review_count", Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<Product>()
        }
    }

    suspend fun getDiscountedProducts(limit: Int = 10): Result<List<Product>> {
        return runCatching {
            supabase.postgrest
                .from("products")
                .select(Columns.raw("*, images:product_images(*)")) {
                    filter {
                        eq("is_published", true)
                        gt("previous_price", 0)
                        gt("stock_quantity", 0)
                    }
                    order("created_at", Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<Product>()
        }
    }

    suspend fun getNewProducts(limit: Int = 10): Result<List<Product>> {
        return runCatching {
            supabase.postgrest
                .from("products")
                .select(Columns.raw("*, images:product_images(*)")) {
                    filter {
                        eq("is_published", true)
                        gt("stock_quantity", 0)
                    }
                    order("created_at", Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<Product>()
        }
    }

    suspend fun searchProducts(query: String, limit: Int = 20): Result<List<Product>> {
        return runCatching {
            supabase.postgrest
                .from("products")
                .select(Columns.raw("*, images:product_images(*)")) {
                    filter {
                        eq("is_published", true)
                        ilike("name_ka", "%$query%")
                    }
                    limit(limit.toLong())
                }
                .decodeList<Product>()
        }
    }

    suspend fun getRelatedProducts(productId: String, limit: Int = 10): Result<List<Product>> {
        return runCatching {
            // Via related_products junction table
            supabase.postgrest
                .from("related_products")
                .select(
                    Columns.raw("related_product:products!related_product_id(*, images:product_images(*))")
                ) {
                    filter { eq("product_id", productId) }
                    limit(limit.toLong())
                }
                .decodeList<RelatedProductRow>()
                .mapNotNull { it.product }
        }
    }

    suspend fun getReviews(productId: String): Result<List<ProductReview>> {
        return runCatching {
            supabase.postgrest
                .from("product_reviews")
                .select(
                    Columns.raw("*, reviewer:profiles!user_id(full_name)")
                ) {
                    filter {
                        eq("product_id", productId)
                        eq("is_published", true)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<ProductReview>()
        }
    }
}

@kotlinx.serialization.Serializable
private data class RelatedProductRow(
    @kotlinx.serialization.SerialName("related_product") val product: Product? = null,
)

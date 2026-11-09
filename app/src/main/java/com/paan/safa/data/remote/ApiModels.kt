package com.paan.safa.data.remote

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.roundToInt

/** Reads 40, 40.00 and "40.00" alike (Laravel decimals often arrive as strings). */
object FlexIntSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexInt", PrimitiveKind.INT)
    override fun deserialize(decoder: Decoder): Int {
        val element = (decoder as JsonDecoder).decodeJsonElement()
        val text = (element as? JsonPrimitive)?.takeIf { it !is JsonNull }?.content ?: return 0
        return text.toDoubleOrNull()?.roundToInt() ?: 0
    }
    override fun serialize(encoder: Encoder, value: Int) = encoder.encodeInt(value)
}

/** Reads ids whether the server sends 7 or "7". */
object FlexStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexString", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): String {
        val element = (decoder as JsonDecoder).decodeJsonElement()
        return (element as? JsonPrimitive)?.takeIf { it !is JsonNull }?.content ?: ""
    }
    override fun serialize(encoder: Encoder, value: String) = encoder.encodeString(value)
}

@Serializable
data class ApiResponse<T>(
    val success: Boolean = true,
    val message: String? = null,
    val data: T? = null,
    val meta: PageMeta? = null,
    val errors: Map<String, List<String>>? = null,
    val code: String? = null,
)

@Serializable
data class PageMeta(
    @SerialName("current_page") val currentPage: Int = 1,
    @SerialName("last_page") val lastPage: Int = 1,
    @SerialName("per_page") val perPage: Int = 0,
    val total: Int = 0,
    @SerialName("unread_count") val unreadCount: Int? = null,
)

// ---- Auth / profile ----

@Serializable
data class SendOtpRequest(val phone: String, val purpose: String = "login")

@Serializable
data class VerifyOtpRequest(val phone: String, val otp: String)

@Serializable
data class ProfileRequest(val name: String)

@Serializable
data class UserDto(
    @Serializable(with = FlexStringSerializer::class) val id: String,
    val name: String? = null,
    val phone: String = "",
    val email: String? = null,
)

@Serializable
data class AuthDataDto(
    val token: String,
    val user: UserDto,
    @SerialName("is_new_user") val isNewUser: Boolean = false,
)

// ---- Catalog ----

@Serializable
data class CategoryDto(
    @Serializable(with = FlexStringSerializer::class) val id: String,
    val name: String,
)

@Serializable
data class ProductDto(
    @Serializable(with = FlexStringSerializer::class) val id: String,
    val name: String,
    val description: String? = null,
    @Serializable(with = FlexIntSerializer::class) val price: Int = 0,
    val category: CategoryDto? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("is_available") val isAvailable: Boolean = true,
    @SerialName("is_featured") val isFeatured: Boolean = false,
    val tag: String? = null,
    @SerialName("prep_minutes") val prepMinutes: Int = 3,
    val ingredients: List<String> = emptyList(),
)

// ---- Orders ----

@Serializable
data class ItemOptionsRequest(val sweetness: String, val supari: String)

@Serializable
data class CreateItemRequest(
    @SerialName("product_id") val productId: Long,
    val quantity: Int,
    val options: ItemOptionsRequest,
)

@Serializable
data class CreateOrderRequest(
    val items: List<CreateItemRequest>,
    val comment: String? = null,
    @SerialName("payment_method") val paymentMethod: String = "gateway",
    @SerialName("payment_channel") val paymentChannel: String,
)

@Serializable
data class OrderOptionsDto(val sweetness: String? = null, val supari: String? = null)

@Serializable
data class OrderItemDto(
    @Serializable(with = FlexStringSerializer::class) @SerialName("product_id") val productId: String = "",
    val name: String,
    @Serializable(with = FlexIntSerializer::class) @SerialName("unit_price") val unitPrice: Int = 0,
    val quantity: Int = 1,
    @SerialName("image_url") val imageUrl: String? = null,
    val options: OrderOptionsDto? = null,
)

@Serializable
data class OrderDto(
    @Serializable(with = FlexStringSerializer::class) val id: String,
    @SerialName("order_number") val orderNumber: String,
    @Serializable(with = FlexIntSerializer::class) val token: Int = 0,
    val status: String,
    @SerialName("payment_status") val paymentStatus: String,
    @SerialName("payment_channel") val paymentChannel: String? = null,
    @Serializable(with = FlexIntSerializer::class) val total: Int = 0,
    val comment: String? = null,
    val items: List<OrderItemDto> = emptyList(),
    @SerialName("created_at") val createdAt: String = "",
)

@Serializable
data class OrderStatusDto(
    @Serializable(with = FlexStringSerializer::class) val id: String,
    val status: String,
    @SerialName("payment_status") val paymentStatus: String,
    @SerialName("failure_reason") val failureReason: String? = null,
)

@Serializable
data class PayOrderDto(
    @Serializable(with = FlexStringSerializer::class) @SerialName("order_id") val orderId: String = "",
    @SerialName("payment_status") val paymentStatus: String = "pending",
    @SerialName("checkout_url") val checkoutUrl: String? = null,
)
@Serializable
data class DeviceTokenRequest(val token: String, val platform: String, val app: String)


// ---- Notifications ----

@Serializable
data class NotificationDto(
    @Serializable(with = FlexStringSerializer::class) val id: String,
    val type: String = "order_status",
    val title: String = "",
    val body: String = "",
    @Serializable(with = FlexStringSerializer::class) @SerialName("order_id") val orderId: String = "", // "" = none
    val status: String? = null,
    @SerialName("read_at") val readAt: String? = null,
    @SerialName("created_at") val createdAt: String = "",
)

@Serializable
data class UnreadCountDto(@SerialName("unread_count") val unreadCount: Int = 0)

@Serializable
data class PickupRequest(val code: String)

@Serializable
data class ShopDto(
    @SerialName("shop_name") val shopName: String? = null,
    @SerialName("shop_address") val shopAddress: String? = null,
    @SerialName("shop_phone") val shopPhone: String? = null,
    @SerialName("pickup_message") val pickupMessage: String? = null,
    @SerialName("status_message") val statusMessage: String? = null,
    @SerialName("is_open") val isOpen: Boolean? = null,
    @SerialName("shop_status") val shopStatus: String? = null,
    val status: String? = null,
    @SerialName("opening_time") val openingTime: String? = null,
    @SerialName("closing_time") val closingTime: String? = null,
)
package com.paan.safa.data.remote

import com.paan.safa.domain.model.CartItem
import com.paan.safa.domain.model.Category
import com.paan.safa.domain.model.ItemOptions
import com.paan.safa.domain.model.Order
import com.paan.safa.domain.model.OrderItem
import com.paan.safa.domain.model.OrderStatus
import com.paan.safa.domain.model.PaymentMethod
import com.paan.safa.domain.model.PaymentStatus
import com.paan.safa.domain.model.Product
import com.paan.safa.domain.model.SupariType
import com.paan.safa.domain.model.Sweetness
import com.paan.safa.domain.model.User
import com.paan.safa.domain.model.AppNotification
import com.paan.safa.domain.model.ShopInfo
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.Instant
import java.time.OffsetDateTime

fun UserDto.toDomain() = User(
    id = id,
    // Keep the server value intact. UI chooses a localized fallback for an unnamed account.
    name = name.orEmpty(),
    phone = phone,
)

fun CategoryDto.toDomain() = Category(id = id, name = name)

fun ProductDto.toDomain() = Product(
    id = id,
    name = name,
    description = description.orEmpty(),
    price = price,
    imageUrl = imageUrl,
    thumbnailUrl = thumbnailUrl,
    categoryId = category?.id.orEmpty(),
    isAvailable = isAvailable,
    tag = tag?.takeIf { it.isNotBlank() },
    prepMinutes = prepMinutes.coerceAtLeast(1),
    isFeatured = isFeatured,
    ingredients = ingredients.filter { it.isNotBlank() },
)

private fun OrderOptionsDto?.toItemOptions() = ItemOptions(
    sweetness = Sweetness.entries.firstOrNull { it.api == this?.sweetness } ?: Sweetness.Regular,
    supari = SupariType.entries.firstOrNull { it.api == this?.supari } ?: SupariType.Fried,
)

fun OrderItemDto.toDomain() = OrderItem(
    productId = productId,
    name = name,
    unitPrice = unitPrice,
    quantity = quantity,
    options = options.toItemOptions(),
    imageUrl = imageUrl,
)

fun OrderDto.toDomain(): Order {
    val created = parseTime(createdAt)
    return Order(
        id = id,
        orderNumber = orderNumber,
        token = token,
        items = items.map { it.toDomain() },
        total = total,
        comment = comment.orEmpty(),
        paymentMethod = paymentChannel.toPaymentMethod(),
        paymentStatus = paymentStatus.toPaymentStatus(),
        status = resolveOrderStatus(status, paymentStatus, created),
        createdAt = created,
    )
}

const val PAYMENT_EXPIRY_MS = 15 * 60 * 1000L

fun resolveOrderStatus(status: String, payment: String, createdAt: Long): OrderStatus {
    val s = status.toOrderStatus()
    val p = payment.toPaymentStatus()
    val expired = p == PaymentStatus.PENDING &&
            System.currentTimeMillis() - createdAt > PAYMENT_EXPIRY_MS
    return if (s == OrderStatus.PENDING && (p == PaymentStatus.FAILED || expired))
        OrderStatus.PAYMENT_FAILED else s
}

fun String.toOrderStatus(): OrderStatus = when (lowercase()) {
    "making" -> OrderStatus.MAKING
    "ready" -> OrderStatus.READY
    "completed" -> OrderStatus.COMPLETED
    "admin_cancelled", "cancelled" -> OrderStatus.ADMIN_CANCELLED
    "payment_failed", "failed" -> OrderStatus.PAYMENT_FAILED
    else -> OrderStatus.PENDING
}

fun String.toPaymentStatus(): PaymentStatus = when (lowercase()) {
    "paid" -> PaymentStatus.PAID
    "failed" -> PaymentStatus.FAILED
    "refunded" -> PaymentStatus.REFUNDED
    else -> PaymentStatus.PENDING
}

private fun String?.toPaymentMethod(): PaymentMethod =
    if (this?.lowercase() == "card") PaymentMethod.Card else PaymentMethod.Upi

val PaymentMethod.channel: String
    get() = when (this) {
        PaymentMethod.Upi -> "upi"
        PaymentMethod.Card -> "card"
    }

fun CartItem.toRequest() = CreateItemRequest(
    productId = product.id.toLong(),
    quantity = quantity,
    options = ItemOptionsRequest(sweetness = options.sweetness.api, supari = options.supari.api),
)

private fun parseTime(value: String): Long =
    runCatching { OffsetDateTime.parse(value).toInstant().toEpochMilli() }
        .recoverCatching { Instant.parse(value).toEpochMilli() }
        .getOrDefault(0L)


fun NotificationDto.toDomain() = AppNotification(
    id = id,
    title = title,
    body = body,
    orderId = orderId.takeIf { it.isNotBlank() },
    status = status?.takeIf { it.isNotBlank() }?.toOrderStatus(),
    readAt = readAt?.takeIf { it.isNotBlank() }?.let(::parseTime),
    createdAt = parseTime(createdAt),
)

private val clockFormat = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

private fun clockText(t: String?): String? =
    t?.takeIf { it.isNotBlank() }
        ?.let { runCatching { LocalTime.parse(it.take(5)).format(clockFormat) }.getOrNull() }

fun ShopDto.toDomain(): ShopInfo {
    val open = isOpen ?: (shopStatus ?: status).equals("open", ignoreCase = true)
    val from = clockText(openingTime)
    val to = clockText(closingTime)
    return ShopInfo(
        isOpen = open,
        hours = if (from != null && to != null) "$from – $to" else null,
        name = shopName?.trim().orEmpty(),
        address = shopAddress?.trim().orEmpty(),
        phone = shopPhone?.trim().orEmpty(),
        pickupMessage = pickupMessage?.trim().orEmpty(),
        statusMessage = statusMessage?.trim().orEmpty(),
    )
}

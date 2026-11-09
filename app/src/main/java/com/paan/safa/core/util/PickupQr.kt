package com.paan.safa.core.util

data class PickupQr(val orderNumber: String, val code: String)

private val CODE_PATTERN = Regex("^[A-Za-z0-9_-]{16,128}$")

/** Reads "PAANPICKUP1|<order_number>|<code>". Returns null for any other QR code. */
fun parsePickupQr(raw: String): PickupQr? {
    val parts = raw.trim().split('|')
    if (parts.size != 3 || parts[0] != "PAANPICKUP1") return null
    val number = parts[1].trim()
    val code = parts[2].trim()
    if (number.isEmpty() || !CODE_PATTERN.matches(code)) return null
    return PickupQr(number, code)
}
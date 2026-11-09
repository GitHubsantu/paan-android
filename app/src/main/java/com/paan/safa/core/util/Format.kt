package com.paan.safa.core.util

import java.util.Locale
import android.content.Context
import com.paan.safa.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Int.asRupees(): String = "₹$this"

fun Long.asOrderTime(): String =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale.getDefault()))

/** "9876543210" → "+91 98765 43210" (falls back to the raw text if it isn't 10 digits). */
fun String.asIndianPhone(): String =
    if (length == 10) "+91 ${take(5)} ${drop(5)}" else this

/** Up to two initials for the avatar, e.g. "Rahul Das" → "RD". */
fun String.asInitials(): String =
    trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }


/** "Just now", "5 min ago", "3 h ago", "Yesterday", then "12 Oct". */
fun Long.asTimeAgo(context: Context, now: Long = System.currentTimeMillis()): String {
    val minutes = ((now - this).coerceAtLeast(0)) / 60_000
    return when {
        minutes < 1 -> context.getString(R.string.time_just_now)
        minutes < 60 -> context.resources.getQuantityString(R.plurals.time_minutes_ago, minutes.toInt(), minutes.toInt())
        minutes < 60 * 24 -> {
            val hours = (minutes / 60).toInt()
            context.resources.getQuantityString(R.plurals.time_hours_ago, hours, hours)
        }
        minutes < 60 * 48 -> context.getString(R.string.time_yesterday)
        else -> Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault()))
    }
}

/** Badge text: 1..9, then "9+". */
fun Int.asBadgeCount(): String = if (this > 9) "9+" else toString()

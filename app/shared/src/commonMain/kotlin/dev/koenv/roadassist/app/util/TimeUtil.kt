package dev.koenv.roadassist.app.util

expect fun parseIso8601ToMillis(iso: String): Long

fun timeAgo(isoString: String, nowMillis: Long): String {
    val diff = (nowMillis - parseIso8601ToMillis(isoString)).coerceAtLeast(0L)
    return when {
        diff < 60_000L -> "just now"
        diff < 3_600_000L -> "${diff / 60_000L}m"
        diff < 86_400_000L -> "${diff / 3_600_000L}h"
        else -> "${diff / 86_400_000L}d"
    }
}

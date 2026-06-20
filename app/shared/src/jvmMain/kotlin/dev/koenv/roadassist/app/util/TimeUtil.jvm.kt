package dev.koenv.roadassist.app.util

actual fun parseIso8601ToMillis(iso: String): Long =
    java.time.Instant.parse(iso).toEpochMilli()

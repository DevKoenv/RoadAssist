package dev.koenv.roadassist.app

import dev.koenv.roadassist.core.Role
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
@Suppress("TooGenericExceptionCaught", "SwallowedException")
fun decodeRoleFromJwt(token: String): Role? {
    return try {
        val payload = token.split(".").getOrNull(1) ?: return null
        val padded = payload.padEnd((payload.length + 3) / 4 * 4, '=')
        val decoded = Base64.UrlSafe.decode(padded)
        val json = decoded.decodeToString()
        val roleStr = Json.parseToJsonElement(json).jsonObject["role"]?.jsonPrimitive?.content
        roleStr?.let { Role.valueOf(it) }
    } catch (e: Exception) {
        null
    }
}

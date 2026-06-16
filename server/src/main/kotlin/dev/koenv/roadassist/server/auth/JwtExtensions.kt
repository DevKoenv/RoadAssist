package dev.koenv.roadassist.server.auth

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

internal fun ApplicationCall.jwtClaims(): Pair<Int, String>? {
    val principal = principal<JWTPrincipal>() ?: return null
    val userId = principal.payload.subject?.toIntOrNull() ?: return null
    val role = principal.payload.getClaim("role")?.asString() ?: return null
    return userId to role
}

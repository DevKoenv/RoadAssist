package dev.koenv.roadassist.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.koenv.roadassist.core.AuthResponse
import dev.koenv.roadassist.core.LoginRequest
import dev.koenv.roadassist.core.RefreshRequest
import dev.koenv.roadassist.server.database.RefreshTokensTable
import dev.koenv.roadassist.server.database.UsersTable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.mindrot.jbcrypt.BCrypt
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Date

private const val ACCESS_TOKEN_TTL_MS = 15 * 60 * 1000L
private const val REFRESH_TOKEN_TTL_MS = 30 * 24 * 60 * 60 * 1000L

fun Route.configureAuthRouting(jwtSecret: String) {
    route("/auth") {
        post("/login") { handleLogin(call, jwtSecret) }
        post("/refresh") { handleRefresh(call, jwtSecret) }
        post("/logout") { handleLogout(call) }
    }
}

private suspend fun handleLogin(call: ApplicationCall, jwtSecret: String) {
    val request = call.receive<LoginRequest>()
    val user = transaction {
        UsersTable.selectAll().where { UsersTable.username eq request.username }.firstOrNull()
    }
    if (user == null || !BCrypt.checkpw(request.password, user[UsersTable.passwordHash])) {
        call.respondText("Invalid credentials", status = HttpStatusCode.Unauthorized)
        return
    }
    val now = System.currentTimeMillis()
    val accessToken = JWT.create()
        .withSubject(user[UsersTable.id].value.toString())
        .withClaim("role", user[UsersTable.role].name)
        .withExpiresAt(Date(now + ACCESS_TOKEN_TTL_MS))
        .sign(Algorithm.HMAC256(jwtSecret))
    val refreshToken = generateRefreshToken()
    transaction {
        RefreshTokensTable.insert {
            it[userId] = user[UsersTable.id]
            it[tokenHash] = sha256(refreshToken)
            it[expiresAt] = now + REFRESH_TOKEN_TTL_MS
            it[revoked] = false
        }
    }
    call.respond(AuthResponse(token = accessToken, refreshToken = refreshToken, role = user[UsersTable.role]))
}

private suspend fun handleRefresh(call: ApplicationCall, jwtSecret: String) {
    val request = call.receive<RefreshRequest>()
    val hash = sha256(request.refreshToken)
    val now = System.currentTimeMillis()
    val tokenRow = transaction {
        RefreshTokensTable.selectAll()
            .where { (RefreshTokensTable.tokenHash eq hash) and (RefreshTokensTable.revoked eq false) }
            .firstOrNull()
    }
    if (tokenRow == null || tokenRow[RefreshTokensTable.expiresAt] < now) {
        call.respondText("Invalid or expired refresh token", status = HttpStatusCode.Unauthorized)
        return
    }
    val userId = tokenRow[RefreshTokensTable.userId]
    val user = transaction {
        UsersTable.selectAll().where { UsersTable.id eq userId }.firstOrNull()
    }
    if (user == null) {
        call.respondText("Invalid or expired refresh token", status = HttpStatusCode.Unauthorized)
        return
    }
    val accessToken = JWT.create()
        .withSubject(user[UsersTable.id].value.toString())
        .withClaim("role", user[UsersTable.role].name)
        .withExpiresAt(Date(now + ACCESS_TOKEN_TTL_MS))
        .sign(Algorithm.HMAC256(jwtSecret))
    call.respond(AuthResponse(token = accessToken, refreshToken = request.refreshToken, role = user[UsersTable.role]))
}

private suspend fun handleLogout(call: ApplicationCall) {
    val request = call.receive<RefreshRequest>()
    val hash = sha256(request.refreshToken)
    transaction {
        RefreshTokensTable.update({ RefreshTokensTable.tokenHash eq hash }) {
            it[revoked] = true
        }
    }
    call.respond(HttpStatusCode.NoContent)
}

private fun generateRefreshToken(): String {
    val bytes = ByteArray(32)
    SecureRandom().nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
}

private fun sha256(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

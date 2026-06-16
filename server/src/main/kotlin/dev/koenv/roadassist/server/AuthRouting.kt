package dev.koenv.roadassist.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.koenv.roadassist.core.auth.AuthResponse
import dev.koenv.roadassist.core.auth.LoginRequest
import dev.koenv.roadassist.core.auth.RefreshRequest
import dev.koenv.roadassist.core.auth.RegisterRequest
import dev.koenv.roadassist.core.user.Role
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
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Date
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.mindrot.jbcrypt.BCrypt

private const val ACCESS_TOKEN_TTL_MS = 24 * 60 * 60 * 1000L  // 1 day
private const val REFRESH_TOKEN_TTL_MS = 7 * 24 * 60 * 60 * 1000L  // 7 days

fun Route.configureAuthRouting(jwtSecret: String) {
    route("/auth") {
        post("/register") { handleRegister(call, jwtSecret) }
        post("/login") { handleLogin(call, jwtSecret) }
        post("/refresh") { handleRefresh(call, jwtSecret) }
        post("/logout") { handleLogout(call) }
    }
}

private suspend fun handleRegister(call: ApplicationCall, jwtSecret: String) {
    val request = call.receive<RegisterRequest>()
    val exists = transaction {
        UsersTable.selectAll().where { UsersTable.username eq request.username }.count() > 0L
    }
    if (exists) {
        call.respondText("Username already taken", status = HttpStatusCode.Conflict)
        return
    }
    val now = System.currentTimeMillis()
    val userId = transaction {
        UsersTable.insert {
            it[username] = request.username
            it[passwordHash] = BCrypt.hashpw(request.password, BCrypt.gensalt())
            it[role] = Role.ROAD_USER
        } get UsersTable.id
    }
    val accessToken = JWT.create()
        .withSubject(userId.value.toString())
        .withClaim("role", Role.ROAD_USER.name)
        .withExpiresAt(Date(now + ACCESS_TOKEN_TTL_MS))
        .sign(Algorithm.HMAC256(jwtSecret))
    val refreshToken = generateRefreshToken()
    transaction {
        RefreshTokensTable.insert {
            it[RefreshTokensTable.userId] = userId
            it[tokenHash] = sha256(refreshToken)
            it[expiresAt] = now + REFRESH_TOKEN_TTL_MS
            it[revoked] = false
        }
    }
    call.respond(HttpStatusCode.Created, AuthResponse(token = accessToken, refreshToken = refreshToken, role = Role.ROAD_USER))
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

// 32 random bytes = 64 hex chars; enough entropy for a non-guessable token
private fun generateRefreshToken(): String {
    val bytes = ByteArray(32)
    SecureRandom().nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
}

// Only the hash is stored; the raw token is never persisted
private fun sha256(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

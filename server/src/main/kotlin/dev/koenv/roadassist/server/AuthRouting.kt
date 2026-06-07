package dev.koenv.roadassist.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.koenv.roadassist.core.AuthResponse
import dev.koenv.roadassist.core.LoginRequest
import dev.koenv.roadassist.server.database.UsersTable
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.Date

fun Route.configureAuthRouting(jwtSecret: String) {
    route("/auth") {
        post("/login") {
            val request = call.receive<LoginRequest>()
            val user = transaction {
                UsersTable.selectAll()
                    .where { UsersTable.username eq request.username }
                    .firstOrNull()
            }
            if (user == null || !BCrypt.checkpw(request.password, user[UsersTable.passwordHash])) {
                call.respondText("Invalid credentials", status = HttpStatusCode.Unauthorized)
                return@post
            }
            val token = JWT.create()
                .withSubject(user[UsersTable.id].value.toString())
                .withClaim("role", user[UsersTable.role].name)
                .withExpiresAt(Date(System.currentTimeMillis() + 86_400_000L))
                .sign(Algorithm.HMAC256(jwtSecret))
            call.respond(AuthResponse(token = token, role = user[UsersTable.role]))
        }
    }
}

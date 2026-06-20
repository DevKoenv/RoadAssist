package dev.koenv.roadassist.server.database

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object RefreshTokensTable : IntIdTable("refresh_tokens") {
    val userId = reference("user_id", UsersTable)
    val tokenHash = varchar("token_hash", 64).uniqueIndex()  // SHA-256 hex = always 64 chars; unique index for fast lookup
    val expiresAt = long("expires_at")
    val revoked = bool("revoked").default(false)
}

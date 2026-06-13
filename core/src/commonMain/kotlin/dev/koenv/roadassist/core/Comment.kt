package dev.koenv.roadassist.core

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: Int,
    val incidentId: Int,
    val authorRole: AuthorRole,
    val type: CommentType,
    val content: String,
    val createdAt: String,
)

package dev.koenv.roadassist.core.comment

import kotlinx.serialization.Serializable

@Serializable
data class PostCommentRequest(val content: String)

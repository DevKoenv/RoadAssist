package dev.koenv.roadassist.core

import kotlinx.serialization.Serializable

@Serializable
data class PostCommentRequest(val content: String)

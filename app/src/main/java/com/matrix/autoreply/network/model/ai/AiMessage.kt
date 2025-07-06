package com.matrix.autoreply.network.model.ai

import com.google.gson.annotations.SerializedName

data class AiMessage(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: String
)
package com.matrix.autoreply.network.model.ai

import com.google.gson.annotations.SerializedName

data class AiRequest(
    @SerializedName("model")
    val model: String,
    @SerializedName("messages")
    val messages: List<AiMessage>,
    @SerializedName("max_tokens")
    val maxTokens: Int = 150,
    @SerializedName("temperature")
    val temperature: Double = 0.7
)
package com.matrix.autoreply.network.model.ai

import com.google.gson.annotations.SerializedName

data class AiResponse(
    @SerializedName("choices")
    val choices: List<AiChoice>
)

data class AiChoice(
    @SerializedName("message")
    val message: AiMessage,
    @SerializedName("finish_reason")
    val finishReason: String
)
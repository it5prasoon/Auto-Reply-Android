package com.matrix.autoreply.network.model.ai

import com.google.gson.annotations.SerializedName

data class AiErrorResponse(
    @SerializedName("error")
    val error: AiErrorDetail?
)

data class AiErrorDetail(
    @SerializedName("message")
    val message: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("code")
    val code: String?
)
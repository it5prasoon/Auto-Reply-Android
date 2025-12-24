package com.matrix.autoreply.network.model.ai

import com.google.gson.annotations.SerializedName

data class OllamaModelResponse(
    @SerializedName("models")
    val models: List<OllamaModel>
)

data class OllamaModel(
    @SerializedName("name")
    val name: String,
    @SerializedName("size")
    val size: Long,
    @SerializedName("digest")
    val digest: String? = null,
    @SerializedName("modified_at")
    val modifiedAt: String? = null
)

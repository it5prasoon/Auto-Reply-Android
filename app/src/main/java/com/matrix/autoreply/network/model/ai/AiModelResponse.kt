package com.matrix.autoreply.network.model.ai

import com.google.gson.annotations.SerializedName

data class AiModelResponse(
    @SerializedName("data")
    val data: List<AiModel>
)

data class AiModel(
    @SerializedName("id")
    val id: String,
    @SerializedName("object")
    val objectType: String,
    @SerializedName("created")
    val created: Long,
    @SerializedName("owned_by")
    val ownedBy: String
)
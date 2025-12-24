package com.matrix.autoreply.network.model.ai

import com.google.gson.annotations.SerializedName

data class GoogleModelResponse(
    @SerializedName("models")
    val models: List<GoogleModel>
)

data class GoogleModel(
    @SerializedName("name")
    val name: String,
    @SerializedName("displayName")
    val displayName: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("supportedGenerationMethods")
    val supportedGenerationMethods: List<String> = emptyList()
)

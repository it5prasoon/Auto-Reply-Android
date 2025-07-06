package com.matrix.autoreply.network

import com.matrix.autoreply.network.model.ai.AiRequest
import com.matrix.autoreply.network.model.ai.AiResponse
import com.matrix.autoreply.network.model.ai.AiModelResponse
import retrofit2.Call
import retrofit2.http.*

interface AiService {
    @POST("v1/chat/completions")
    fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Body requestBody: AiRequest
    ): Call<AiResponse>

    @GET("v1/models")
    fun getModels(@Header("Authorization") authorization: String): Call<AiModelResponse>
}
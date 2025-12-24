package com.matrix.autoreply.network

import com.matrix.autoreply.network.model.ai.AiRequest
import com.matrix.autoreply.network.model.ai.AiResponse
import com.matrix.autoreply.network.model.ai.AiModelResponse
import com.matrix.autoreply.network.model.ai.GoogleModelResponse
import com.matrix.autoreply.network.model.ai.OllamaModelResponse
import retrofit2.Call
import retrofit2.http.*

interface AiService {
    // OpenAI & Groq compatible endpoints
    @POST("v1/chat/completions")
    fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Body requestBody: AiRequest
    ): Call<AiResponse>

    @GET("v1/models")
    fun getModels(@Header("Authorization") authorization: String): Call<AiModelResponse>
    
    // Anthropic endpoints
    @POST("v1/messages")
    fun getAnthropicCompletion(
        @Header("Authorization") authorization: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body requestBody: AiRequest
    ): Call<AiResponse>
    
    // Google Gemini endpoints
    @GET("v1beta/models")
    fun getGoogleModels(@Query("key") apiKey: String): Call<GoogleModelResponse>
    
    // Ollama endpoints
    @GET("api/tags")
    fun getOllamaModels(): Call<OllamaModelResponse>
    
    @POST("api/generate")
    fun getOllamaCompletion(@Body requestBody: AiRequest): Call<AiResponse>
    
    // Bedrock - Note: Bedrock uses AWS SDK, not Retrofit in practice
    // These are placeholder endpoints for future implementation
    @GET("model/list")
    fun getBedrockModels(@Header("Authorization") authorization: String): Call<AiModelResponse>
}

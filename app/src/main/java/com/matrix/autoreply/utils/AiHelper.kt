package com.matrix.autoreply.utils

import android.content.Context
import android.util.Log
import com.matrix.autoreply.network.AiService
import com.matrix.autoreply.network.model.ai.AiModel
import com.matrix.autoreply.network.model.ai.AiModelResponse
import com.matrix.autoreply.network.model.ai.GoogleModelResponse
import com.matrix.autoreply.preferences.PreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AiHelper {
    private const val TAG = "AiHelper"
    private const val CACHE_DURATION_MS = 60 * 60 * 1000L // 1 hour
    
    private var cachedModels: List<AiModel>? = null
    private var lastModelsFetchTime: Long = 0
    private var lastProvider: String? = null
    
    interface ModelFetchCallback {
        fun onModelsFetched(models: List<AiModel>)
        fun onError(errorMessage: String)
    }
    
    private fun getBaseUrl(provider: String, customUrl: String? = null): String {
        return when (provider) {
            "groq" -> "https://api.groq.com/openai/"
            "openai" -> "https://api.openai.com/"
            "anthropic" -> "https://api.anthropic.com/"
            "google" -> "https://generativelanguage.googleapis.com/"
            "bedrock" -> "https://bedrock-runtime.us-east-1.amazonaws.com/"
            else -> "https://api.groq.com/openai/"
        }
    }
    
    private fun getRetrofitInstance(provider: String, customUrl: String? = null): Retrofit {
        val baseUrl = getBaseUrl(provider, customUrl)
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    fun fetchModels(context: Context, callback: ModelFetchCallback) {
        val preferencesManager = PreferencesManager.getPreferencesInstance(context)
        val provider = preferencesManager?.aiProvider ?: "groq"
        
        // Invalidate cache if provider changed
        if (lastProvider != provider) {
            invalidateCache()
            lastProvider = provider
        }
        
        if (isCacheValid()) {
            cachedModels?.let { callback.onModelsFetched(it) }
            return
        }
        
        when (provider) {
            "groq", "openai" -> fetchOpenAICompatibleModels(context, provider, callback)
            "anthropic" -> fetchAnthropicModels(context, callback)
            "google" -> fetchGoogleModels(context, callback)
            else -> fetchOpenAICompatibleModels(context, provider, callback)
        }
    }
    
    private fun fetchOpenAICompatibleModels(context: Context, provider: String, callback: ModelFetchCallback) {
        val preferencesManager = PreferencesManager.getPreferencesInstance(context)
        val apiKey = preferencesManager?.aiApiKey
        
        if (apiKey.isNullOrEmpty()) {
            callback.onError("API Key required for $provider. Please configure it in settings.")
            return
        }
        
        val service = getRetrofitInstance(provider).create(AiService::class.java)
        val call = service.getModels("Bearer $apiKey")
        
        call.enqueue(object : Callback<AiModelResponse> {
            override fun onResponse(call: Call<AiModelResponse>, response: Response<AiModelResponse>) {
                if (response.isSuccessful && response.body()?.data != null) {
                    cachedModels = response.body()!!.data
                    lastModelsFetchTime = System.currentTimeMillis()
                    Log.i(TAG, "$provider models fetched successfully. Count: ${cachedModels!!.size}")
                    callback.onModelsFetched(cachedModels!!)
                } else {
                    val errorMsg = "Failed to fetch $provider models. Code: ${response.code()}"
                    Log.e(TAG, errorMsg)
                    callback.onError(errorMsg)
                }
            }
            
            override fun onFailure(call: Call<AiModelResponse>, t: Throwable) {
                val errorMsg = "Network error fetching $provider models: ${t.message}"
                Log.e(TAG, errorMsg, t)
                callback.onError(errorMsg)
            }
        })
    }
    
    private fun fetchAnthropicModels(context: Context, callback: ModelFetchCallback) {
        // Anthropic doesn't have a public models endpoint, return hardcoded models
        val anthropicModels = listOf(
            AiModel(id = "claude-3-5-sonnet-20241022", objectType = "model", created = 0, ownedBy = "anthropic"),
            AiModel(id = "claude-3-5-sonnet-20240620", objectType = "model", created = 0, ownedBy = "anthropic"),
            AiModel(id = "claude-3-5-haiku-20241022", objectType = "model", created = 0, ownedBy = "anthropic"),
            AiModel(id = "claude-3-opus-20240229", objectType = "model", created = 0, ownedBy = "anthropic"),
            AiModel(id = "claude-3-sonnet-20240229", objectType = "model", created = 0, ownedBy = "anthropic"),
            AiModel(id = "claude-3-haiku-20240307", objectType = "model", created = 0, ownedBy = "anthropic")
        )
        
        cachedModels = anthropicModels
        lastModelsFetchTime = System.currentTimeMillis()
        Log.i(TAG, "Anthropic models loaded from hardcoded list. Count: ${anthropicModels.size}")
        callback.onModelsFetched(anthropicModels)
    }
    
    private fun fetchGoogleModels(context: Context, callback: ModelFetchCallback) {
        val preferencesManager = PreferencesManager.getPreferencesInstance(context)
        val apiKey = preferencesManager?.aiApiKey
        
        if (apiKey.isNullOrEmpty()) {
            callback.onError("API Key required for Google Gemini. Please configure it in settings.")
            return
        }
        
        val service = getRetrofitInstance("google").create(AiService::class.java)
        val call = service.getGoogleModels(apiKey)
        
        call.enqueue(object : Callback<GoogleModelResponse> {
            override fun onResponse(call: Call<GoogleModelResponse>, response: Response<GoogleModelResponse>) {
                if (response.isSuccessful && response.body()?.models != null) {
                    val googleModels = response.body()!!.models
                        .filter { it.supportedGenerationMethods.contains("generateContent") }
                        .map { googleModel ->
                            val modelId = googleModel.name.substringAfterLast("/")
                            AiModel(
                                id = modelId,
                                objectType = "model",
                                created = 0,
                                ownedBy = "google"
                            )
                        }
                    
                    cachedModels = googleModels
                    lastModelsFetchTime = System.currentTimeMillis()
                    Log.i(TAG, "Google models fetched successfully. Count: ${googleModels.size}")
                    callback.onModelsFetched(googleModels)
                } else {
                    val errorMsg = "Failed to fetch Google models. Code: ${response.code()}"
                    Log.e(TAG, errorMsg)
                    callback.onError(errorMsg)
                }
            }
            
            override fun onFailure(call: Call<GoogleModelResponse>, t: Throwable) {
                val errorMsg = "Network error fetching Google models: ${t.message}"
                Log.e(TAG, errorMsg, t)
                callback.onError(errorMsg)
            }
        })
    }
    
    
    private fun isCacheValid(): Boolean {
        return cachedModels != null && 
               (System.currentTimeMillis() - lastModelsFetchTime < CACHE_DURATION_MS)
    }
    
    fun invalidateCache() {
        cachedModels = null
        lastModelsFetchTime = 0
        lastProvider = null
        Log.d(TAG, "AI models cache invalidated")
    }
}

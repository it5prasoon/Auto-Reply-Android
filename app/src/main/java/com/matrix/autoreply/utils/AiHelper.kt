package com.matrix.autoreply.utils

import android.content.Context
import android.util.Log
import com.matrix.autoreply.network.AiService
import com.matrix.autoreply.network.model.ai.AiModel
import com.matrix.autoreply.network.model.ai.AiModelResponse
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
    
    interface ModelFetchCallback {
        fun onModelsFetched(models: List<AiModel>)
        fun onError(errorMessage: String)
    }
    
    private fun getRetrofitInstance(provider: String = "groq"): Retrofit {
        val baseUrl = when (provider) {
            "groq" -> "https://api.groq.com/openai/"
            "openai" -> "https://api.openai.com/"
            else -> "https://api.groq.com/openai/"
        }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    fun fetchModels(context: Context, callback: ModelFetchCallback) {
        if (isCacheValid()) {
            cachedModels?.let { callback.onModelsFetched(it) }
            return
        }
        
        val preferencesManager = PreferencesManager.getPreferencesInstance(context)
        val apiKey = preferencesManager?.aiApiKey
        
        if (apiKey.isNullOrEmpty()) {
            callback.onError("AI API Key not set. Please configure it in settings.")
            return
        }
        
        val provider = preferencesManager?.aiProvider ?: "groq"
        val service = getRetrofitInstance(provider).create(AiService::class.java)
        val call = service.getModels("Bearer $apiKey")
        
        call.enqueue(object : Callback<AiModelResponse> {
            override fun onResponse(call: Call<AiModelResponse>, response: Response<AiModelResponse>) {
                if (response.isSuccessful() && response.body()?.data != null) {
                    cachedModels = response.body()!!.data
                    lastModelsFetchTime = System.currentTimeMillis()
                    Log.i(TAG, "Models fetched successfully. Count: ${cachedModels!!.size}")
                    callback.onModelsFetched(cachedModels!!)
                } else {
                    val errorMsg = "Failed to fetch models. Code: ${response.code()}"
                    Log.e(TAG, errorMsg)
                    callback.onError(errorMsg)
                }
            }
            
            override fun onFailure(call: Call<AiModelResponse>, t: Throwable) {
                val errorMsg = "Network error: ${t.message}"
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
        Log.d(TAG, "AI models cache invalidated")
    }
}
package com.matrix.autoreply.utils

import android.content.Context
import android.util.Log
import com.matrix.autoreply.network.AiService
import com.matrix.autoreply.network.model.ai.AiMessage
import com.matrix.autoreply.network.model.ai.AiRequest
import com.matrix.autoreply.network.model.ai.AiResponse
import com.matrix.autoreply.network.model.ai.AiErrorResponse
import com.matrix.autoreply.preferences.PreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson

object AiReplyHandler {
    private const val TAG = "AiReplyHandler"
    private const val BASE_URL = "https://api.openai.com/"
    
    interface AiReplyCallback {
        fun onReplyGenerated(reply: String)
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
    
    fun generateReply(context: Context, incomingMessage: String, callback: AiReplyCallback) {
        val preferencesManager = PreferencesManager.getPreferencesInstance(context) ?: return
        
        if (!preferencesManager.isAiEnabled) {
            callback.onError("AI replies are disabled")
            return
        }
        
        val apiKey = preferencesManager.aiApiKey
        if (apiKey.isNullOrEmpty()) {
            callback.onError("AI API Key not configured")
            return
        }
        
        val provider = preferencesManager.aiProvider
        val selectedModel = preferencesManager.aiSelectedModel
        val baseSystemMessage = preferencesManager.aiSystemMessage
        val systemMessage = "$baseSystemMessage Do not include <think> or <thinking> tags in your response. Only provide the direct reply."
        val messages = listOf(
            AiMessage("system", systemMessage),
            AiMessage("user", incomingMessage)
        )
        
        val request = AiRequest(
            model = selectedModel,
            messages = messages,
            maxTokens = 150,
            temperature = 0.7
        )
        
        val service = getRetrofitInstance(provider).create(AiService::class.java)
        val call = service.getChatCompletion("Bearer $apiKey", request)
        
        call.enqueue(object : Callback<AiResponse> {
            override fun onResponse(call: Call<AiResponse>, response: Response<AiResponse>) {
                if (response.isSuccessful() && response.body() != null) {
                    val aiResponse = response.body()!!
                    if (aiResponse.choices.isNotEmpty() && 
                        aiResponse.choices[0].message.content.isNotEmpty()) {
                        
                        val rawReply = aiResponse.choices[0].message.content.trim()
                        val cleanReply = cleanThinkTags(rawReply)
                        Log.i(TAG, "AI reply generated successfully with model $selectedModel")
                        callback.onReplyGenerated(cleanReply)
                        
                        // Clear any previous errors
                        preferencesManager.clearAiLastError()
                    } else {
                        handleError(response, selectedModel, incomingMessage, preferencesManager, callback)
                    }
                } else {
                    handleError(response, selectedModel, incomingMessage, preferencesManager, callback)
                }
            }
            
            override fun onFailure(call: Call<AiResponse>, t: Throwable) {
                val errorMsg = "Network error: ${t.message}"
                Log.e(TAG, errorMsg, t)
                callback.onError(errorMsg)
            }
        })
    }
    
    private fun handleError(
        response: Response<AiResponse>,
        originalModel: String,
        incomingMessage: String,
        preferencesManager: PreferencesManager,
        callback: AiReplyCallback
    ) {
        val errorBody = response.errorBody()?.string()
        var aiError: AiErrorResponse? = null
        
        try {
            if (!errorBody.isNullOrEmpty()) {
                aiError = Gson().fromJson(errorBody, AiErrorResponse::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing AI error response", e)
        }
        
        val errorCode = aiError?.error?.code
        val errorMessage = aiError?.error?.message ?: "Unknown error"
        val detailedError = "AI API failed with model $originalModel. Code: ${response.code()}. Message: $errorMessage"
        
        Log.e(TAG, detailedError)
        
        when {
            errorCode == "insufficient_quota" -> {
                val userError = "AI: Insufficient quota. Please check your plan and billing."
                preferencesManager.saveAiLastError(userError, System.currentTimeMillis())
                callback.onError(userError)
            }
            response.code() == 401 -> {
                val userError = "AI: Invalid API Key. Please check your API Key in settings."
                preferencesManager.saveAiLastError(userError, System.currentTimeMillis())
                callback.onError(userError)
            }
            response.code() == 400 || response.code() == 404 || errorCode == "model_not_found" -> {
                // Retry with default model
                retryWithDefaultModel(incomingMessage, preferencesManager, callback)
            }
            else -> {
                callback.onError("AI service temporarily unavailable")
            }
        }
    }
    
    private fun retryWithDefaultModel(
        incomingMessage: String,
        preferencesManager: PreferencesManager,
        callback: AiReplyCallback
    ) {
        val provider = preferencesManager.aiProvider
        val defaultModel = when (provider) {
            "groq" -> "llama-3.1-70b-versatile"
            "openai" -> "gpt-3.5-turbo"
            else -> "llama-3.1-70b-versatile"
        }
        
        Log.w(TAG, "Retrying with default model $defaultModel for provider $provider")
        
        val apiKey = preferencesManager.aiApiKey ?: return
        val baseSystemMessage = preferencesManager.aiSystemMessage
        val systemMessage = "$baseSystemMessage Do not include <think> or <thinking> tags in your response. Only provide the direct reply."
        val messages = listOf(
            AiMessage("system", systemMessage),
            AiMessage("user", incomingMessage)
        )
        
        val request = AiRequest(
            model = defaultModel,
            messages = messages,
            maxTokens = 150,
            temperature = 0.7
        )
        
        val service = getRetrofitInstance(preferencesManager.aiProvider).create(AiService::class.java)
        val call = service.getChatCompletion("Bearer $apiKey", request)
        
        call.enqueue(object : Callback<AiResponse> {
            override fun onResponse(call: Call<AiResponse>, response: Response<AiResponse>) {
                if (response.isSuccessful() && response.body() != null) {
                    val aiResponse = response.body()!!
                    if (aiResponse.choices.isNotEmpty() && 
                        aiResponse.choices[0].message.content.isNotEmpty()) {
                        
                        val rawReply = aiResponse.choices[0].message.content.trim()
                        val cleanReply = cleanThinkTags(rawReply)
                        Log.i(TAG, "AI retry successful with default model $defaultModel")
                        callback.onReplyGenerated(cleanReply)
                    } else {
                        callback.onError("AI service failed to generate response")
                    }
                } else {
                    callback.onError("AI service temporarily unavailable")
                }
            }
            
            override fun onFailure(call: Call<AiResponse>, t: Throwable) {
                Log.e(TAG, "AI retry failed", t)
                callback.onError("AI service temporarily unavailable")
            }
        })
    }
    
    private fun cleanThinkTags(text: String): String {
        var result = text
        
        // Simple string-based removal as fallback
        val thinkStart = result.indexOf("<think>")
        if (thinkStart != -1) {
            val thinkEnd = result.indexOf("</think>", thinkStart)
            if (thinkEnd != -1) {
                result = result.removeRange(thinkStart, thinkEnd + 8)
            }
        }
        
        val thinkingStart = result.indexOf("<thinking>")
        if (thinkingStart != -1) {
            val thinkingEnd = result.indexOf("</thinking>", thinkingStart)
            if (thinkingEnd != -1) {
                result = result.removeRange(thinkingStart, thinkingEnd + 11)
            }
        }
        
        return result.trim()
    }
}
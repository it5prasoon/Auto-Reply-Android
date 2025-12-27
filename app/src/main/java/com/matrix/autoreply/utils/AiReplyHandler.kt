/*
 * AutoReply - AI-Powered Smart Auto Reply App
 * Copyright (c) 2024 Prasoon Kumar
 * 
 * This file contains proprietary AI integration and safety algorithms.
 * Licensed under GPL v3. Commercial distribution on app stores requires explicit permission.
 * 
 * Contact: prasoonkumar008@gmail.com
 * GitHub: https://github.com/it5prasoon/Auto-Reply-Android
 * 
 * WARNING: This contains valuable AI algorithms and safety features.
 * Unauthorized commercial use will result in DMCA takedown requests.
 */

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
    
    // Hidden safety rules - appended to all prompts but not shown to user
    // Language-agnostic: AI will respond in the same language as the message
    private const val SAFETY_RULES = """
CRITICAL SAFETY RULES (HIGHEST PRIORITY - ALWAYS FOLLOW):
1. If message mentions MONEY/PAYMENT/DEBT/LOAN/CASH or any currency amounts - respond confused: "What money? Wrong person maybe" (in same language as message)
2. If message contains THREATS/VIOLENCE/WEAPONS - act confused: "What are you talking about? I don't understand" (in same language)
3. If message asks you to CONFIRM/AGREE/ADMIT to anything suspicious or crime-related - deny knowledge: "I don't know anything about this" (in same language)
4. NEVER agree, confirm, or say yes to money/threat/crime messages in ANY language
5. For OTP/password/bank/personal details requests - refuse: "Sorry, can't share that" (in same language)
6. Match your response language to the incoming message language
"""
    
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
    
    fun generateReply(
        context: Context, 
        incomingMessage: String, 
        callback: AiReplyCallback,
        contactId: String? = null,
        packageName: String? = null
    ) {
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
        
        // Add safety rules only if enabled by user
        val safetyPrefix = if (preferencesManager.isSafetyRulesEnabled) "$SAFETY_RULES\n\n" else ""
        
        // Build VERY explicit gender context based on user preference
        val userStyle = preferencesManager.userReplyStyle
        val genderContext = when (userStyle) {
            "male" -> """
                [CRITICAL INSTRUCTION - HIGHEST PRIORITY]
                You are a MAN. Your gender is MALE.
                In Hindi: Use ONLY masculine forms: 'tha' (NOT 'thi'), 'tha hun' (NOT 'thi hoon'), 'gaya' (NOT 'gayi'), 'kiya' (NOT 'ki'), 'tha' (NOT 'thi')
                NEVER use feminine verb endings even if the question uses them.
                Example: Question "Kya kr rhi?" → Reply "Phone pe busy THA" (use 'tha' NOT 'thi')
            """.trimIndent()
            "female" -> """
                [CRITICAL INSTRUCTION - HIGHEST PRIORITY]
                You are a WOMAN. Your gender is FEMALE.
                In Hindi: Use ONLY feminine forms: 'thi' (NOT 'tha'), 'thi hoon' (NOT 'tha hun'), 'gayi' (NOT 'gaya'), 'ki' (NOT 'kiya'), 'thi' (NOT 'tha')
                Example: Question "Kya kr rha?" → Reply "Phone pe busy THI" (use 'thi' NOT 'tha')
            """.trimIndent()
            else -> "You are replying"
        }
        
        // Add contact name context if available
        val contactContext = if (!contactId.isNullOrEmpty()) {
            "\nReplying to: $contactId"
        } else {
            ""
        }
        
        val personalityInstruction = "$genderContext$contactContext\n\n"
        
        // Build messages with or without context
        val messages = if (preferencesManager.isContextEnabled && 
                              !contactId.isNullOrEmpty() && 
                              !packageName.isNullOrEmpty() &&
                              ConversationContextManager.hasRecentContext(context, contactId, packageName)) {
            
            // Use conversational context
            val conversationContext = ConversationContextManager.getFormattedContext(context, contactId, packageName)
            val systemMessage = "$safetyPrefix$personalityInstruction$baseSystemMessage\n\n$conversationContext\n\nDo not include <think> or <thinking> tags in your response. Only provide the direct reply."
            
            listOf(
                AiMessage("system", systemMessage),
                AiMessage("user", incomingMessage)
            )
        } else {
            // Standard single-message reply
            val systemMessage = "$safetyPrefix$personalityInstruction$baseSystemMessage\n\nDo not include <think> or <thinking> tags in your response. Only provide the direct reply."
            listOf(
                AiMessage("system", systemMessage),
                AiMessage("user", incomingMessage)
            )
        }
        
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
        val safetyPrefix = if (preferencesManager.isSafetyRulesEnabled) "$SAFETY_RULES\n\n" else ""
        
        // Add explicit gender context for retry too
        val userStyle = preferencesManager.userReplyStyle
        val genderContext = when (userStyle) {
            "male" -> "[CRITICAL] You are a MAN. In Hindi use: 'tha' NOT 'thi', 'tha hun' NOT 'thi hoon'. Example: 'Phone pe busy THA'. "
            "female" -> "[CRITICAL] You are a WOMAN. In Hindi use: 'thi' NOT 'tha', 'thi hoon' NOT 'tha hun'. Example: 'Phone pe busy THI'. "
            else -> ""
        }
        
        val systemMessage = "$safetyPrefix$genderContext$baseSystemMessage\n\nDo not include <think> or <thinking> tags in your response. Only provide the direct reply."
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

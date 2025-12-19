package com.matrix.autoreply.utils

import android.content.Context
import android.util.Log
import com.matrix.autoreply.preferences.PreferencesManager
import com.matrix.autoreply.store.database.MessageLogsDB
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages conversational context for AI replies
 * Maintains conversation sessions with configurable time windows
 */
object ConversationContextManager {
    private const val TAG = "ConversationContextManager"
    
    // In-memory session storage for active conversations
    private val activeSessions = ConcurrentHashMap<String, ConversationSession>()
    
    data class ConversationSession(
        val contactId: String,        // Contact identifier (title from notification)
        val packageName: String,      // App package (WhatsApp, Instagram, etc.)
        val messages: MutableList<ContextMessage>,
        var lastActivity: Long,       // Last message timestamp (mutable)
        val sessionStart: Long        // Session start time
    )
    
    data class ContextMessage(
        val content: String,
        val isIncoming: Boolean,      // true = received, false = sent by AutoReply
        val timestamp: Long
    )
    
    /**
     * Add a new incoming message to conversation context
     * Automatically starts new context session if previous one expired
     */
    fun addIncomingMessage(
        context: Context,
        contactId: String,
        packageName: String,
        message: String,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val sessionKey = "$packageName:$contactId"
        
        cleanupExpiredSessions(context)
        
        // Check if we need to start a new context session
        val existingSession = activeSessions[sessionKey]
        val shouldStartNewSession = existingSession == null || isSessionExpired(context, existingSession)
        
        val session = if (shouldStartNewSession) {
            // Start completely fresh context session
            ConversationSession(
                contactId = contactId,
                packageName = packageName,
                messages = mutableListOf(),
                lastActivity = timestamp,
                sessionStart = timestamp
            )
        } else {
            // Continue existing session
            existingSession!!
        }
        
        session.messages.add(
            ContextMessage(
                content = message,
                isIncoming = true,
                timestamp = timestamp
            )
        )
        session.lastActivity = timestamp
        
        // Keep only recent messages to avoid AI token limits
        limitContextSize(session)
        
        activeSessions[sessionKey] = session
        
        if (shouldStartNewSession) {
            Log.d(TAG, "Started new context session for $contactId (previous session expired)")
        }
    }
    
    /**
     * Add an outgoing reply to conversation context
     */
    fun addOutgoingReply(
        context: Context,
        contactId: String,
        packageName: String,
        reply: String,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val sessionKey = "$packageName:$contactId"
        
        activeSessions[sessionKey]?.let { session ->
            session.messages.add(
                ContextMessage(
                    content = reply,
                    isIncoming = false,
                    timestamp = timestamp
                )
            )
            session.lastActivity = timestamp
            
            limitContextSize(session)
        }
    }
    
    /**
     * Get conversation context for AI reply generation
     * Combines in-memory session with recent database history for robust context
     */
    fun getConversationContext(
        context: Context,
        contactId: String,
        packageName: String
    ): List<ContextMessage> {
        val sessionKey = "$packageName:$contactId"
        
        cleanupExpiredSessions(context)
        
        val sessionMessages = activeSessions[sessionKey]?.messages ?: mutableListOf()
        
        // If in-memory session is empty or limited, fetch recent messages from database
        if (sessionMessages.size < 3) {
            try {
                val messageLogsDB = MessageLogsDB.getInstance(context)
                val recentDbMessages = messageLogsDB?.messageLogsDao()?.getMessageLogsWithTitle(contactId) ?: emptyList()
                
                // Filter messages within context window and convert to ContextMessage
                val contextWindowMinutes = getContextWindowMinutes(context)
                val contextWindowMs = contextWindowMinutes * 60 * 1000L
                val cutoffTime = System.currentTimeMillis() - contextWindowMs
                
                val dbContextMessages = recentDbMessages
                    .filter { it.notifArrivedTime >= cutoffTime }
                    .map { messageLog ->
                        ContextMessage(
                            content = messageLog.notifMessage ?: "",
                            isIncoming = true, // Messages from DB are incoming
                            timestamp = messageLog.notifArrivedTime
                        )
                    }
                    .sortedBy { it.timestamp }
                
                // Combine database messages with in-memory session, remove duplicates
                val allMessages = (dbContextMessages + sessionMessages).distinctBy { 
                    "${it.content}-${it.timestamp}" 
                }.sortedBy { it.timestamp }
                
                return allMessages.takeLast(12) // Limit to last 12 messages
            } catch (e: Exception) {
                // Fallback to in-memory only if database fails
                return sessionMessages
            }
        }
        
        return sessionMessages
    }
    
    /**
     * Check if conversation has recent context
     */
    fun hasRecentContext(
        context: Context,
        contactId: String,
        packageName: String
    ): Boolean {
        val sessionKey = "$packageName:$contactId"
        val session = activeSessions[sessionKey] ?: return false
        
        val contextWindowMinutes = getContextWindowMinutes(context)
        val contextWindowMs = contextWindowMinutes * 60 * 1000L
        
        return (System.currentTimeMillis() - session.lastActivity) < contextWindowMs
    }
    
    /**
     * Get conversation context formatted for AI prompt
     */
    fun getFormattedContext(
        context: Context,
        contactId: String,
        packageName: String
    ): String {
        val contextMessages = getConversationContext(context, contactId, packageName)
        
        if (contextMessages.isEmpty()) return ""
        
        val contextBuilder = StringBuilder()
        contextBuilder.append("Recent conversation context:\n")
        
        contextMessages.forEach { msg ->
            val sender = if (msg.isIncoming) "User" else "You"
            contextBuilder.append("$sender: \"${msg.content}\"\n")
        }
        
        contextBuilder.append("\nPlease respond naturally considering this conversation history.")
        return contextBuilder.toString()
    }
    
    /**
     * Clean up expired sessions based on user preferences
     */
    private fun cleanupExpiredSessions(context: Context) {
        val contextWindowMinutes = getContextWindowMinutes(context)
        val contextWindowMs = contextWindowMinutes * 60 * 1000L
        val currentTime = System.currentTimeMillis()
        
        val expiredKeys = activeSessions.filter { (_, session) ->
            (currentTime - session.lastActivity) > contextWindowMs
        }.keys
        
        expiredKeys.forEach { key ->
            activeSessions.remove(key)
        }
    }
    
    /**
     * Limit context size to avoid AI token limits
     */
    private fun limitContextSize(session: ConversationSession) {
        val maxMessages = 12 // Keep last 12 messages max
        
        if (session.messages.size > maxMessages) {
            // Remove oldest messages, keep the most recent
            val messagesToRemove = session.messages.size - maxMessages
            repeat(messagesToRemove) {
                session.messages.removeAt(0)
            }
        }
    }
    
    /**
     * Get user's preferred context window in minutes
     */
    private fun getContextWindowMinutes(context: Context): Int {
        val prefsManager = PreferencesManager.getPreferencesInstance(context)
        return prefsManager?.contextWindowMinutes ?: 20 // Default 20 minutes
    }
    
    /**
     * Clear all conversation contexts (privacy function)
     */
    fun clearAllContexts() {
        activeSessions.clear()
    }
    
    /**
     * Clear context for specific conversation
     */
    fun clearConversationContext(contactId: String, packageName: String) {
        val sessionKey = "$packageName:$contactId"
        activeSessions.remove(sessionKey)
    }
    
    /**
     * Check if a session has expired based on user preferences
     */
    private fun isSessionExpired(context: Context, session: ConversationSession): Boolean {
        val contextWindowMinutes = getContextWindowMinutes(context)
        val contextWindowMs = contextWindowMinutes * 60 * 1000L
        val currentTime = System.currentTimeMillis()
        
        return (currentTime - session.lastActivity) >= contextWindowMs
    }
    
    /**
     * Get active sessions count (for debugging/stats)
     */
    fun getActiveSessionsCount(): Int = activeSessions.size
}

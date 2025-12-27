package com.matrix.autoreply.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.matrix.autoreply.model.CustomRepliesData
import com.matrix.autoreply.preferences.LiveChatPreferencesManager
import com.matrix.autoreply.utils.AiReplyHandler
import kotlinx.coroutines.*

/**
 * Live Chat Accessibility Service
 * Monitors WhatsApp and other messaging apps in real-time and auto-replies
 * using accessibility service capabilities.
 */
class LiveChatAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "LiveChatAccessibility"
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
        private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
        private const val MESSENGER_PACKAGE = "com.facebook.orca"
        private const val INSTAGRAM_PACKAGE = "com.instagram.android"
        
        // EditText resource IDs for different apps
        private const val WHATSAPP_INPUT_FIELD = "entry"
        private const val WHATSAPP_SEND_BUTTON = "send"
        
        // Messenger resource IDs
        private const val MESSENGER_INPUT_FIELD = "composerEditText"
        private const val MESSENGER_SEND_BUTTON = "send_button"
        
        // Instagram resource IDs
        private const val INSTAGRAM_INPUT_FIELD = "row_thread_composer_edittext"
        private const val INSTAGRAM_SEND_BUTTON = "row_thread_composer_button_send"
        
        var isServiceRunning = false
            private set
    }

    private lateinit var preferencesManager: LiveChatPreferencesManager
    private var customRepliesData: CustomRepliesData? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val processedMessages = mutableSetOf<String>()
    private val lastReplyTime = mutableMapOf<String, Long>()
    private val lastProcessedMessage = mutableMapOf<String, String>()
    private val lastSeenMessage = mutableMapOf<String, String>()
    private var lastSentReply: String = ""
    private val handler = Handler(Looper.getMainLooper())
    private var isInitialLoad = true

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        Log.d(TAG, "Live Chat Accessibility Service Connected")

        preferencesManager = LiveChatPreferencesManager.getInstance(this)
        customRepliesData = CustomRepliesData.getInstance(this)

        // Configure accessibility service
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED

            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            
            // Only monitor enabled packages
            packageNames = getEnabledPackages().toTypedArray()
        }
        serviceInfo = info

        Log.d(TAG, "Monitoring packages: ${getEnabledPackages()}")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!preferencesManager.isLiveChatEnabled) {
            return
        }

        val packageName = event.packageName?.toString() ?: return
        
        // Check if this package is enabled for live chat
        if (!preferencesManager.enabledApps.contains(packageName)) {
            return
        }

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                handleChatUpdate(event, packageName)
            }
        }
    }

    private fun handleChatUpdate(event: AccessibilityEvent, packageName: String) {
        try {
            // Get the root node
            val rootNode = rootInActiveWindow ?: return
            
            // Extract contact/chat name from the screen
            val contactName = extractContactName(rootNode, packageName) ?: "Unknown"
            
            // Create conversation ID from package + contact
            val conversationId = "${packageName}_${contactName}"
            
            // Extract the latest incoming message (not sent by us)
            val incomingMessage = extractLatestIncomingMessage(rootNode, packageName) ?: return
            
            // Skip very short messages (likely UI elements)
            if (incomingMessage.length < 3) {
                return
            }
            
            // Check if this is our own sent reply
            if (incomingMessage.trim().equals(lastSentReply.trim(), ignoreCase = true)) {
                Log.d(TAG, "Skipping - this is our own sent message")
                return
            }
            
            // Check if this is the first time we're seeing messages in this chat (just opened)
            val lastSeen = lastSeenMessage[conversationId]
            if (lastSeen == null) {
                // First time seeing this chat - store the message but don't reply
                Log.d(TAG, "First time seeing this chat - storing message without replying")
                lastSeenMessage[conversationId] = incomingMessage
                lastProcessedMessage[conversationId] = incomingMessage
                return
            }
            
            // Check if this is the same message we already saw
            if (lastSeen == incomingMessage) {
                Log.d(TAG, "Skipping - already seen this message")
                return
            }
            
            // Check if this is the same message we already processed
            val lastMsg = lastProcessedMessage[conversationId]
            if (lastMsg == incomingMessage) {
                Log.d(TAG, "Skipping - already processed this exact message")
                return
            }
            
            // Check if this message was recently replied to
            val lastReply = lastReplyTime[conversationId] ?: 0L
            val timeSinceLastReply = System.currentTimeMillis() - lastReply
            
            // Don't reply more than once every 8 seconds per conversation
            if (timeSinceLastReply < 8000) {
                Log.d(TAG, "Skipping - replied recently (${timeSinceLastReply}ms ago)")
                return
            }
            
            // Update last seen message
            lastSeenMessage[conversationId] = incomingMessage
            lastProcessedMessage[conversationId] = incomingMessage

            Log.d(TAG, "New message from $contactName: $incomingMessage")

            // Generate and send reply with context
            serviceScope.launch {
                delay(preferencesManager.replyDelaySeconds * 1000L)
                sendReply(rootNode, packageName, contactName, incomingMessage)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error handling chat update: ${e.message}", e)
        }
    }

    private fun extractContactName(rootNode: AccessibilityNodeInfo, packageName: String): String? {
        try {
            // For WhatsApp, the contact name is usually in a TextView at the top
            val textNodes = findNodesByClassName(rootNode, "android.widget.TextView")
            
            // Look for the first few TextViews which typically contain the contact/chat name
            for (node in textNodes.take(5)) {
                val text = node.text?.toString()
                if (!text.isNullOrEmpty() && text.length in 3..50 && !text.contains(":")) {
                    // Skip if it looks like a timestamp
                    if (!text.matches(Regex(".*\\d{1,2}:\\d{2}.*"))) {
                        return text
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting contact name: ${e.message}")
        }
        
        return null
    }

    private fun extractLatestIncomingMessage(rootNode: AccessibilityNodeInfo, packageName: String): String? {
        try {
            // Find all TextViews that might contain messages
            val textNodes = findNodesByClassName(rootNode, "android.widget.TextView")
            
            if (textNodes.isEmpty()) {
                return null
            }

            // Look for messages in reverse order (latest first)
            // Skip the first few which are usually title/header
            val messageNodes = textNodes.drop(5).takeLast(20)
            
            for (node in messageNodes.reversed()) {
                val text = node.text?.toString()
                
                // Valid message criteria
                if (!text.isNullOrEmpty() && 
                    text.length in 3..500 &&
                    !text.matches(Regex(".*\\d{1,2}:\\d{2}.*")) && // Not a timestamp
                    !text.contains("✓") && // Not a sent status indicator (our messages)
                    !text.contains("✓✓") && // Not a delivered status (our messages)
                    text != lastSentReply.trim()) { // Not our own message
                    
                    // Additional check: try to determine if it's an outgoing message
                    // In WhatsApp, outgoing messages are typically right-aligned
                    // This is a heuristic - we check the parent layout
                    if (!isOutgoingMessage(node)) {
                        return text.trim()
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting message: ${e.message}")
        }
        
        return null
    }
    
    private fun isOutgoingMessage(node: AccessibilityNodeInfo): Boolean {
        try {
            // Check if the message has checkmarks (indicating it's sent by us)
            val text = node.text?.toString() ?: ""
            if (text.contains("✓")) {
                return true
            }
            
            // Try to check parent nodes for alignment or specific class names
            // that indicate outgoing messages
            var parent = node.parent
            var depth = 0
            while (parent != null && depth < 5) {
                val className = parent.className?.toString() ?: ""
                val viewId = parent.viewIdResourceName ?: ""
                
                // WhatsApp specific checks
                if (viewId.contains("outgoing") || 
                    viewId.contains("message_out") ||
                    className.contains("RightAligned")) {
                    return true
                }
                
                parent = parent.parent
                depth++
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if outgoing message: ${e.message}")
        }
        
        return false
    }

    private fun isOwnMessage(rootNode: AccessibilityNodeInfo, message: String): Boolean {
        // Check if this matches our last sent reply
        if (message == lastSentReply && lastSentReply.isNotEmpty()) {
            return true
        }
        
        // Check if message appears multiple times (might be typing indicator or our message)
        var count = 0
        val textNodes = findNodesByClassName(rootNode, "android.widget.TextView")
        for (node in textNodes) {
            if (node.text?.toString() == message) {
                count++
            }
        }
        
        // If message appears more than twice, likely our own or UI element
        if (count > 2) {
            return true
        }
        
        return false
    }

    private suspend fun sendReply(rootNode: AccessibilityNodeInfo, packageName: String, contactName: String, incomingMessage: String) {
        try {
            // Generate reply text with conversation context
            val replyText = if (preferencesManager.isAiEnabled && !preferencesManager.aiApiKey.isNullOrEmpty()) {
                generateAiReply(incomingMessage, contactName, packageName)
            } else {
                getCustomReply()
            }

            Log.d(TAG, "Sending reply to $contactName: $replyText")

            // Store this as our last sent reply
            lastSentReply = replyText.trim()
            
            // Update last reply time for this conversation
            val conversationId = "${packageName}_${contactName}"
            lastReplyTime[conversationId] = System.currentTimeMillis()

            // Type and send the reply
            typeAndSendMessage(rootNode, packageName, replyText)

        } catch (e: Exception) {
            Log.e(TAG, "Error sending reply: ${e.message}", e)
        }
    }

    private suspend fun generateAiReply(incomingMessage: String, contactName: String, packageName: String): String {
        return suspendCancellableCoroutine { continuation ->
            AiReplyHandler.generateReply(
                this,
                incomingMessage,
                object : AiReplyHandler.AiReplyCallback {
                    override fun onReplyGenerated(reply: String) {
                        if (continuation.isActive) {
                            continuation.resume(reply) {}
                        }
                    }

                    override fun onError(errorMessage: String) {
                        Log.w(TAG, "AI reply failed: $errorMessage, using custom reply")
                        if (continuation.isActive) {
                            continuation.resume(getCustomReply()) {}
                        }
                    }
                },
                contactName,
                packageName
            )
        }
    }

    private fun getCustomReply(): String {
        return customRepliesData?.getTextToSendOrElse(null) ?: "Thanks for your message!"
    }

    private fun typeAndSendMessage(rootNode: AccessibilityNodeInfo, packageName: String, message: String) {
        try {
            when (packageName) {
                WHATSAPP_PACKAGE, WHATSAPP_BUSINESS_PACKAGE -> {
                    sendWhatsAppMessage(rootNode, message)
                }
                MESSENGER_PACKAGE -> {
                    sendMessengerMessage(rootNode, message)
                }
                INSTAGRAM_PACKAGE -> {
                    sendInstagramMessage(rootNode, message)
                }
                else -> {
                    // Generic approach for other apps
                    sendGenericMessage(rootNode, message)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error typing message: ${e.message}", e)
        }
    }

    private fun sendWhatsAppMessage(rootNode: AccessibilityNodeInfo, message: String) {
        // Find the input field
        val inputField = findNodeByResourceId(rootNode, WHATSAPP_INPUT_FIELD)
        
        if (inputField != null) {
            // Use clipboard and paste
            pasteTextUsingClipboard(inputField, message)
            
            // Wait a moment for the text to be pasted
            handler.postDelayed({
                // Find and click send button
                val sendButton = findNodeByResourceId(rootNode, WHATSAPP_SEND_BUTTON)
                sendButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    ?: Log.w(TAG, "WhatsApp send button not found")
            }, 500)
        } else {
            Log.w(TAG, "WhatsApp input field not found, trying generic")
            sendGenericMessage(rootNode, message)
        }
    }
    
    private fun sendMessengerMessage(rootNode: AccessibilityNodeInfo, message: String) {
        // Find the input field
        val inputField = findNodeByResourceId(rootNode, MESSENGER_INPUT_FIELD)
        
        if (inputField != null) {
            // Use clipboard and paste
            pasteTextUsingClipboard(inputField, message)
            
            // Wait a moment for the text to be pasted
            handler.postDelayed({
                // Find and click send button
                val sendButton = findNodeByResourceId(rootNode, MESSENGER_SEND_BUTTON)
                sendButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    ?: Log.w(TAG, "Messenger send button not found")
            }, 500)
        } else {
            Log.w(TAG, "Messenger input field not found, trying generic")
            sendGenericMessage(rootNode, message)
        }
    }
    
    private fun sendInstagramMessage(rootNode: AccessibilityNodeInfo, message: String) {
        // Find the input field
        val inputField = findNodeByResourceId(rootNode, INSTAGRAM_INPUT_FIELD)
        
        if (inputField != null) {
            // Use clipboard and paste
            pasteTextUsingClipboard(inputField, message)
            
            // Wait a moment for the text to be pasted
            handler.postDelayed({
                // Find and click send button
                val sendButton = findNodeByResourceId(rootNode, INSTAGRAM_SEND_BUTTON)
                sendButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    ?: Log.w(TAG, "Instagram send button not found")
            }, 500)
        } else {
            Log.w(TAG, "Instagram input field not found, trying generic")
            sendGenericMessage(rootNode, message)
        }
    }

    private fun sendGenericMessage(rootNode: AccessibilityNodeInfo, message: String) {
        // Find any EditText for input
        val editTextNodes = findNodesByClassName(rootNode, "android.widget.EditText")
        
        if (editTextNodes.isNotEmpty()) {
            val inputField = editTextNodes.last() // Usually the input is the last EditText
            pasteTextUsingClipboard(inputField, message)
            
            // Try to find and click send button (usually an ImageButton or Button)
            handler.postDelayed({
                val buttons = findNodesByClassName(rootNode, "android.widget.ImageButton")
                buttons.lastOrNull()?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }, 500)
        }
    }

    private fun pasteTextUsingClipboard(node: AccessibilityNodeInfo, text: String) {
        // Focus on the input field
        node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        
        // Copy text to clipboard
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("reply", text)
        clipboard.setPrimaryClip(clip)
        
        // Paste from clipboard
        node.performAction(AccessibilityNodeInfo.ACTION_PASTE)
    }

    private fun findNodeByResourceId(rootNode: AccessibilityNodeInfo, resourceId: String): AccessibilityNodeInfo? {
        if (rootNode.viewIdResourceName?.contains(resourceId) == true) {
            return rootNode
        }

        for (i in 0 until rootNode.childCount) {
            val child = rootNode.getChild(i) ?: continue
            val result = findNodeByResourceId(child, resourceId)
            if (result != null) {
                return result
            }
        }

        return null
    }

    private fun findNodesByClassName(rootNode: AccessibilityNodeInfo, className: String): List<AccessibilityNodeInfo> {
        val nodes = mutableListOf<AccessibilityNodeInfo>()

        if (rootNode.className?.toString() == className) {
            nodes.add(rootNode)
        }

        for (i in 0 until rootNode.childCount) {
            val child = rootNode.getChild(i) ?: continue
            nodes.addAll(findNodesByClassName(child, className))
        }

        return nodes
    }

    private fun getEnabledPackages(): List<String> {
        val enabledApps = preferencesManager.enabledApps
        return if (enabledApps.isEmpty()) {
            // Default to WhatsApp if nothing is selected
            listOf(WHATSAPP_PACKAGE, WHATSAPP_BUSINESS_PACKAGE)
        } else {
            enabledApps.toList()
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Live Chat Accessibility Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        serviceScope.cancel()
        processedMessages.clear()
        lastReplyTime.clear()
        lastProcessedMessage.clear()
        lastSeenMessage.clear()
        lastSentReply = ""
        isInitialLoad = true
        Log.d(TAG, "Live Chat Accessibility Service Destroyed")
    }
}

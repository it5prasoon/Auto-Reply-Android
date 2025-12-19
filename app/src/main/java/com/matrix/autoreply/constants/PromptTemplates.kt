package com.matrix.autoreply.constants

data class PromptTemplate(
    val id: String,
    val name: String,
    val description: String,
    val prompt: String,
    val icon: String = "🤖"
)

object PromptTemplates {
    
    // Note: Safety rules are added silently by AiReplyHandler - no need to show to users

    val TEMPLATES = listOf(
        PromptTemplate(
            id = "custom",
            name = "Custom Prompt",
            description = "Create your own personalized prompt",
            prompt = "Reply like a real person texting. Keep it natural and match the length of the message. If someone says 'Hi', reply with something casual like 'Hey!'. Short, simple, and human.",
            icon = "✏️"
        ),
        PromptTemplate(
            id = "friendly",
            name = "Friendly & Casual",
            description = "Warm, approachable responses with a friendly tone",
            prompt = "Reply like a friendly friend texting back. Keep it warm, casual, and natural. Match the length of the message. If they say 'Hi', reply with 'Hey!' (just a couple of words).",
            icon = "😊"
        ),
        PromptTemplate(
            id = "professional",
            name = "Professional",
            description = "Formal, business-appropriate responses",
            prompt = "Reply politely and professionally without sounding stiff. Keep responses concise and business-appropriate. Match the message length. If the message is 'Hi', reply with 'Hello!'.",
            icon = "💼"
        ),
        PromptTemplate(
            id = "busy",
            name = "Busy Person",
            description = "Quick acknowledgments when you're occupied",
            prompt = "You're busy and can't chat much right now. Replies should be very short and to the point. If someone says 'Hi', respond with 'Hey! Busy rn, ttyl'. Keep replies under 5-8 words.",
            icon = "⏰"
        ),
        PromptTemplate(
            id = "humorous",
            name = "Humorous & Fun",
            description = "Light-hearted, witty responses with humor",
            prompt = "Reply like a fun, playful friend. Keep it light, casual, and brief. For 'Hi', respond with something like 'Heyyy!' or 'Yo! 👋'. Use emojis sparingly.",
            icon = "😄"
        ),
        PromptTemplate(
            id = "supportive",
            name = "Supportive & Caring",
            description = "Empathetic, understanding responses",
            prompt = "Reply in a warm, caring way like a supportive friend. Keep it brief but kind. If they say 'Hi', respond with 'Hey! 💙'. Sound empathetic, not robotic.",
            icon = "💙"
        ),
        PromptTemplate(
            id = "minimal",
            name = "Minimal Responder",
            description = "Short, to-the-point acknowledgments",
            prompt = "Keep replies extremely short. One to three words only. For 'Hi', reply with 'Hey'. For questions, use simple replies like 'Sure', 'Ok', or 'Got it'.",
            icon = "✓"
        ),
        PromptTemplate(
            id = "enthusiastic",
            name = "Enthusiastic",
            description = "Energetic, positive responses with excitement",
            prompt = "Reply with positive energy and excitement. Keep it short and punchy, don't ramble. For 'Hi', say something like 'Hey!! 🎉' or 'Hiii!'.",
            icon = "🎉"
        ),
        PromptTemplate(
            id = "academic",
            name = "Academic/Student",
            description = "Scholarly, study-focused responses",
            prompt = "Reply like a busy student focused on studying. Keep messages short and practical. If someone says 'Hi', reply with 'Hey! Studying rn'.",
            icon = "📚"
        ),
        PromptTemplate(
            id = "entrepreneur",
            name = "Entrepreneur/Hustler",
            description = "Business-focused, goal-oriented responses",
            prompt = "Reply like a busy entrepreneur with a packed schedule. Be direct, clear, and brief. If the message is 'Hi', respond with 'Hey! In a meeting, ttyl'. Time is valuable.",
            icon = "🚀"
        )
    )
    
    fun getTemplateById(id: String): PromptTemplate? {
        return TEMPLATES.find { it.id == id }
    }
    
    fun getDefaultTemplate(): PromptTemplate {
        return TEMPLATES.first { it.id == "friendly" }
    }
}

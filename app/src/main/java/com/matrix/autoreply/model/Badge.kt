package com.matrix.autoreply.model

/**
 * Achievement Badge model
 */
data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val threshold: Int,
    val rarity: BadgeRarity
)

enum class BadgeRarity {
    COMMON,    // 50, 100
    RARE,      // 250, 500
    EPIC,      // 750, 1000
    LEGENDARY  // 2000+
}

object BadgeRegistry {
    
    val ALL_BADGES = listOf(
        Badge(
            id = "milestone_50",
            title = "Getting Started",
            description = "Sent 50 auto-replies",
            emoji = "🌟",
            threshold = 50,
            rarity = BadgeRarity.COMMON
        ),
        Badge(
            id = "milestone_100",
            title = "Century Club",
            description = "Sent 100 auto-replies",
            emoji = "💯",
            threshold = 100,
            rarity = BadgeRarity.COMMON
        ),
        Badge(
            id = "milestone_250",
            title = "Quarter Master",
            description = "Sent 250 auto-replies",
            emoji = "⚡",
            threshold = 250,
            rarity = BadgeRarity.RARE
        ),
        Badge(
            id = "milestone_500",
            title = "Half-K Hero",
            description = "Sent 500 auto-replies",
            emoji = "🚀",
            threshold = 500,
            rarity = BadgeRarity.RARE
        ),
        Badge(
            id = "milestone_750",
            title = "Elite Automator",
            description = "Sent 750 auto-replies",
            emoji = "💪",
            threshold = 750,
            rarity = BadgeRarity.EPIC
        ),
        Badge(
            id = "milestone_1000",
            title = "Automation King",
            description = "Sent 1,000 auto-replies",
            emoji = "👑",
            threshold = 1000,
            rarity = BadgeRarity.EPIC
        ),
        Badge(
            id = "milestone_2000",
            title = "Reply Ninja",
            description = "Sent 2,000 auto-replies",
            emoji = "🥷",
            threshold = 2000,
            rarity = BadgeRarity.LEGENDARY
        ),
        Badge(
            id = "milestone_5000",
            title = "Time Saver Supreme",
            description = "Sent 5,000 auto-replies",
            emoji = "🎯",
            threshold = 5000,
            rarity = BadgeRarity.LEGENDARY
        ),
        Badge(
            id = "milestone_10000",
            title = "Legendary Automator",
            description = "Sent 10,000 auto-replies",
            emoji = "🏅",
            threshold = 10000,
            rarity = BadgeRarity.LEGENDARY
        )
    )
    
    /**
     * Get badge by milestone threshold
     */
    fun getBadgeByThreshold(threshold: Int): Badge? {
        return ALL_BADGES.firstOrNull { it.threshold == threshold }
    }
    
    /**
     * Get all badges user should have based on reply count
     */
    fun getBadgesForCount(replyCount: Int): List<Badge> {
        return ALL_BADGES.filter { it.threshold <= replyCount }
    }
    
    /**
     * Get next badge to unlock
     */
    fun getNextBadge(replyCount: Int): Badge? {
        return ALL_BADGES.firstOrNull { it.threshold > replyCount }
    }
}

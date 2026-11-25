package com.example.model

enum class SourcePlatform(val label: String) {
    YOUTUBE("YouTube"),
    FACEBOOK("Facebook");

    companion object {
        fun fromRaw(raw: String?): SourcePlatform =
            entries.firstOrNull { it.name == raw } ?: YOUTUBE
    }
}

enum class ContentCategory {
    SELF_BUILDING,
    MOTIVATION,
    ISLAMIC,
    OTHER;

    companion object {
        fun fromRaw(raw: String?): ContentCategory =
            entries.firstOrNull { it.name == raw } ?: SELF_BUILDING
    }
}

data class UserProfile(
    val id: String = "local_user",
    val displayName: String = "Focused Learner",
    val language: String = "en",
    val darkTheme: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val dailyLimitMinutes: Long = 20L,
    val endOfFeedVideoLimit: Long = 10L,
    val pauseDuringPrayer: Boolean = true,
    val cityName: String = "Dhaka",
    val latitude: Double = 23.8103,
    val longitude: Double = 90.4125,
    val selectedInterests: List<String> = emptyList(),
    val streakDays: Long = 1L
)

data class VideoSource(
    val id: String = "",
    val name: String = "",
    val handleOrUrl: String = "",
    val platform: String = SourcePlatform.YOUTUBE.name,
    val category: String = ContentCategory.SELF_BUILDING.name,
    val active: Boolean = true,
    val gradientIndex: Long = 0L
) {
    fun platformEnum(): SourcePlatform = SourcePlatform.fromRaw(platform)
    fun categoryEnum(): ContentCategory = ContentCategory.fromRaw(category)
}

data class SavedVideo(
    val id: String = "",
    val videoId: String = "",
    val title: String = "",
    val channelName: String = "",
    val platform: String = SourcePlatform.YOUTUBE.name,
    val category: String = ContentCategory.SELF_BUILDING.name,
    val youtubeVideoId: String? = null,
    val videoUrl: String = "",
    val viewsCount: Long = 0L,
    val likesCount: Long = 0L,
    val durationSeconds: Long = 60L,
    val note: String = "",
    val gradientIndex: Long = 0L,
    val createdAtMillis: Long = 0L
) {
    fun platformEnum(): SourcePlatform = SourcePlatform.fromRaw(platform)
    fun categoryEnum(): ContentCategory = ContentCategory.fromRaw(category)
}

data class WatchHistoryEntry(
    val id: String = "",
    val videoId: String = "",
    val watchedAtMillis: Long = 0L,
    val notInterested: Boolean = false,
    val reported: Boolean = false
)

data class DailyLog(
    val dateKey: String = "",
    val watchedSeconds: Long = 0L,
    val videosWatchedCount: Long = 0L,
    val quranDone: Boolean = false,
    val prayerDone: Boolean = false,
    val learningDone: Boolean = false,
    val exerciseDone: Boolean = false
)

data class FeedVideo(
    val videoId: String,
    val sourceId: String,
    val titleEn: String,
    val titleBn: String,
    val channelName: String,
    val platform: SourcePlatform,
    val category: ContentCategory,
    val youtubeVideoId: String? = null,
    val videoUrl: String,
    val viewsCount: Long,
    val likesCount: Long,
    val durationSeconds: Int,
    val publishedDaysAgo: Int,
    val gradientIndex: Int,
    val takeawayPromptEn: String,
    val takeawayPromptBn: String
)

data class DailyQuote(
    val sourceTypeEn: String,
    val sourceTypeBn: String,
    val textEn: String,
    val textBn: String,
    val referenceEn: String,
    val referenceBn: String
)

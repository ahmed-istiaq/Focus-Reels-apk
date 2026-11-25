package com.example.data

import com.example.data.local.DailyLogEntity
import com.example.data.local.FocusDao
import com.example.data.local.SavedVideoEntity
import com.example.data.local.UserProfileEntity
import com.example.data.local.VideoSourceEntity
import com.example.data.local.WatchHistoryEntity
import com.example.model.ContentCategory
import com.example.model.DailyLog
import com.example.model.FeedVideo
import com.example.model.SavedVideo
import com.example.model.SourcePlatform
import com.example.model.UserProfile
import com.example.model.VideoSource
import com.example.model.WatchHistoryEntry
import com.example.util.FocusUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class FocusRepository(private val dao: FocusDao) {

    fun observeUserProfile(): Flow<UserProfile?> =
        dao.observeUserProfile().map { it?.toDomain() }

    fun observeSources(): Flow<List<VideoSource>> =
        dao.observeSources().map { list -> list.map { it.toDomain() } }

    fun observeSavedVideos(): Flow<List<SavedVideo>> =
        dao.observeSavedVideos().map { list -> list.map { it.toDomain() } }

    fun observeWatchHistory(): Flow<List<WatchHistoryEntry>> =
        dao.observeWatchHistory().map { list -> list.map { it.toDomain() } }

    fun observeDailyLogs(): Flow<List<DailyLog>> =
        dao.observeDailyLogs().map { list -> list.map { it.toDomain() } }

    suspend fun ensureInitialUserProfile() {
        if (dao.getUserProfileOnce() == null) {
            dao.upsertUserProfile(UserProfileEntity())
        }
    }

    suspend fun completeOnboarding(
        interests: List<ContentCategory>,
        selectedStarters: List<StarterChannelTemplate>,
        dailyLimitMinutes: Int
    ) {
        val current = dao.getUserProfileOnce() ?: UserProfileEntity()
        val updated = current.copy(
            onboardingCompleted = true,
            dailyLimitMinutes = dailyLimitMinutes.coerceIn(5, 120).toLong(),
            selectedInterestsCsv = interests.distinct().joinToString(",") { it.name }
        )
        dao.upsertUserProfile(updated)
        val sourceEntities = selectedStarters.map { starter ->
            VideoSourceEntity(
                id = FocusUtils.sanitizeFirestoreId(starter.id),
                name = starter.name.take(100),
                handleOrUrl = starter.handleOrUrl.take(300),
                platform = starter.platform.name,
                category = starter.category.name,
                active = true,
                gradientIndex = starter.gradientIndex.toLong()
            )
        }
        dao.upsertSources(sourceEntities)
    }

    suspend fun updateUserSettings(
        language: String? = null,
        darkTheme: Boolean? = null,
        dailyLimitMinutes: Int? = null,
        endOfFeedVideoLimit: Int? = null,
        pauseDuringPrayer: Boolean? = null,
        cityName: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        val current = dao.getUserProfileOnce() ?: UserProfileEntity()
        val updated = current.copy(
            language = language?.takeIf { it in listOf("en", "bn") } ?: current.language,
            darkTheme = darkTheme ?: current.darkTheme,
            dailyLimitMinutes = dailyLimitMinutes?.coerceIn(5, 120)?.toLong() ?: current.dailyLimitMinutes,
            endOfFeedVideoLimit = endOfFeedVideoLimit?.coerceIn(0, 50)?.toLong() ?: current.endOfFeedVideoLimit,
            pauseDuringPrayer = pauseDuringPrayer ?: current.pauseDuringPrayer,
            cityName = cityName?.take(60) ?: current.cityName,
            latitude = latitude?.coerceIn(-90.0, 90.0) ?: current.latitude,
            longitude = longitude?.coerceIn(-180.0, 180.0) ?: current.longitude
        )
        dao.upsertUserProfile(updated)
    }

    suspend fun addSource(
        name: String,
        handleOrUrl: String,
        platform: SourcePlatform,
        category: ContentCategory,
        gradientIndex: Int
    ) {
        val cleanUrl = handleOrUrl.trim().take(300)
        if (cleanUrl.isEmpty()) return
        val cleanName = name.trim().ifBlank { FocusUtils.inferChannelName(cleanUrl, "Focus Channel") }.take(100)
        val id = "src_${UUID.randomUUID().toString().take(8)}"
        dao.upsertSource(
            VideoSourceEntity(
                id = id,
                name = cleanName,
                handleOrUrl = cleanUrl,
                platform = platform.name,
                category = category.name,
                active = true,
                gradientIndex = gradientIndex.coerceIn(0, 7).toLong()
            )
        )
    }

    suspend fun toggleSourceActive(sourceId: String, active: Boolean) {
        dao.updateSourceActive(sourceId, active)
    }

    suspend fun deleteSource(sourceId: String) {
        dao.deleteSource(sourceId)
    }

    suspend fun saveVideo(video: FeedVideo, language: String, initialNote: String = "") {
        val docId = FocusUtils.sanitizeFirestoreId(video.videoId)
        val title = (if (language == "bn") video.titleBn else video.titleEn).take(200)
        dao.upsertSavedVideo(
            SavedVideoEntity(
                id = docId,
                videoId = video.videoId.take(120),
                title = title.ifBlank { "Focused Reel" },
                channelName = video.channelName.take(100).ifBlank { "Focus Channel" },
                platform = video.platform.name,
                category = video.category.name,
                youtubeVideoId = video.youtubeVideoId?.take(32),
                videoUrl = video.videoUrl.take(300),
                viewsCount = video.viewsCount.coerceAtLeast(0L),
                likesCount = video.likesCount.coerceAtLeast(0L),
                durationSeconds = video.durationSeconds.coerceIn(1, 3600).toLong(),
                note = initialNote.take(2000),
                gradientIndex = video.gradientIndex.coerceIn(0, 7).toLong(),
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateSavedVideoNote(
        savedId: String,
        note: String,
        category: ContentCategory
    ): Boolean {
        dao.updateSavedNote(savedId, note.take(2000), category.name)
        return true
    }

    suspend fun removeSavedVideo(savedId: String) {
        dao.deleteSavedVideo(savedId)
    }

    suspend fun recordWatchEvent(
        videoId: String,
        notInterested: Boolean = false,
        reported: Boolean = false
    ) {
        val docId = FocusUtils.sanitizeFirestoreId(videoId)
        dao.upsertWatchHistory(
            WatchHistoryEntity(
                id = docId,
                videoId = videoId.take(120),
                watchedAtMillis = System.currentTimeMillis(),
                notInterested = notInterested,
                reported = reported
            )
        )
    }

    suspend fun upsertDailyLog(
        dateKey: String,
        watchedSeconds: Long,
        videosWatchedCount: Long,
        quranDone: Boolean,
        prayerDone: Boolean,
        learningDone: Boolean,
        exerciseDone: Boolean
    ) {
        dao.upsertDailyLog(
            DailyLogEntity(
                dateKey = dateKey,
                watchedSeconds = watchedSeconds.coerceIn(0L, 86400L),
                videosWatchedCount = videosWatchedCount.coerceIn(0L, 1000L),
                quranDone = quranDone,
                prayerDone = prayerDone,
                learningDone = learningDone,
                exerciseDone = exerciseDone
            )
        )
    }
}

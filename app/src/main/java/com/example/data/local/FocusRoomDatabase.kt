package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.DailyLog
import com.example.model.SavedVideo
import com.example.model.UserProfile
import com.example.model.VideoSource
import com.example.model.WatchHistoryEntry
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "local_user",
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
    val selectedInterestsCsv: String = "SELF_BUILDING,ISLAMIC",
    val streakDays: Long = 1L
) {
    fun toDomain(): UserProfile = UserProfile(
        id = id,
        displayName = displayName,
        language = language,
        darkTheme = darkTheme,
        onboardingCompleted = onboardingCompleted,
        dailyLimitMinutes = dailyLimitMinutes,
        endOfFeedVideoLimit = endOfFeedVideoLimit,
        pauseDuringPrayer = pauseDuringPrayer,
        cityName = cityName,
        latitude = latitude,
        longitude = longitude,
        selectedInterests = selectedInterestsCsv.split(",").filter { it.isNotBlank() },
        streakDays = streakDays
    )
}

@Entity(tableName = "video_sources")
data class VideoSourceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val handleOrUrl: String,
    val platform: String,
    val category: String,
    val active: Boolean,
    val gradientIndex: Long
) {
    fun toDomain(): VideoSource = VideoSource(
        id = id,
        name = name,
        handleOrUrl = handleOrUrl,
        platform = platform,
        category = category,
        active = active,
        gradientIndex = gradientIndex
    )
}

@Entity(tableName = "saved_videos")
data class SavedVideoEntity(
    @PrimaryKey val id: String,
    val videoId: String,
    val title: String,
    val channelName: String,
    val platform: String,
    val category: String,
    val youtubeVideoId: String?,
    val videoUrl: String,
    val viewsCount: Long,
    val likesCount: Long,
    val durationSeconds: Long,
    val note: String,
    val gradientIndex: Long,
    val createdAtMillis: Long
) {
    fun toDomain(): SavedVideo = SavedVideo(
        id = id,
        videoId = videoId,
        title = title,
        channelName = channelName,
        platform = platform,
        category = category,
        youtubeVideoId = youtubeVideoId,
        videoUrl = videoUrl,
        viewsCount = viewsCount,
        likesCount = likesCount,
        durationSeconds = durationSeconds,
        note = note,
        gradientIndex = gradientIndex,
        createdAtMillis = createdAtMillis
    )
}

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val id: String,
    val videoId: String,
    val watchedAtMillis: Long,
    val notInterested: Boolean,
    val reported: Boolean
) {
    fun toDomain(): WatchHistoryEntry = WatchHistoryEntry(
        id = id,
        videoId = videoId,
        watchedAtMillis = watchedAtMillis,
        notInterested = notInterested,
        reported = reported
    )
}

@Entity(tableName = "daily_logs")
data class DailyLogEntity(
    @PrimaryKey val dateKey: String,
    val watchedSeconds: Long,
    val videosWatchedCount: Long,
    val quranDone: Boolean,
    val prayerDone: Boolean,
    val learningDone: Boolean,
    val exerciseDone: Boolean
) {
    fun toDomain(): DailyLog = DailyLog(
        dateKey = dateKey,
        watchedSeconds = watchedSeconds,
        videosWatchedCount = videosWatchedCount,
        quranDone = quranDone,
        prayerDone = prayerDone,
        learningDone = learningDone,
        exerciseDone = exerciseDone
    )
}

@Dao
interface FocusDao {
    @Query("SELECT * FROM user_profile WHERE id = 'local_user' LIMIT 1")
    fun observeUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 'local_user' LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM video_sources ORDER BY LOWER(name) ASC")
    fun observeSources(): Flow<List<VideoSourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSource(source: VideoSourceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSources(sources: List<VideoSourceEntity>)

    @Query("UPDATE video_sources SET active = :active WHERE id = :sourceId")
    suspend fun updateSourceActive(sourceId: String, active: Boolean)

    @Query("DELETE FROM video_sources WHERE id = :sourceId")
    suspend fun deleteSource(sourceId: String)

    @Query("SELECT * FROM saved_videos ORDER BY createdAtMillis DESC")
    fun observeSavedVideos(): Flow<List<SavedVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSavedVideo(video: SavedVideoEntity)

    @Query("UPDATE saved_videos SET note = :note, category = :category WHERE id = :savedId")
    suspend fun updateSavedNote(savedId: String, note: String, category: String)

    @Query("DELETE FROM saved_videos WHERE id = :savedId")
    suspend fun deleteSavedVideo(savedId: String)

    @Query("SELECT * FROM watch_history")
    fun observeWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchHistory(entry: WatchHistoryEntity)

    @Query("SELECT * FROM daily_logs")
    fun observeDailyLogs(): Flow<List<DailyLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyLog(log: DailyLogEntity)
}

@Database(
    entities = [
        UserProfileEntity::class,
        VideoSourceEntity::class,
        SavedVideoEntity::class,
        WatchHistoryEntity::class,
        DailyLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FocusDatabase : RoomDatabase() {
    abstract fun focusDao(): FocusDao

    companion object {
        @Volatile
        private var INSTANCE: FocusDatabase? = null

        fun getInstance(context: Context): FocusDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FocusDatabase::class.java,
                    "focus_reels.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

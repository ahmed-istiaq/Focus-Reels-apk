package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.CuratedCatalog
import com.example.data.FocusRepository
import com.example.data.StarterChannelTemplate
import com.example.data.local.FocusDatabase
import com.example.model.ContentCategory
import com.example.model.DailyLog
import com.example.model.FeedVideo
import com.example.model.SavedVideo
import com.example.model.SourcePlatform
import com.example.model.UserProfile
import com.example.model.VideoSource
import com.example.model.WatchHistoryEntry
import com.example.util.FocusUtils
import com.example.util.PrayerStatus
import com.example.util.PrayerTimeCalculator
import com.example.util.VideoRanker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MainTab {
    FEED, SOURCES, SAVED, STATS
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class FocusDashboardData(
    val profile: UserProfile,
    val sources: List<VideoSource>,
    val savedVideos: List<SavedVideo>,
    val watchHistory: List<WatchHistoryEntry>,
    val dailyLogs: List<DailyLog>
)

data class FocusLocalUiState(
    val activeTab: MainTab = MainTab.FEED,
    val selectedCategory: ContentCategory? = null,
    val showDailyIntentionCard: Boolean = true,
    val sessionWatchedSeconds: Int = 0,
    val sessionVideosCount: Int = 0,
    val showBreakReminder: Boolean = false,
    val showEndOfFeedStop: Boolean = false,
    val prayerPauseDismissed: Boolean = false,
    val isFeedPlaying: Boolean = false,
    val isMuted: Boolean = false,
    val customYoutubeVideo: FeedVideo? = null,
    val rankedFeed: List<FeedVideo> = emptyList(),
    val statusMessage: String? = null
)

class FocusViewModel(
    private val repository: FocusRepository
) : ViewModel() {

    private val _localState = MutableStateFlow(FocusLocalUiState())
    val localState: StateFlow<FocusLocalUiState> = _localState.asStateFlow()

    private val _prayerStatus = MutableStateFlow(
        PrayerTimeCalculator.calculate(23.8103, 90.4125)
    )
    val prayerStatus: StateFlow<PrayerStatus> = _prayerStatus.asStateFlow()

    private var timerJob: Job? = null
    private var lastPersistedSeconds: Long = -1L
    private var syncedTodaySeconds: Long = 0L
    private var syncedTodayVideos: Long = 0L
    private var latestTodayLog: DailyLog? = null
    private var latestProfile: UserProfile? = null
    private var lastSourcesSignature: String = ""

    val dashboardState: StateFlow<UiState<FocusDashboardData>> = combine(
        repository.observeUserProfile(),
        repository.observeSources(),
        repository.observeSavedVideos(),
        repository.observeWatchHistory(),
        repository.observeDailyLogs()
    ) { profile, sources, saved, history, logs ->
        val resolvedProfile = profile ?: UserProfile()
        latestProfile = resolvedProfile
        val todayKey = FocusUtils.todayDateKey()
        val todayLog = logs.firstOrNull { it.dateKey == todayKey }
        latestTodayLog = todayLog
        if (todayLog != null && todayLog.watchedSeconds > syncedTodaySeconds) {
            syncedTodaySeconds = todayLog.watchedSeconds
            syncedTodayVideos = todayLog.videosWatchedCount
        }
        _prayerStatus.value = PrayerTimeCalculator.calculate(
            resolvedProfile.latitude,
            resolvedProfile.longitude
        )

        val signature = sources.joinToString("|") { "${it.id}:${it.active}:${it.category}" } +
            "::${_localState.value.selectedCategory}"
        if (signature != lastSourcesSignature || _localState.value.rankedFeed.isEmpty()) {
            lastSourcesSignature = signature
            rebuildRankedFeed(sources, history, _localState.value.selectedCategory)
        }

        UiState.Success(
            FocusDashboardData(
                profile = resolvedProfile,
                sources = sources,
                savedVideos = saved,
                watchHistory = history,
                dailyLogs = logs
            )
        ) as UiState<FocusDashboardData>
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading
    )

    init {
        viewModelScope.launch {
            repository.ensureInitialUserProfile()
        }
        startTimerLoop()
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val ui = _localState.value
                val profile = latestProfile ?: continue
                val limitSeconds = profile.dailyLimitMinutes * 60L
                val isPrayerBlocked = profile.pauseDuringPrayer &&
                    _prayerStatus.value.activePrayerWindow != null &&
                    !ui.prayerPauseDismissed
                val isLimitReached = syncedTodaySeconds >= limitSeconds

                if (ui.activeTab == MainTab.FEED &&
                    !ui.showDailyIntentionCard &&
                    !ui.showBreakReminder &&
                    !ui.showEndOfFeedStop &&
                    !isPrayerBlocked &&
                    !isLimitReached &&
                    ui.isFeedPlaying
                ) {
                    syncedTodaySeconds += 1L
                    val nextSessionSecs = ui.sessionWatchedSeconds + 1
                    val shouldBreak = nextSessionSecs > 0 && nextSessionSecs % 600 == 0
                    _localState.update {
                        it.copy(
                            sessionWatchedSeconds = nextSessionSecs,
                            showBreakReminder = it.showBreakReminder || shouldBreak,
                            isFeedPlaying = if (shouldBreak) false else it.isFeedPlaying
                        )
                    }
                    if (syncedTodaySeconds - lastPersistedSeconds >= 10L || syncedTodaySeconds >= limitSeconds) {
                        persistTodayProgress()
                    }
                }
            }
        }
    }

    private fun persistTodayProgress() {
        lastPersistedSeconds = syncedTodaySeconds
        val today = FocusUtils.todayDateKey()
        val log = latestTodayLog
        viewModelScope.launch {
            repository.upsertDailyLog(
                dateKey = today,
                watchedSeconds = syncedTodaySeconds,
                videosWatchedCount = syncedTodayVideos,
                quranDone = log?.quranDone ?: false,
                prayerDone = log?.prayerDone ?: false,
                learningDone = log?.learningDone ?: false,
                exerciseDone = log?.exerciseDone ?: false
            )
        }
    }

    fun getEffectiveTodaySeconds(): Long = syncedTodaySeconds

    private fun rebuildRankedFeed(
        sources: List<VideoSource>,
        history: List<WatchHistoryEntry>,
        category: ContentCategory?
    ) {
        val pool = CuratedCatalog.buildFeedForSources(sources)
        val ranked = VideoRanker.rankFeed(
            allVideos = pool,
            sources = sources,
            history = history,
            selectedCategory = category
        )
        _localState.update { it.copy(rankedFeed = ranked) }
    }

    fun refreshFeed(sources: List<VideoSource>, history: List<WatchHistoryEntry>) {
        rebuildRankedFeed(sources, history, _localState.value.selectedCategory)
    }

    fun selectTab(tab: MainTab) {
        _localState.update {
            it.copy(
                activeTab = tab,
                isFeedPlaying = if (tab == MainTab.FEED && !it.showDailyIntentionCard) it.isFeedPlaying else false
            )
        }
    }

    fun selectCategory(
        category: ContentCategory?,
        sources: List<VideoSource>,
        history: List<WatchHistoryEntry>
    ) {
        _localState.update { it.copy(selectedCategory = category) }
        rebuildRankedFeed(sources, history, category)
    }

    fun beginFocusedSession() {
        _localState.update { it.copy(showDailyIntentionCard = false, isFeedPlaying = true) }
    }

    fun setFeedPlaying(playing: Boolean) {
        _localState.update { it.copy(isFeedPlaying = playing) }
    }

    fun toggleMute() {
        _localState.update { it.copy(isMuted = !it.isMuted) }
    }

    fun dismissBreakAndContinue() {
        _localState.update { it.copy(showBreakReminder = false, isFeedPlaying = true) }
    }

    fun takeBreakFromReminder() {
        _localState.update {
            it.copy(showBreakReminder = false, isFeedPlaying = false, activeTab = MainTab.STATS)
        }
        persistTodayProgress()
    }

    fun continuePastEndOfFeed() {
        _localState.update {
            it.copy(showEndOfFeedStop = false, sessionVideosCount = 0, isFeedPlaying = true)
        }
    }

    fun finishSessionFromEndOfFeed() {
        _localState.update {
            it.copy(showEndOfFeedStop = false, isFeedPlaying = false, activeTab = MainTab.STATS)
        }
        persistTodayProgress()
    }

    fun dismissPrayerPauseForNow() {
        _localState.update { it.copy(prayerPauseDismissed = true, isFeedPlaying = true) }
    }

    fun onVideoSwipedTo(video: FeedVideo, endOfFeedCap: Int) {
        syncedTodayVideos += 1L
        val nextSessionVideos = _localState.value.sessionVideosCount + 1
        val hitCap = endOfFeedCap > 0 && nextSessionVideos >= endOfFeedCap
        _localState.update {
            it.copy(
                sessionVideosCount = nextSessionVideos,
                showEndOfFeedStop = hitCap,
                isFeedPlaying = !hitCap
            )
        }
        viewModelScope.launch {
            repository.recordWatchEvent(video.videoId)
            persistTodayProgress()
        }
    }

    fun markNotInterested(video: FeedVideo, sources: List<VideoSource>, history: List<WatchHistoryEntry>) {
        viewModelScope.launch {
            repository.recordWatchEvent(video.videoId, notInterested = true)
            _localState.update { state ->
                state.copy(rankedFeed = state.rankedFeed.filterNot { it.videoId == video.videoId })
            }
        }
    }

    fun reportBrokenVideo(video: FeedVideo) {
        viewModelScope.launch {
            repository.recordWatchEvent(video.videoId, reported = true)
            _localState.update { state ->
                state.copy(
                    rankedFeed = state.rankedFeed.filterNot { it.videoId == video.videoId },
                    statusMessage = "Reported broken video"
                )
            }
        }
    }

    fun playPastedYoutubeUrl(rawUrl: String, category: ContentCategory) {
        val videoId = FocusUtils.extractYoutubeVideoId(rawUrl)
        if (videoId == null) {
            _localState.update { it.copy(statusMessage = "Could not recognize a valid YouTube video URL") }
            return
        }
        val customVideo = FeedVideo(
            videoId = "yt_$videoId",
            sourceId = "custom_youtube",
            titleEn = "YouTube Focus Clip ($videoId)",
            titleBn = "YouTube ফোকাস ক্লিপ ($videoId)",
            channelName = "YouTube Link",
            platform = SourcePlatform.YOUTUBE,
            category = category,
            youtubeVideoId = videoId,
            videoUrl = "https://www.youtube.com/watch?v=$videoId",
            viewsCount = 1L,
            likesCount = 1L,
            durationSeconds = 90,
            publishedDaysAgo = 0,
            gradientIndex = 2,
            takeawayPromptEn = "Write down your main insight from this video.",
            takeawayPromptBn = "এই ভিডিও থেকে আপনার মূল শিক্ষা লিখে রাখুন।"
        )
        _localState.update { state ->
            val updated = listOf(customVideo) + state.rankedFeed.filterNot { it.videoId == customVideo.videoId }
            state.copy(
                customYoutubeVideo = customVideo,
                rankedFeed = updated,
                showDailyIntentionCard = false,
                activeTab = MainTab.FEED,
                isFeedPlaying = true
            )
        }
    }

    fun toggleSaveVideo(video: FeedVideo, currentlySaved: SavedVideo?, language: String) {
        viewModelScope.launch {
            if (currentlySaved != null) {
                repository.removeSavedVideo(currentlySaved.id)
            } else {
                repository.saveVideo(video, language)
            }
        }
    }

    fun updateSavedNote(savedId: String, note: String, category: ContentCategory) {
        viewModelScope.launch {
            val saved = repository.updateSavedVideoNote(savedId, note, category)
            if (saved) {
                _localState.update { it.copy(statusMessage = "Saved") }
            }
        }
    }

    fun removeSaved(savedId: String) {
        viewModelScope.launch {
            repository.removeSavedVideo(savedId)
        }
    }

    fun addSource(name: String, url: String, platform: SourcePlatform, category: ContentCategory) {
        viewModelScope.launch {
            val grad = kotlin.math.abs(url.hashCode()) % 8
            repository.addSource(name, url, platform, category, grad)
        }
    }

    fun toggleSourceActive(sourceId: String, active: Boolean) {
        viewModelScope.launch {
            repository.toggleSourceActive(sourceId, active)
        }
    }

    fun deleteSource(sourceId: String) {
        viewModelScope.launch {
            repository.deleteSource(sourceId)
        }
    }

    fun completeOnboarding(
        interests: List<ContentCategory>,
        starters: List<StarterChannelTemplate>,
        dailyLimitMinutes: Int
    ) {
        viewModelScope.launch {
            repository.completeOnboarding(interests, starters, dailyLimitMinutes)
        }
    }

    fun toggleHabit(habitIndex: Int) {
        val today = FocusUtils.todayDateKey()
        val current = latestTodayLog
        val q = (current?.quranDone ?: false).let { if (habitIndex == 0) !it else it }
        val p = (current?.prayerDone ?: false).let { if (habitIndex == 1) !it else it }
        val l = (current?.learningDone ?: false).let { if (habitIndex == 2) !it else it }
        val e = (current?.exerciseDone ?: false).let { if (habitIndex == 3) !it else it }
        viewModelScope.launch {
            repository.upsertDailyLog(
                dateKey = today,
                watchedSeconds = syncedTodaySeconds,
                videosWatchedCount = syncedTodayVideos,
                quranDone = q,
                prayerDone = p,
                learningDone = l,
                exerciseDone = e
            )
        }
    }

    fun markQuranDoneFromLimitScreen() {
        val today = FocusUtils.todayDateKey()
        val current = latestTodayLog
        viewModelScope.launch {
            repository.upsertDailyLog(
                dateKey = today,
                watchedSeconds = syncedTodaySeconds,
                videosWatchedCount = syncedTodayVideos,
                quranDone = true,
                prayerDone = current?.prayerDone ?: false,
                learningDone = current?.learningDone ?: false,
                exerciseDone = current?.exerciseDone ?: false
            )
        }
    }

    fun updateSettings(
        language: String? = null,
        darkTheme: Boolean? = null,
        dailyLimitMinutes: Int? = null,
        endOfFeedVideoLimit: Int? = null,
        pauseDuringPrayer: Boolean? = null,
        cityName: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            repository.updateUserSettings(
                language = language,
                darkTheme = darkTheme,
                dailyLimitMinutes = dailyLimitMinutes,
                endOfFeedVideoLimit = endOfFeedVideoLimit,
                pauseDuringPrayer = pauseDuringPrayer,
                cityName = cityName,
                latitude = latitude,
                longitude = longitude
            )
        }
    }

    fun clearStatusMessage() {
        _localState.update { it.copy(statusMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L

        fun factory() = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val db = FocusDatabase.getInstance(app)
                FocusViewModel(FocusRepository(db.focusDao()))
            }
        }
    }
}

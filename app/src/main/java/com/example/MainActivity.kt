package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.i18n.AppStrings
import com.example.i18n.FocusStrings
import com.example.model.UserProfile
import com.example.ui.components.AmbientOrbsBackground
import com.example.ui.components.FloatingGlassBottomBar
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPillChip
import com.example.ui.screens.DailyLimitScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SavedScreen
import com.example.ui.screens.SourcesScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.FocusReelsTheme
import com.example.ui.theme.LocalFocusGlassTokens
import com.example.ui.theme.VioletAccent
import com.example.ui.viewmodel.FocusViewModel
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.UiState
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusReelsAppRoot()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusReelsAppRoot() {
    val viewModel: FocusViewModel = viewModel(factory = FocusViewModel.factory())
    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val localState by viewModel.localState.collectAsStateWithLifecycle()
    val prayerStatus by viewModel.prayerStatus.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showSettingsSheet by remember { mutableStateOf(false) }

    val profile = (dashboardState as? UiState.Success)?.data?.profile
    val isDark = profile?.darkTheme ?: true
    val strings = AppStrings.get(profile?.language ?: "en")

    LaunchedEffect(localState.statusMessage) {
        localState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    BackHandler(enabled = localState.activeTab != MainTab.FEED) {
        viewModel.selectTab(MainTab.FEED)
    }

    FocusReelsTheme(darkTheme = isDark) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background
        ) { _ ->
            when (val state = dashboardState) {
                is UiState.Loading -> {
                    AmbientOrbsBackground {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = CyanAccent)
                        }
                    }
                }

                is UiState.Error -> {
                    AmbientOrbsBackground {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                is UiState.Success -> {
                    val data = state.data
                    val todaySeconds = viewModel.getEffectiveTodaySeconds()
                    val limitReached = todaySeconds >= data.profile.dailyLimitMinutes * 60L

                    if (!data.profile.onboardingCompleted) {
                        OnboardingScreen(
                            strings = strings,
                            onComplete = { interests, starters, limitMins ->
                                viewModel.completeOnboarding(interests, starters, limitMins)
                            }
                        )
                    } else if (limitReached && localState.activeTab == MainTab.FEED) {
                        DailyLimitScreen(
                            language = data.profile.language,
                            strings = strings,
                            onReadQuranDone = { viewModel.markQuranDoneFromLimitScreen() },
                            onReviewNotes = { viewModel.selectTab(MainTab.SAVED) }
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AnimatedContent(
                                targetState = localState.activeTab,
                                transitionSpec = {
                                    fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                                        fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
                                },
                                label = "mainTabTransition"
                            ) { tab ->
                                when (tab) {
                                    MainTab.FEED -> {
                                        FeedScreen(
                                            profile = data.profile,
                                            sources = data.sources,
                                            savedVideos = data.savedVideos,
                                            history = data.watchHistory,
                                            localState = localState,
                                            todayWatchedSeconds = todaySeconds,
                                            prayerStatus = prayerStatus,
                                            strings = strings,
                                            onSelectCategory = { cat ->
                                                viewModel.selectCategory(cat, data.sources, data.watchHistory)
                                            },
                                            onBeginSession = { viewModel.beginFocusedSession() },
                                            onTogglePlay = { viewModel.setFeedPlaying(it) },
                                            onToggleMute = { viewModel.toggleMute() },
                                            onVideoSwiped = { video ->
                                                viewModel.onVideoSwipedTo(
                                                    video,
                                                    data.profile.endOfFeedVideoLimit.toInt()
                                                )
                                            },
                                            onToggleSave = { video, savedItem ->
                                                viewModel.toggleSaveVideo(video, savedItem, data.profile.language)
                                            },
                                            onNotInterested = { video ->
                                                viewModel.markNotInterested(video, data.sources, data.watchHistory)
                                            },
                                            onReportBroken = { video ->
                                                viewModel.reportBrokenVideo(video)
                                            },
                                            onRefreshFeed = {
                                                viewModel.refreshFeed(data.sources, data.watchHistory)
                                            },
                                            onPlayYoutubeUrl = { url, cat ->
                                                viewModel.playPastedYoutubeUrl(url, cat)
                                            },
                                            onDismissBreakContinue = { viewModel.dismissBreakAndContinue() },
                                            onTakeBreak = { viewModel.takeBreakFromReminder() },
                                            onContinueEndOfFeed = { viewModel.continuePastEndOfFeed() },
                                            onFinishSession = { viewModel.finishSessionFromEndOfFeed() },
                                            onDismissPrayerPause = { viewModel.dismissPrayerPauseForNow() },
                                            onGoToSources = { viewModel.selectTab(MainTab.SOURCES) },
                                            onOpenSettings = { showSettingsSheet = true }
                                        )
                                    }

                                    MainTab.SOURCES -> {
                                        SourcesScreen(
                                            sources = data.sources,
                                            strings = strings,
                                            onAddSource = { name, url, platform, cat ->
                                                viewModel.addSource(name, url, platform, cat)
                                            },
                                            onToggleActive = { id, active ->
                                                viewModel.toggleSourceActive(id, active)
                                            },
                                            onDeleteSource = { id ->
                                                viewModel.deleteSource(id)
                                            },
                                            onPlayYoutubeVideoUrl = { url, cat ->
                                                viewModel.playPastedYoutubeUrl(url, cat)
                                            },
                                            onOpenSettings = { showSettingsSheet = true }
                                        )
                                    }

                                    MainTab.SAVED -> {
                                        SavedScreen(
                                            savedVideos = data.savedVideos,
                                            strings = strings,
                                            onUpdateNote = { id, note, cat ->
                                                viewModel.updateSavedNote(id, note, cat)
                                            },
                                            onRemoveSaved = { id ->
                                                viewModel.removeSaved(id)
                                            },
                                            onOpenSettings = { showSettingsSheet = true }
                                        )
                                    }

                                    MainTab.STATS -> {
                                        StatsScreen(
                                            profile = data.profile,
                                            dailyLogs = data.dailyLogs,
                                            todayWatchedSeconds = todaySeconds,
                                            prayerStatus = prayerStatus,
                                            strings = strings,
                                            onToggleHabit = { idx -> viewModel.toggleHabit(idx) },
                                            onUpdatePrayerLocation = { city, lat, lon ->
                                                viewModel.updateSettings(
                                                    cityName = city,
                                                    latitude = lat,
                                                    longitude = lon
                                                )
                                            },
                                            onTogglePrayerPause = { pause ->
                                                viewModel.updateSettings(pauseDuringPrayer = pause)
                                            },
                                            onOpenSettings = { showSettingsSheet = true }
                                        )
                                    }
                                }
                            }

                            FloatingGlassBottomBar(
                                activeTab = localState.activeTab,
                                strings = strings,
                                onSelectTab = { viewModel.selectTab(it) },
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }

                    if (showSettingsSheet) {
                        SettingsModalSheet(
                            profile = data.profile,
                            strings = strings,
                            onDismiss = { showSettingsSheet = false },
                            onUpdateDarkMode = { viewModel.updateSettings(darkTheme = it) },
                            onUpdateLanguage = { viewModel.updateSettings(language = it) },
                            onUpdateDailyLimit = { viewModel.updateSettings(dailyLimitMinutes = it) },
                            onUpdateEndOfFeed = { viewModel.updateSettings(endOfFeedVideoLimit = it) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsModalSheet(
    profile: UserProfile,
    strings: FocusStrings,
    onDismiss: () -> Unit,
    onUpdateDarkMode: (Boolean) -> Unit,
    onUpdateLanguage: (String) -> Unit,
    onUpdateDailyLimit: (Int) -> Unit,
    onUpdateEndOfFeed: (Int) -> Unit
) {
    val tokens = LocalFocusGlassTokens.current
    var sliderLimit by remember(profile.dailyLimitMinutes) {
        mutableFloatStateOf(profile.dailyLimitMinutes.toFloat())
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = strings.settingsTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(strings.darkModeLabel, style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = profile.darkTheme,
                    onCheckedChange = onUpdateDarkMode,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = VioletAccent
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(strings.languageLabel, style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassPillChip(
                        label = "English",
                        selected = profile.language == "en",
                        onClick = { onUpdateLanguage("en") }
                    )
                    GlassPillChip(
                        label = "বাংলা",
                        selected = profile.language == "bn",
                        onClick = { onUpdateLanguage("bn") }
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(strings.dailyLimitLabel, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "${sliderLimit.roundToInt()} ${strings.minutesUnit}",
                        style = MaterialTheme.typography.labelLarge,
                        color = CyanAccent
                    )
                }
                Slider(
                    value = sliderLimit,
                    onValueChange = { sliderLimit = it },
                    onValueChangeFinished = { onUpdateDailyLimit(sliderLimit.roundToInt()) },
                    valueRange = 5f..60f,
                    steps = 10,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanAccent,
                        activeTrackColor = VioletAccent,
                        inactiveTrackColor = tokens.elevatedSurface
                    )
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(strings.endOfFeedLimitLabel, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0, 5, 10, 15).forEach { cap ->
                        val label = if (cap == 0) strings.unlimitedLabel else "$cap"
                        GlassPillChip(
                            label = label,
                            selected = profile.endOfFeedVideoLimit.toInt() == cap,
                            onClick = { onUpdateEndOfFeed(cap) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

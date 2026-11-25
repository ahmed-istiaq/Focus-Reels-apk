package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ThumbDownOffAlt
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CuratedCatalog
import com.example.i18n.FocusStrings
import com.example.model.ContentCategory
import com.example.model.FeedVideo
import com.example.model.SavedVideo
import com.example.model.SourcePlatform
import com.example.model.UserProfile
import com.example.model.VideoSource
import com.example.model.WatchHistoryEntry
import com.example.ui.components.AmbientOrbsBackground
import com.example.ui.components.AnimatedReelCanvas
import com.example.ui.components.CircularLimitRing
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPillChip
import com.example.ui.components.GradientPillButton
import com.example.ui.components.YouTubeEmbedPlayer
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.HorizontalAccentGradient
import com.example.ui.theme.LocalFocusGlassTokens
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.VioletAccent
import com.example.ui.viewmodel.FocusLocalUiState
import com.example.util.FocusUtils
import com.example.util.PrayerStatus
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    profile: UserProfile,
    sources: List<VideoSource>,
    savedVideos: List<SavedVideo>,
    history: List<WatchHistoryEntry>,
    localState: FocusLocalUiState,
    todayWatchedSeconds: Long,
    prayerStatus: PrayerStatus,
    strings: FocusStrings,
    onSelectCategory: (ContentCategory?) -> Unit,
    onBeginSession: () -> Unit,
    onTogglePlay: (Boolean) -> Unit,
    onToggleMute: () -> Unit,
    onVideoSwiped: (FeedVideo) -> Unit,
    onToggleSave: (FeedVideo, SavedVideo?) -> Unit,
    onNotInterested: (FeedVideo) -> Unit,
    onReportBroken: (FeedVideo) -> Unit,
    onRefreshFeed: () -> Unit,
    onPlayYoutubeUrl: (String, ContentCategory) -> Unit,
    onDismissBreakContinue: () -> Unit,
    onTakeBreak: () -> Unit,
    onContinueEndOfFeed: () -> Unit,
    onFinishSession: () -> Unit,
    onDismissPrayerPause: () -> Unit,
    onGoToSources: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val tokens = LocalFocusGlassTokens.current
    var showPasteYoutubeDialog by remember { mutableStateOf(false) }

    val videos = localState.rankedFeed
    val activeSourcesCount = sources.count { it.active }
    val isPrayerPaused = profile.pauseDuringPrayer &&
        prayerStatus.activePrayerWindow != null &&
        !localState.prayerPauseDismissed

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            localState.showDailyIntentionCard -> {
                DailyIntentionOverlay(
                    profile = profile,
                    strings = strings,
                    onBegin = onBeginSession,
                    onPasteYoutube = { showPasteYoutubeDialog = true }
                )
            }

            isPrayerPaused -> {
                val activePrayer = prayerStatus.activePrayerWindow
                AmbientOrbsBackground {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MintSuccess.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.SelfImprovement,
                                        contentDescription = null,
                                        tint = MintSuccess,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "${strings.prayerPauseActiveTitle}: ${if (activePrayer != null) (if (profile.language == "bn") activePrayer.bn else activePrayer.en) else ""}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = strings.prayerPauseActiveBody,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = tokens.secondaryText
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                GradientPillButton(
                                    text = strings.resumeAnyway,
                                    onClick = onDismissPrayerPause,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            activeSourcesCount == 0 -> {
                AmbientOrbsBackground {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = strings.noActiveSourcesTitle,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = strings.noActiveSourcesBody,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = tokens.secondaryText
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                GradientPillButton(
                                    text = strings.goToSources,
                                    onClick = onGoToSources,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            videos.isEmpty() -> {
                AmbientOrbsBackground {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = strings.allCaughtUpTitle,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = strings.allCaughtUpBody,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = tokens.secondaryText
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                GradientPillButton(
                                    text = strings.reloadFeed,
                                    icon = Icons.Outlined.Refresh,
                                    onClick = onRefreshFeed,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            else -> {
                val pagerState = rememberPagerState(pageCount = { videos.size })
                LaunchedEffect(pagerState, videos) {
                    snapshotFlow { pagerState.settledPage }
                        .distinctUntilChanged()
                        .collect { page ->
                            videos.getOrNull(page)?.let { video ->
                                onVideoSwiped(video)
                            }
                        }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val video = videos[page]
                        val savedItem = savedVideos.firstOrNull { it.videoId == video.videoId }
                        val title = if (profile.language == "bn") video.titleBn else video.titleEn
                        val prompt = if (profile.language == "bn") video.takeawayPromptBn else video.takeawayPromptEn

                        Box(modifier = Modifier.fillMaxSize()) {
                            if (!video.youtubeVideoId.isNullOrBlank()) {
                                YouTubeEmbedPlayer(youtubeVideoId = video.youtubeVideoId)
                            } else {
                                AnimatedReelCanvas(
                                    gradientIndex = video.gradientIndex,
                                    isPlaying = localState.isFeedPlaying,
                                    isPausedOverlayText = strings.pausedTapToResume,
                                    takeawayPrompt = prompt,
                                    onTogglePlay = { onTogglePlay(!localState.isFeedPlaying) }
                                )
                            }

                            // Bottom dark scrim for legible metadata
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(270.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.86f))
                                        )
                                    )
                            )

                            // Bottom-left metadata + Facebook external notice
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .navigationBarsPadding()
                                    .padding(start = 18.dp, end = 86.dp, bottom = 96.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(VioletAccent.copy(alpha = 0.35f))
                                            .border(1.dp, VioletAccent.copy(alpha = 0.65f), RoundedCornerShape(50))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = strings.categoryLabel(video.category),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Color.White.copy(alpha = 0.14f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = video.platform.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CyanAccent
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "${video.channelName}  •  ${FocusUtils.formatCompactCount(video.viewsCount)} ${strings.viewsLabel}  •  ${FocusUtils.formatDuration(video.durationSeconds)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.78f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (video.platform == SourcePlatform.FACEBOOK) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color.Black.copy(alpha = 0.45f))
                                            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
                                            .clickable {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.videoUrl))
                                                context.startActivity(intent)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                            contentDescription = strings.openInBrowser,
                                            tint = CyanAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = strings.facebookExternalNotice,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.9f),
                                            maxLines = 2
                                        )
                                    }
                                }
                            }

                            // Right Rail Action Circles
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .navigationBarsPadding()
                                    .padding(end = 14.dp, bottom = 98.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val isSaved = savedItem != null
                                val saveScale by animateFloatAsState(
                                    targetValue = if (isSaved) 1.18f else 1f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                    label = "savePop"
                                )
                                RailCircleAction(
                                    icon = if (isSaved) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
                                    label = if (isSaved) strings.saved else strings.save,
                                    tint = if (isSaved) CyanAccent else Color.White,
                                    modifier = Modifier.scale(saveScale),
                                    testTag = "feed_save_button",
                                    onClick = { onToggleSave(video, savedItem) }
                                )

                                RailCircleAction(
                                    icon = Icons.Outlined.Share,
                                    label = strings.share,
                                    tint = Color.White,
                                    testTag = "feed_share_button",
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "$title — ${video.channelName}\n${video.videoUrl}")
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, strings.share))
                                    }
                                )

                                RailCircleAction(
                                    icon = if (localState.isMuted) Icons.AutoMirrored.Outlined.VolumeOff else Icons.AutoMirrored.Outlined.VolumeUp,
                                    label = if (localState.isMuted) strings.mute else strings.unmute,
                                    tint = Color.White,
                                    onClick = onToggleMute
                                )

                                RailCircleAction(
                                    icon = Icons.Outlined.ThumbDownOffAlt,
                                    label = strings.notInterested,
                                    tint = Color.White,
                                    onClick = { onNotInterested(video) }
                                )

                                RailCircleAction(
                                    icon = Icons.Outlined.Flag,
                                    label = strings.reportBroken,
                                    tint = Color.White.copy(alpha = 0.85f),
                                    onClick = { onReportBroken(video) }
                                )
                            }

                            // Thin gradient progress bar right above the floating glass bottom nav
                            val progressRatio = ((todayWatchedSeconds.toFloat() / (profile.dailyLimitMinutes * 60f).coerceAtLeast(60f))).coerceIn(0.04f, 1f)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                                    .padding(horizontal = 24.dp, vertical = 82.dp)
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(alpha = 0.16f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progressRatio)
                                        .height(3.dp)
                                        .background(HorizontalAccentGradient)
                                )
                            }
                        }
                    }

                    // Top Overlay: Category Chips + Limit Ring + Paste YouTube Link + Settings
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(top = 10.dp, start = 14.dp, end = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val cats = listOf<ContentCategory?>(
                                    null,
                                    ContentCategory.SELF_BUILDING,
                                    ContentCategory.MOTIVATION,
                                    ContentCategory.ISLAMIC,
                                    ContentCategory.OTHER
                                )
                                cats.forEach { cat ->
                                    GlassPillChip(
                                        label = strings.categoryLabel(cat),
                                        selected = localState.selectedCategory == cat,
                                        onClick = { onSelectCategory(cat) },
                                        testTag = "feed_chip_${cat?.name?.lowercase() ?: "all"}"
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
                                    .clickable { showPasteYoutubeDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Link,
                                    contentDescription = strings.pasteYoutubeUrl,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
                                    .clickable(onClick = onOpenSettings),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Tune,
                                    contentDescription = strings.settingsTitle,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            CircularLimitRing(
                                watchedSeconds = todayWatchedSeconds,
                                limitMinutes = profile.dailyLimitMinutes
                            )
                        }
                    }
                }
            }
        }

        // End of feed checkpoint overlay
        AnimatedVisibility(
            visible = localState.showEndOfFeedStop,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.78f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.endOfFeedTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = strings.endOfFeedBody,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.78f)
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        GradientPillButton(
                            text = strings.finishSession,
                            onClick = onFinishSession,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(onClick = onContinueEndOfFeed) {
                            Text(strings.continueMindfully, color = CyanAccent)
                        }
                    }
                }
            }
        }

        // 10-minute Break Reminder ModalBottomSheet
        if (localState.showBreakReminder) {
            ModalBottomSheet(
                onDismissRequest = onDismissBreakContinue,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(CyanAccent.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SelfImprovement,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = strings.breakReminderTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.breakReminderBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.secondaryText
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    GradientPillButton(
                        text = strings.takeABreak,
                        onClick = onTakeBreak,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(onClick = onDismissBreakContinue) {
                        Text(strings.continueWatching, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }

        // Paste YouTube URL Dialog
        if (showPasteYoutubeDialog) {
            var urlInput by remember { mutableStateOf("") }
            var selectedCat by remember { mutableStateOf(ContentCategory.SELF_BUILDING) }
            AlertDialog(
                onDismissRequest = { showPasteYoutubeDialog = false },
                title = { Text(strings.pasteYoutubeUrl) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            placeholder = { Text(strings.pasteYoutubeHint) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ContentCategory.entries.forEach { cat ->
                                GlassPillChip(
                                    label = strings.categoryLabel(cat),
                                    selected = selectedCat == cat,
                                    onClick = { selectedCat = cat }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (urlInput.isNotBlank()) {
                                onPlayYoutubeUrl(urlInput, selectedCat)
                                showPasteYoutubeDialog = false
                            }
                        }
                    ) {
                        Text(strings.playNow, color = CyanAccent)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPasteYoutubeDialog = false }) {
                        Text(strings.cancel)
                    }
                }
            )
        }
    }
}

@Composable
private fun DailyIntentionOverlay(
    profile: UserProfile,
    strings: FocusStrings,
    onBegin: () -> Unit,
    onPasteYoutube: () -> Unit
) {
    val quote = remember { CuratedCatalog.todayQuote() }
    val tokens = LocalFocusGlassTokens.current
    val isBn = profile.language == "bn"

    AmbientOrbsBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 72.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(tokens.glassFill)
                        .border(1.dp, tokens.glassBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = strings.dailyCardHeader,
                    style = MaterialTheme.typography.labelLarge,
                    color = MintSuccess
                )
                Spacer(modifier = Modifier.height(12.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 26.dp,
                    contentPadding = PaddingValues(22.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isBn) quote.sourceTypeBn else quote.sourceTypeEn,
                            style = MaterialTheme.typography.labelMedium,
                            color = VioletAccent
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "\"${if (isBn) quote.textBn else quote.textEn}\"",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isBn) quote.referenceBn else quote.referenceEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.secondaryText
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                GradientPillButton(
                    text = strings.beginFocusedSession,
                    icon = Icons.Outlined.PlayArrow,
                    onClick = onBegin,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "begin_focused_session_button"
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onPasteYoutube) {
                    Icon(
                        imageVector = Icons.Outlined.Link,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.pasteYoutubeUrl, color = CyanAccent)
                }
            }
        }
    }
}

@Composable
private fun RailCircleAction(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .let { if (testTag != null) it.testTag(testTag) else it }
                .minimumInteractiveComponentSize()
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.48f))
                .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.85f),
            maxLines = 1
        )
    }
}

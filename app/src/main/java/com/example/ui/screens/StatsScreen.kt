package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.i18n.FocusStrings
import com.example.model.DailyLog
import com.example.model.UserProfile
import com.example.ui.components.AmbientOrbsBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPillChip
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.LocalFocusGlassTokens
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.VioletAccent
import com.example.util.FocusUtils
import com.example.util.PrayerStatus
import com.example.util.PrayerTimeCalculator
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@SuppressLint("MissingPermission")
@Composable
fun StatsScreen(
    profile: UserProfile,
    dailyLogs: List<DailyLog>,
    todayWatchedSeconds: Long,
    prayerStatus: PrayerStatus,
    strings: FocusStrings,
    onToggleHabit: (Int) -> Unit,
    onUpdatePrayerLocation: (String, Double, Double) -> Unit,
    onTogglePrayerPause: (Boolean) -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val tokens = LocalFocusGlassTokens.current
    val todayKey = remember { FocusUtils.todayDateKey() }
    val todayLog = dailyLogs.firstOrNull { it.dateKey == todayKey }
    val weekKeys = remember { FocusUtils.last7DaysKeys() }

    val fusedLocationClient = remember(context) {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        onUpdatePrayerLocation("GPS Location", location.latitude, location.longitude)
                    }
                }
        }
    }

    AmbientOrbsBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.statsTitle,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = strings.settingsTitle,
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // KPI Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val todayMins = (todayWatchedSeconds / 60L).toInt()
                    val videosToday = todayLog?.videosWatchedCount ?: 0L
                    StatKpiCard(
                        title = strings.todayWatchTime,
                        value = "${todayMins}/${profile.dailyLimitMinutes}${strings.minutesUnit.take(1)}",
                        accent = CyanAccent,
                        modifier = Modifier.weight(1f)
                    )
                    StatKpiCard(
                        title = strings.videosWatched,
                        value = videosToday.toString(),
                        accent = VioletAccent,
                        modifier = Modifier.weight(1f)
                    )
                    StatKpiCard(
                        title = strings.underLimitStreak,
                        value = "${profile.streakDays} ${strings.daysUnit}",
                        accent = MintSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Weekly Bar Chart
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = strings.weeklyWatchTime,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val logMap = dailyLogs.associateBy { it.dateKey }
                        val barValues = weekKeys.map { (key, dayLabel) ->
                            val secs = if (key == todayKey) todayWatchedSeconds else (logMap[key]?.watchedSeconds ?: 0L)
                            dayLabel to (secs / 60f)
                        }
                        val maxY = maxOf(
                            profile.dailyLimitMinutes.toFloat(),
                            barValues.maxOfOrNull { it.second } ?: 1f,
                            10f
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val chartHeight = size.height - 28.dp.toPx()
                                val barWidth = (size.width / barValues.size) * 0.48f
                                val slotWidth = size.width / barValues.size

                                // Daily limit dashed threshold line
                                val limitY = chartHeight - (profile.dailyLimitMinutes.toFloat() / maxY) * chartHeight
                                drawLine(
                                    color = MintSuccess.copy(alpha = 0.55f),
                                    start = Offset(0f, limitY),
                                    end = Offset(size.width, limitY),
                                    strokeWidth = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                                )

                                barValues.forEachIndexed { idx, (_, mins) ->
                                    val barHeight = ((mins / maxY) * chartHeight).coerceAtLeast(6.dp.toPx())
                                    val left = idx * slotWidth + (slotWidth - barWidth) / 2f
                                    val top = chartHeight - barHeight
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(listOf(CyanAccent, VioletAccent)),
                                        topLeft = Offset(left, top),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                barValues.forEach { (label, _) ->
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.secondaryText
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Daily Growth Habits (Quran, Prayer, Learning, Exercise)
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = strings.dailyGoalsTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        val habits = listOf(
                            Triple(0, strings.goalQuran, Pair(Icons.Outlined.MenuBook, todayLog?.quranDone ?: false)),
                            Triple(1, strings.goalPrayer, Pair(Icons.Outlined.SelfImprovement, todayLog?.prayerDone ?: false)),
                            Triple(2, strings.goalLearning, Pair(Icons.Outlined.Psychology, todayLog?.learningDone ?: false)),
                            Triple(3, strings.goalExercise, Pair(Icons.Outlined.DirectionsRun, todayLog?.exerciseDone ?: false))
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            habits.forEach { (index, label, iconAndDone) ->
                                HabitCheckRing(
                                    label = label,
                                    icon = iconAndDone.first,
                                    done = iconAndDone.second,
                                    onClick = { onToggleHabit(index) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("habit_ring_$index")
                                )
                            }
                        }
                    }
                }
            }

            // Prayer Time Awareness Card
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = strings.prayerTimesTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                val nextName = if (profile.language == "bn") prayerStatus.nextPrayer.bn else prayerStatus.nextPrayer.en
                                Text(
                                    text = "${strings.nextPrayerLabel}: $nextName (${prayerStatus.minutesUntilNext}m) • ${profile.cityName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MintSuccess
                                )
                            }
                            TextButton(
                                onClick = {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (granted) {
                                        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                                            .addOnSuccessListener { loc ->
                                                if (loc != null) {
                                                    onUpdatePrayerLocation("GPS Location", loc.latitude, loc.longitude)
                                                }
                                            }
                                    } else {
                                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                                    }
                                }
                            ) {
                                Icon(Icons.Outlined.MyLocation, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.useLocation, color = CyanAccent, style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            prayerStatus.entries.forEach { entry ->
                                val isNext = entry.prayer == prayerStatus.nextPrayer
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isNext) VioletAccent.copy(alpha = 0.24f) else Color.Transparent)
                                        .padding(horizontal = 6.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (profile.language == "bn") entry.prayer.bn else entry.prayer.en,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isNext) CyanAccent else tokens.secondaryText
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = entry.formattedTime,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PrayerTimeCalculator.presetCities.forEach { city ->
                                GlassPillChip(
                                    label = city.name,
                                    selected = profile.cityName == city.name,
                                    onClick = { onUpdatePrayerLocation(city.name, city.latitude, city.longitude) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.pauseDuringPrayerLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Switch(
                                checked = profile.pauseDuringPrayer,
                                onCheckedChange = onTogglePrayerPause,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MintSuccess
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatKpiCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val tokens = LocalFocusGlassTokens.current
    GlassCard(
        modifier = modifier,
        contentPadding = PaddingValues(14.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = tokens.secondaryText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = accent
            )
        }
    }
}

@Composable
private fun HabitCheckRing(
    label: String,
    icon: ImageVector,
    done: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalFocusGlassTokens.current
    val progress by animateFloatAsState(
        targetValue = if (done) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "habitRing"
    )
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (done) MintSuccess.copy(alpha = 0.14f) else tokens.elevatedSurface),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(3.dp)) {
                val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                drawArc(
                    color = Color.White.copy(alpha = 0.14f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke
                )
                drawArc(
                    color = MintSuccess,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = stroke
                )
            }
            Icon(
                imageVector = if (done) Icons.Outlined.Check else icon,
                contentDescription = label,
                tint = if (done) MintSuccess else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (done) MintSuccess else tokens.secondaryText,
            maxLines = 2
        )
    }
}

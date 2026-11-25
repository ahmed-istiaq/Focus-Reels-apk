package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.CuratedCatalog
import com.example.data.StarterChannelTemplate
import com.example.i18n.FocusStrings
import com.example.model.ContentCategory
import com.example.ui.components.AmbientOrbsBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientPillButton
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.LocalFocusGlassTokens
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.PrimaryAccentGradient
import com.example.ui.theme.VioletAccent
import kotlin.math.roundToInt

@Composable
fun OnboardingScreen(
    strings: FocusStrings,
    onComplete: (List<ContentCategory>, List<StarterChannelTemplate>, Int) -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var selectedInterests by remember {
        mutableStateOf(setOf(ContentCategory.SELF_BUILDING, ContentCategory.ISLAMIC))
    }
    var selectedChannelIds by remember {
        mutableStateOf(CuratedCatalog.starterChannels.take(4).map { it.id }.toSet())
    }
    var dailyLimitFloat by remember { mutableFloatStateOf(20f) }

    val tokens = LocalFocusGlassTokens.current

    AmbientOrbsBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 16.dp)
                .widthIn(max = 540.dp)
                .align(Alignment.Center)
        ) {
            // Step indicator bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 0) {
                    IconButton(onClick = { step -= 1 }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = strings.backStep,
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { idx ->
                        val active = idx <= step
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (active) PrimaryAccentGradient
                                    else androidx.compose.ui.graphics.Brush.linearGradient(
                                        listOf(tokens.glassFill, tokens.glassFill)
                                    )
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${step + 1}/3",
                    style = MaterialTheme.typography.labelMedium,
                    color = tokens.secondaryText
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            AnimatedContent(
                targetState = step,
                modifier = Modifier.weight(1f),
                label = "onboardingStep"
            ) { currentStep ->
                when (currentStep) {
                    0 -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = strings.onboardingStep1Title,
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = strings.onboardingStep1Sub,
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.secondaryText
                            )
                            Spacer(modifier = Modifier.height(22.dp))

                            val categories = listOf(
                                Triple(ContentCategory.SELF_BUILDING, strings.categorySelfBuilding, Icons.Outlined.Psychology),
                                Triple(ContentCategory.MOTIVATION, strings.categoryMotivation, Icons.Outlined.AutoAwesome),
                                Triple(ContentCategory.ISLAMIC, strings.categoryIslamic, Icons.Outlined.MenuBook),
                                Triple(ContentCategory.OTHER, strings.categoryOther, Icons.Outlined.SelfImprovement)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                categories.forEach { (cat, label, icon) ->
                                    val isSelected = cat in selectedInterests
                                    GlassCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("onboarding_interest_${cat.name.lowercase()}"),
                                        onClick = {
                                            selectedInterests = if (isSelected && selectedInterests.size > 1) {
                                                selectedInterests - cat
                                            } else {
                                                selectedInterests + cat
                                            }
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) PrimaryAccentGradient
                                                        else androidx.compose.ui.graphics.Brush.linearGradient(
                                                            listOf(tokens.elevatedSurface, tokens.elevatedSurface)
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = label,
                                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Check,
                                                    contentDescription = null,
                                                    tint = MintSuccess
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = strings.onboardingStep2Title,
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = strings.onboardingStep2Sub,
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.secondaryText
                            )
                            Spacer(modifier = Modifier.height(18.dp))

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(CuratedCatalog.starterChannels) { channel ->
                                    val isSelected = channel.id in selectedChannelIds
                                    GlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
                                        onClick = {
                                            selectedChannelIds = if (isSelected && selectedChannelIds.size > 1) {
                                                selectedChannelIds - channel.id
                                            } else {
                                                selectedChannelIds + channel.id
                                            }
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(PrimaryAccentGradient),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = channel.name.take(1),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = Color.White
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = channel.name,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                                Text(
                                                    text = "${channel.platform.label} • ${strings.categoryLabel(channel.category)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = tokens.secondaryText
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) MintSuccess else tokens.elevatedSurface)
                                                    .border(1.dp, tokens.glassBorder, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Check,
                                                        contentDescription = null,
                                                        tint = Color(0xFF062016),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = strings.onboardingStep3Title,
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = strings.onboardingStep3Sub,
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.secondaryText
                            )
                            Spacer(modifier = Modifier.height(28.dp))

                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 26.dp
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val mins = dailyLimitFloat.roundToInt()
                                    Text(
                                        text = "$mins ${strings.minutesUnit}",
                                        style = MaterialTheme.typography.displayLarge,
                                        color = CyanAccent
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Slider(
                                        value = dailyLimitFloat,
                                        onValueChange = { dailyLimitFloat = it },
                                        valueRange = 5f..60f,
                                        steps = 10,
                                        colors = SliderDefaults.colors(
                                            thumbColor = CyanAccent,
                                            activeTrackColor = VioletAccent,
                                            inactiveTrackColor = tokens.elevatedSurface
                                        ),
                                        modifier = Modifier.testTag("onboarding_limit_slider")
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("5m", style = MaterialTheme.typography.bodySmall, color = tokens.secondaryText)
                                        Text("20m", style = MaterialTheme.typography.bodySmall, color = MintSuccess)
                                        Text("60m", style = MaterialTheme.typography.bodySmall, color = tokens.secondaryText)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            GradientPillButton(
                text = if (step < 2) strings.nextStep else strings.startFocusReels,
                onClick = {
                    if (step < 2) {
                        step += 1
                    } else {
                        val starters = CuratedCatalog.starterChannels.filter { it.id in selectedChannelIds }
                        onComplete(
                            selectedInterests.toList(),
                            starters,
                            dailyLimitFloat.roundToInt()
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "onboarding_next_button"
            )
        }
    }
}

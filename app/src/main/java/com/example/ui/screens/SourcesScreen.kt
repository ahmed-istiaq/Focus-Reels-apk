package com.example.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CuratedCatalog
import com.example.i18n.FocusStrings
import com.example.model.ContentCategory
import com.example.model.SourcePlatform
import com.example.model.VideoSource
import com.example.ui.components.AmbientOrbsBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPillChip
import com.example.ui.components.GradientPillButton
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.LocalFocusGlassTokens
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.PrimaryAccentGradient
import com.example.ui.theme.ReelGradients
import com.example.ui.theme.VioletAccent
import com.example.util.FocusUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcesScreen(
    sources: List<VideoSource>,
    strings: FocusStrings,
    onAddSource: (String, String, SourcePlatform, ContentCategory) -> Unit,
    onToggleActive: (String, Boolean) -> Unit,
    onDeleteSource: (String) -> Unit,
    onPlayYoutubeVideoUrl: (String, ContentCategory) -> Unit,
    onOpenSettings: () -> Unit
) {
    val tokens = LocalFocusGlassTokens.current
    var showAddSheet by remember { mutableStateOf(false) }

    AmbientOrbsBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 18.dp)
            ) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.sourcesTitle,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.sourcesSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.secondaryText
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = strings.settingsTitle,
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 176.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sources, key = { it.id }) { source ->
                        val grad = ReelGradients[source.gradientIndex.toInt().coerceIn(0, ReelGradients.lastIndex)]
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(grad)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = source.name.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = source.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(VioletAccent.copy(alpha = 0.22f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = source.platformEnum().label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CyanAccent
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(tokens.elevatedSurface)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = strings.categoryLabel(source.categoryEnum()),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = tokens.secondaryText
                                            )
                                        }
                                    }
                                }
                                Switch(
                                    checked = source.active,
                                    onCheckedChange = { onToggleActive(source.id, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MintSuccess
                                    )
                                )
                                IconButton(onClick = { onDeleteSource(source.id) }) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = tokens.secondaryText
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Floating "+ Add channel" gradient button above bottom nav
            GradientPillButton(
                text = strings.addChannel,
                icon = Icons.Outlined.Add,
                onClick = { showAddSheet = true },
                testTag = "add_channel_button",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 20.dp, bottom = 92.dp)
            )
        }

        if (showAddSheet) {
            var urlText by remember { mutableStateOf("") }
            var nameText by remember { mutableStateOf("") }
            var platform by remember { mutableStateOf(SourcePlatform.YOUTUBE) }
            var category by remember { mutableStateOf(ContentCategory.SELF_BUILDING) }

            ModalBottomSheet(
                onDismissRequest = { showAddSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = strings.addChannel,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = urlText,
                        onValueChange = {
                            urlText = it
                            platform = FocusUtils.detectPlatform(it)
                        },
                        placeholder = { Text(strings.channelUrlHint) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("source_url_input")
                    )

                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        placeholder = { Text(strings.channelNameHint) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(strings.choosePlatform, style = MaterialTheme.typography.labelMedium, color = tokens.secondaryText)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SourcePlatform.entries.forEach { p ->
                            GlassPillChip(
                                label = p.label,
                                selected = platform == p,
                                onClick = { platform = p }
                            )
                        }
                    }

                    Text(strings.chooseCategory, style = MaterialTheme.typography.labelMedium, color = tokens.secondaryText)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ContentCategory.entries.forEach { cat ->
                            GlassPillChip(
                                label = strings.categoryLabel(cat),
                                selected = category == cat,
                                onClick = { category = cat }
                            )
                        }
                    }

                    val directYoutubeId = FocusUtils.extractYoutubeVideoId(urlText)
                    if (directYoutubeId != null) {
                        GradientPillButton(
                            text = strings.playNow,
                            onClick = {
                                onPlayYoutubeVideoUrl(urlText, category)
                                showAddSheet = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        GradientPillButton(
                            text = strings.addSourceButton,
                            enabled = urlText.isNotBlank(),
                            onClick = {
                                onAddSource(nameText, urlText, platform, category)
                                showAddSheet = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "confirm_add_source_button"
                        )
                    }

                    val existingNames = sources.map { it.name.lowercase() }.toSet()
                    val unaddedStarters = CuratedCatalog.starterChannels.filter { it.name.lowercase() !in existingNames }
                    if (unaddedStarters.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.starterChannelsLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = tokens.secondaryText
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            unaddedStarters.forEach { starter ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(tokens.glassFill)
                                        .border(1.dp, tokens.glassBorder, RoundedCornerShape(50))
                                        .clickable {
                                            onAddSource(starter.name, starter.handleOrUrl, starter.platform, starter.category)
                                            showAddSheet = false
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "+ ${starter.name}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CyanAccent
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

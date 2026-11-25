package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.i18n.FocusStrings
import com.example.model.ContentCategory
import com.example.model.SavedVideo
import com.example.ui.components.AmbientOrbsBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPillChip
import com.example.ui.components.GradientPillButton
import com.example.ui.components.YouTubeEmbedPlayer
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.LocalFocusGlassTokens
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.ReelGradients

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    savedVideos: List<SavedVideo>,
    strings: FocusStrings,
    onUpdateNote: (String, String, ContentCategory) -> Unit,
    onRemoveSaved: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val tokens = LocalFocusGlassTokens.current

    var selectedCategory by remember { mutableStateOf<ContentCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var activeDetailVideo by remember { mutableStateOf<SavedVideo?>(null) }

    val filtered = remember(savedVideos, selectedCategory, searchQuery) {
        val q = searchQuery.trim().lowercase()
        savedVideos.filter { item ->
            val catOk = selectedCategory == null || item.categoryEnum() == selectedCategory
            val queryOk = q.isEmpty() ||
                item.title.lowercase().contains(q) ||
                item.channelName.lowercase().contains(q) ||
                item.note.lowercase().contains(q)
            catOk && queryOk
        }
    }

    AmbientOrbsBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = strings.savedTitle,
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.searchSavedHint) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("saved_search_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BookmarkBorder,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = strings.noSavedTitle,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = strings.noSavedBody,
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.secondaryText
                            )
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 112.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered, key = { it.id }) { item ->
                        val grad = ReelGradients[item.gradientIndex.toInt().coerceIn(0, ReelGradients.lastIndex)]
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("saved_card_${item.id}"),
                            contentPadding = PaddingValues(10.dp),
                            onClick = { activeDetailVideo = item }
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1.25f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Brush.linearGradient(grad))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = strings.categoryLabel(item.categoryEnum()),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanAccent,
                                        modifier = Modifier.align(Alignment.TopStart)
                                    )
                                    Text(
                                        text = item.channelName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.align(Alignment.BottomStart)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Notes,
                                        contentDescription = null,
                                        tint = if (item.note.isNotBlank()) MintSuccess else tokens.secondaryText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = item.note.ifBlank { strings.takeawayNotesHint },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (item.note.isNotBlank()) MaterialTheme.colorScheme.onBackground else tokens.secondaryText,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Saved video detail & takeaway notes bottom sheet
        activeDetailVideo?.let { detail ->
            var noteText by remember(detail.id) { mutableStateOf(detail.note) }
            var folderCat by remember(detail.id) { mutableStateOf(detail.categoryEnum()) }

            ModalBottomSheet(
                onDismissRequest = { activeDetailVideo = null },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 10.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!detail.youtubeVideoId.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(18.dp))
                        ) {
                            YouTubeEmbedPlayer(youtubeVideoId = detail.youtubeVideoId)
                        }
                    }

                    Text(
                        text = detail.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${detail.channelName} • ${detail.platformEnum().label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.secondaryText
                    )

                    Text(strings.moveFolder, style = MaterialTheme.typography.labelMedium, color = tokens.secondaryText)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ContentCategory.entries.forEach { cat ->
                            GlassPillChip(
                                label = strings.categoryLabel(cat),
                                selected = folderCat == cat,
                                onClick = { folderCat = cat }
                            )
                        }
                    }

                    Text(strings.takeawayNotesLabel, style = MaterialTheme.typography.labelMedium, color = MintSuccess)
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = { Text(strings.takeawayNotesHint) },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("saved_note_input")
                    )

                    GradientPillButton(
                        text = strings.saveNote,
                        onClick = {
                            onUpdateNote(detail.id, noteText, folderCat)
                            activeDetailVideo = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "save_note_button"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(detail.videoUrl))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.openInBrowser, color = CyanAccent)
                        }
                        TextButton(
                            onClick = {
                                val share = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "${detail.title}\n${detail.videoUrl}\n\nNote: $noteText")
                                }
                                context.startActivity(Intent.createChooser(share, strings.share))
                            }
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.share)
                        }
                        TextButton(
                            onClick = {
                                onRemoveSaved(detail.id)
                                activeDetailVideo = null
                            }
                        ) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.removeFromSaved, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.CuratedCatalog
import com.example.i18n.FocusStrings
import com.example.ui.components.AmbientOrbsBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientPillButton
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.LocalFocusGlassTokens
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.VioletAccent

@Composable
fun DailyLimitScreen(
    language: String,
    strings: FocusStrings,
    onReadQuranDone: () -> Unit,
    onReviewNotes: () -> Unit
) {
    val tokens = LocalFocusGlassTokens.current
    val quote = remember { CuratedCatalog.todayQuote() }
    val isBn = language == "bn"
    var showQuranSheet by remember { mutableStateOf(false) }
    var walkBannerVisible by remember { mutableStateOf(false) }

    AmbientOrbsBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(tokens.glassFill)
                        .border(1.dp, MintSuccess.copy(alpha = 0.55f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = strings.dailyLimitTitle,
                        tint = MintSuccess,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = strings.dailyLimitTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = strings.dailyLimitSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.secondaryText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isBn) quote.sourceTypeBn else quote.sourceTypeEn,
                            style = MaterialTheme.typography.labelMedium,
                            color = VioletAccent
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "\"${if (isBn) quote.textBn else quote.textEn}\"",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBn) quote.referenceBn else quote.referenceEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.secondaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                GradientPillButton(
                    text = strings.readQuran,
                    icon = Icons.Outlined.MenuBook,
                    onClick = { showQuranSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "limit_read_quran_button"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReviewNotes,
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, tokens.glassBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Outlined.Notes, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.reviewNotes, maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = { walkBannerVisible = true },
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, tokens.glassBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MintSuccess),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Outlined.DirectionsWalk, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.goForAWalk, maxLines = 1)
                    }
                }

                if (walkBannerVisible) {
                    Spacer(modifier = Modifier.height(14.dp))
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Text(
                            text = strings.walkStartedBanner,
                            style = MaterialTheme.typography.bodySmall,
                            color = MintSuccess,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (showQuranSheet) {
            AlertDialog(
                onDismissRequest = { showQuranSheet = false },
                title = { Text(strings.quranReflectionTitle) },
                text = { Text(strings.quranReflectionBody) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onReadQuranDone()
                            showQuranSheet = false
                        }
                    ) {
                        Text(strings.markQuranDone, color = MintSuccess)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showQuranSheet = false }) {
                        Text(strings.close)
                    }
                }
            )
        }
    }
}

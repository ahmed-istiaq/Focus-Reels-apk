package com.example.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.DynamicFeed
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.i18n.FocusStrings
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.LocalFocusGlassTokens
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.PrimaryAccentGradient
import com.example.ui.theme.ReelGradients
import com.example.ui.theme.VioletAccent
import com.example.ui.viewmodel.MainTab

@Composable
fun AmbientOrbsBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val bgColor = MaterialTheme.colorScheme.background
    val isDark = LocalFocusGlassTokens.current.isDark
    val violetAlpha = if (isDark) 0.22f else 0.14f
    val cyanAlpha = if (isDark) 0.18f else 0.12f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(VioletAccent.copy(alpha = violetAlpha), Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.14f),
                        radius = size.width * 0.72f
                    ),
                    radius = size.width * 0.72f,
                    center = Offset(size.width * 0.15f, size.height * 0.14f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyanAccent.copy(alpha = cyanAlpha), Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.75f),
                        radius = size.width * 0.68f
                    ),
                    radius = size.width * 0.68f,
                    center = Offset(size.width * 0.88f, size.height * 0.75f)
                )
            },
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val tokens = LocalFocusGlassTokens.current
    val shape = RoundedCornerShape(cornerRadius)
    val baseMod = modifier
        .clip(shape)
        .background(tokens.glassFill)
        .border(1.dp, tokens.glassBorder, shape)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        .padding(contentPadding)

    Box(modifier = baseMod, content = content)
}

@Composable
fun GradientPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val alpha = if (enabled) 1f else 0.45f
    Box(
        modifier = modifier
            .let { if (testTag != null) it.testTag(testTag) else it }
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(50))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        VioletAccent.copy(alpha = alpha),
                        CyanAccent.copy(alpha = alpha)
                    )
                )
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GlassPillChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    val tokens = LocalFocusGlassTokens.current
    val borderColor by animateColorAsState(
        targetValue = if (selected) CyanAccent.copy(alpha = 0.7f) else tokens.glassBorder,
        animationSpec = tween(220),
        label = "chipBorder"
    )
    Box(
        modifier = modifier
            .let { if (testTag != null) it.testTag(testTag) else it }
            .height(38.dp)
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) PrimaryAccentGradient
                else Brush.linearGradient(listOf(tokens.glassFill, tokens.glassFill))
            )
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
fun CircularLimitRing(
    watchedSeconds: Long,
    limitMinutes: Long,
    modifier: Modifier = Modifier
) {
    val totalLimitSeconds = (limitMinutes * 60L).coerceAtLeast(60L)
    val rawFraction = (watchedSeconds.toFloat() / totalLimitSeconds.toFloat()).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = rawFraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "limitRing"
    )
    val watchedMin = (watchedSeconds / 60L).toInt()

    Box(
        modifier = modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            val stroke = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = Color.White.copy(alpha = 0.16f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(VioletAccent, CyanAccent, MintSuccess)),
                startAngle = -90f,
                sweepAngle = 360f * animatedFraction,
                useCenter = false,
                style = stroke
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${watchedMin}/${limitMinutes}",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 11.sp
            )
            Text(
                text = "m",
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 8.sp,
                lineHeight = 9.sp
            )
        }
    }
}

@Composable
fun AnimatedReelCanvas(
    gradientIndex: Int,
    isPlaying: Boolean,
    isPausedOverlayText: String,
    takeawayPrompt: String,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ReelGradients[gradientIndex.coerceIn(0, ReelGradients.lastIndex)]
    val infiniteTransition = rememberInfiniteTransition(label = "reelPulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbPulse"
    )
    val effectiveScale = if (isPlaying) pulse else 0.95f

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onTogglePlay)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(colors = colors)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(VioletAccent.copy(alpha = 0.34f), Color.Transparent),
                    center = Offset(size.width * 0.25f, size.height * 0.30f),
                    radius = size.width * 0.58f * effectiveScale
                ),
                radius = size.width * 0.58f * effectiveScale,
                center = Offset(size.width * 0.25f, size.height * 0.30f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CyanAccent.copy(alpha = 0.28f), Color.Transparent),
                    center = Offset(size.width * 0.78f, size.height * 0.56f),
                    radius = size.width * 0.52f * effectiveScale
                ),
                radius = size.width * 0.52f * effectiveScale,
                center = Offset(size.width * 0.78f, size.height * 0.56f)
            )
            // Subtle concentric focus ring at center
            val center = Offset(size.width / 2f, size.height * 0.44f)
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = 96.dp.toPx() * effectiveScale,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.14f),
                radius = 64.dp.toPx(),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Center focal reflection prompt & play/pause indicator
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 36.dp)
                .padding(bottom = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = isPausedOverlayText,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.Black.copy(alpha = 0.34f))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = if (isPlaying) takeawayPrompt else isPausedOverlayText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeEmbedPlayer(
    youtubeVideoId: String,
    modifier: Modifier = Modifier
) {
    val embedHtml = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <style>
            html, body { margin: 0; padding: 0; width: 100%; height: 100%; background: #0A0A0F; overflow: hidden; }
            iframe { width: 100%; height: 100%; border: 0; }
          </style>
        </head>
        <body>
          <iframe
            src="https://www.youtube.com/embed/$youtubeVideoId?playsinline=1&rel=0&modestbranding=1&autoplay=1"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            allowfullscreen>
          </iframe>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(android.graphics.Color.parseColor("#0A0A0F"))
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()
                loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "utf-8", null)
            }
        },
        update = { webView ->
            val currentTag = webView.tag as? String
            if (currentTag != youtubeVideoId) {
                webView.tag = youtubeVideoId
                webView.loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "utf-8", null)
            }
        }
    )
}

@Composable
fun FloatingGlassBottomBar(
    activeTab: MainTab,
    strings: FocusStrings,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalFocusGlassTokens.current
    val surfaceColor = if (tokens.isDark) {
        Color(0xFF14141C).copy(alpha = 0.88f)
    } else {
        Color.White.copy(alpha = 0.92f)
    }

    val items = listOf(
        Triple(MainTab.FEED, strings.tabFeed, Pair(Icons.Rounded.PlayCircle, Icons.Outlined.PlayCircle)),
        Triple(MainTab.SOURCES, strings.tabSources, Pair(Icons.Rounded.DynamicFeed, Icons.Outlined.DynamicFeed)),
        Triple(MainTab.SAVED, strings.tabSaved, Pair(Icons.Rounded.Bookmark, Icons.Outlined.BookmarkBorder)),
        Triple(MainTab.STATS, strings.tabStats, Pair(Icons.Rounded.Insights, Icons.Outlined.Insights))
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(28.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, tokens.glassBorder),
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (tab, label, icons) ->
                val selected = tab == activeTab
                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.06f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "tabScale"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nav_tab_${tab.name.lowercase()}")
                        .minimumInteractiveComponentSize()
                        .scale(scale)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            if (selected) PrimaryAccentGradient
                            else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable { onSelectTab(tab) }
                        .padding(vertical = 8.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (selected) icons.first else icons.second,
                            contentDescription = label,
                            tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

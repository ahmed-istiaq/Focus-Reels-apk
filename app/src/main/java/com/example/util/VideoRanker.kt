package com.example.util

import com.example.model.ContentCategory
import com.example.model.FeedVideo
import com.example.model.VideoSource
import com.example.model.WatchHistoryEntry
import kotlin.math.max
import kotlin.random.Random

/**
 * Smart ranking algorithm for Focus Reels:
 * - Filters strictly to active user-selected sources (and selected category filter if non-null).
 * - Excludes videos marked Not Interested, Reported, or watched within the last 7 days.
 * - Computes popularity score = (views * 1) + (likes * 5), normalized per source so large channels
 *   never drown out smaller curated channels.
 * - Adds a recency boost for videos published within the last 7 days.
 * - Performs weighted random sampling without replacement for a fresh, non-addictive order.
 */
object VideoRanker {
    private const val SEVEN_DAYS_MILLIS = 7L * 24L * 60L * 60L * 1000L

    fun rankFeed(
        allVideos: List<FeedVideo>,
        sources: List<VideoSource>,
        history: List<WatchHistoryEntry>,
        selectedCategory: ContentCategory?,
        nowMillis: Long = System.currentTimeMillis(),
        random: Random = Random.Default
    ): List<FeedVideo> {
        val activeSourceIds = sources.filter { it.active }.map { it.id }.toSet()
        if (activeSourceIds.isEmpty()) return emptyList()

        val blockedVideoIds = history.filter { entry ->
            entry.notInterested || entry.reported || (nowMillis - entry.watchedAtMillis) < SEVEN_DAYS_MILLIS
        }.map { it.videoId }.toSet()

        val candidates = allVideos.filter { video ->
            video.sourceId in activeSourceIds &&
                video.videoId !in blockedVideoIds &&
                (selectedCategory == null || video.category == selectedCategory)
        }

        if (candidates.isEmpty()) return emptyList()

        // Group by source to normalize popularity per source
        val maxScoreBySource = candidates.groupBy { it.sourceId }.mapValues { (_, list) ->
            list.maxOf { rawPopularity(it) }.coerceAtLeast(1.0)
        }

        val scored = candidates.map { video ->
            val maxForSource = maxScoreBySource[video.sourceId] ?: 1.0
            val normalizedPopularity = rawPopularity(video) / maxForSource
            // Recency boost: recent videos (<= 7 days) get up to +0.35 weight
            val recencyBoost = max(0.0, (7 - video.publishedDaysAgo) / 7.0) * 0.35
            val weight = (normalizedPopularity + recencyBoost + 0.15).coerceAtLeast(0.05)
            video to weight
        }.toMutableList()

        // Weighted random sampling without replacement
        val ranked = mutableListOf<FeedVideo>()
        while (scored.isNotEmpty()) {
            val totalWeight = scored.sumOf { it.second }
            var pick = random.nextDouble() * totalWeight
            var chosenIndex = scored.lastIndex
            for (i in scored.indices) {
                pick -= scored[i].second
                if (pick <= 0.0) {
                    chosenIndex = i
                    break
                }
            }
            ranked.add(scored.removeAt(chosenIndex).first)
        }
        return ranked
    }

    fun rawPopularity(video: FeedVideo): Double {
        return (video.viewsCount * 1.0) + (video.likesCount * 5.0)
    }
}

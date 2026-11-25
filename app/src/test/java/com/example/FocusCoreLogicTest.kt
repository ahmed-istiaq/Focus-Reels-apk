package com.example

import com.example.model.ContentCategory
import com.example.model.FeedVideo
import com.example.model.SourcePlatform
import com.example.model.VideoSource
import com.example.model.WatchHistoryEntry
import com.example.util.FocusUtils
import com.example.util.PrayerTimeCalculator
import com.example.util.VideoRanker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class FocusCoreLogicTest {
    @Test
    fun extractYoutubeVideoId_supportsStandardShortsAndShortLinks() {
        assertEquals("dQw4w9WgXcQ", FocusUtils.extractYoutubeVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", FocusUtils.extractYoutubeVideoId("https://youtu.be/dQw4w9WgXcQ?si=abc"))
        assertEquals("dQw4w9WgXcQ", FocusUtils.extractYoutubeVideoId("https://youtube.com/shorts/dQw4w9WgXcQ"))
        assertNull(FocusUtils.extractYoutubeVideoId("https://youtube.com/@yaqeeninstitute"))
    }

    @Test
    fun videoRanker_excludesInactiveSourcesAnd7DayHistoryAndNotInterested() {
        val now = 1_750_000_000_000L
        val sources = listOf(
            VideoSource(id = "s1", name = "Active", active = true, category = ContentCategory.ISLAMIC.name),
            VideoSource(id = "s2", name = "Paused", active = false, category = ContentCategory.MOTIVATION.name)
        )
        val videos = listOf(
            FeedVideo("v1", "s1", "Recent Watched", "v1", "Active", SourcePlatform.YOUTUBE, ContentCategory.ISLAMIC, null, "", 1000, 100, 60, 1, 0, "", ""),
            FeedVideo("v2", "s1", "Eligible", "v2", "Active", SourcePlatform.YOUTUBE, ContentCategory.ISLAMIC, null, "", 500, 50, 60, 2, 1, "", ""),
            FeedVideo("v3", "s1", "Old Watched", "v3", "Active", SourcePlatform.YOUTUBE, ContentCategory.ISLAMIC, null, "", 900, 90, 60, 3, 2, "", ""),
            FeedVideo("v4", "s2", "From Paused Source", "v4", "Paused", SourcePlatform.YOUTUBE, ContentCategory.MOTIVATION, null, "", 99999, 9999, 60, 1, 3, "", "")
        )
        val history = listOf(
            WatchHistoryEntry(id = "v1", videoId = "v1", watchedAtMillis = now - 2L * 24 * 60 * 60 * 1000),
            WatchHistoryEntry(id = "v3", videoId = "v3", watchedAtMillis = now - 8L * 24 * 60 * 60 * 1000)
        )

        val ranked = VideoRanker.rankFeed(videos, sources, history, selectedCategory = null, nowMillis = now, random = Random(42))
        val ids = ranked.map { it.videoId }
        assertFalse(ids.contains("v1")) // Watched 2 days ago (< 7 days)
        assertFalse(ids.contains("v4")) // Paused source
        assertTrue(ids.contains("v2"))  // Unwatched active
        assertTrue(ids.contains("v3"))  // Watched 8 days ago (>= 7 days)
    }

    @Test
    fun prayerCalculator_returnsAllFivePrayersInOrder() {
        val status = PrayerTimeCalculator.calculate(23.8103, 90.4125)
        assertEquals(5, status.entries.size)
        for (i in 0 until 4) {
            assertTrue(status.entries[i].timeMinutesFromMidnight < status.entries[i + 1].timeMinutesFromMidnight)
        }
        assertNotNull(status.nextPrayer)
    }
}

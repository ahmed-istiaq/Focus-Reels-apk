package com.example.util

import com.example.model.SourcePlatform
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FocusUtils {
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dayLabelFormatter = SimpleDateFormat("EEE", Locale.US)

    fun todayDateKey(): String = dateFormatter.format(Date())

    fun last7DaysKeys(): List<Pair<String, String>> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        val result = mutableListOf<Pair<String, String>>()
        repeat(7) {
            val date = cal.time
            result.add(dateFormatter.format(date) to dayLabelFormatter.format(date))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return result
    }

    /**
     * Extracts a YouTube 11-character video ID from standard watch, youtu.be, shorts, or embed URLs.
     */
    fun extractYoutubeVideoId(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        val patterns = listOf(
            Regex("""(?:youtube\.com/watch\?.*?v=|youtu\.be/|youtube\.com/shorts/|youtube\.com/embed/)([A-Za-z0-9_-]{11})"""),
            Regex("""^([A-Za-z0-9_-]{11})$""")
        )
        for (pattern in patterns) {
            val match = pattern.find(trimmed)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }

    fun inferChannelName(handleOrUrl: String, fallback: String): String {
        val trimmed = handleOrUrl.trim()
        if (trimmed.isEmpty()) return fallback
        val atHandle = Regex("""@([A-Za-z0-9_.-]+)""").find(trimmed)?.groupValues?.getOrNull(1)
        if (!atHandle.isNullOrBlank()) {
            return atHandle.replace('_', ' ').replace('-', ' ')
                .split(' ')
                .filter { it.isNotBlank() }
                .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
                .take(40)
        }
        val clean = trimmed
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .substringAfter("/", "")
            .substringBefore("?")
            .trim('/')
            .substringAfterLast("/")
        return if (clean.isNotBlank() && clean.length <= 40) {
            clean.replace('-', ' ').replace('_', ' ')
                .split(' ')
                .filter { it.isNotBlank() }
                .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
        } else {
            fallback
        }
    }

    fun detectPlatform(input: String): SourcePlatform {
        val lower = input.lowercase(Locale.US)
        return if (lower.contains("facebook.com") || lower.contains("fb.watch") || lower.contains("fb.com")) {
            SourcePlatform.FACEBOOK
        } else {
            SourcePlatform.YOUTUBE
        }
    }

    fun formatCompactCount(count: Long): String {
        return when {
            count >= 1_000_000L -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
            count >= 1_000L -> String.format(Locale.US, "%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }

    fun formatDuration(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.US, "%d:%02d", mins, secs)
    }

    fun sanitizeFirestoreId(raw: String): String {
        return raw.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(100).ifBlank { "item_${System.currentTimeMillis()}" }
    }
}

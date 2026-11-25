package com.example.data

import com.example.model.ContentCategory
import com.example.model.DailyQuote
import com.example.model.FeedVideo
import com.example.model.SourcePlatform
import com.example.model.VideoSource
import java.util.Calendar

data class StarterChannelTemplate(
    val id: String,
    val name: String,
    val handleOrUrl: String,
    val platform: SourcePlatform,
    val category: ContentCategory,
    val gradientIndex: Int
)

object CuratedCatalog {
    val starterChannels = listOf(
        StarterChannelTemplate(
            id = "src_yaqeen",
            name = "Yaqeen Institute",
            handleOrUrl = "https://youtube.com/@yaqeeninstituteofficial",
            platform = SourcePlatform.YOUTUBE,
            category = ContentCategory.ISLAMIC,
            gradientIndex = 0
        ),
        StarterChannelTemplate(
            id = "src_bayyinah",
            name = "Bayyinah Institute",
            handleOrUrl = "https://youtube.com/@bayyinah",
            platform = SourcePlatform.YOUTUBE,
            category = ContentCategory.ISLAMIC,
            gradientIndex = 1
        ),
        StarterChannelTemplate(
            id = "src_deep_focus",
            name = "Deep Focus Lab",
            handleOrUrl = "https://youtube.com/@deepfocuslab",
            platform = SourcePlatform.YOUTUBE,
            category = ContentCategory.SELF_BUILDING,
            gradientIndex = 2
        ),
        StarterChannelTemplate(
            id = "src_fajr_habits",
            name = "Fajr & Discipline",
            handleOrUrl = "https://youtube.com/@fajranddiscipline",
            platform = SourcePlatform.YOUTUBE,
            category = ContentCategory.MOTIVATION,
            gradientIndex = 3
        ),
        StarterChannelTemplate(
            id = "src_skill_craft",
            name = "SkillCraft Studio",
            handleOrUrl = "https://facebook.com/skillcraftstudio",
            platform = SourcePlatform.FACEBOOK,
            category = ContentCategory.SELF_BUILDING,
            gradientIndex = 4
        ),
        StarterChannelTemplate(
            id = "src_hikmah_notes",
            name = "Hikmah Daily",
            handleOrUrl = "https://facebook.com/hikmahdaily",
            platform = SourcePlatform.FACEBOOK,
            category = ContentCategory.ISLAMIC,
            gradientIndex = 5
        )
    )

    val dailyQuotes = listOf(
        DailyQuote(
            sourceTypeEn = "Ayah of the Day",
            sourceTypeBn = "আজকের আয়াত",
            textEn = "And He found you lost and guided you.",
            textBn = "আর তিনি আপনাকে পথহারা পেয়েছিলেন, অতঃপর পথপ্রদর্শন করেছেন।",
            referenceEn = "Surah Ad-Duha 93:7",
            referenceBn = "সূরা আদ-দুহা ৯৩:৭"
        ),
        DailyQuote(
            sourceTypeEn = "Hadith Reflection",
            sourceTypeBn = "হাদিস শিক্ষা",
            textEn = "The most beloved deeds to Allah are those done consistently, even if small.",
            textBn = "আল্লাহর কাছে সবচেয়ে প্রিয় আমল হলো যা নিয়মিত করা হয়, যদিও তা অল্প হয়।",
            referenceEn = "Sahih al-Bukhari 6464",
            referenceBn = "সহীহ বুখারী ৬৪৬৪"
        ),
        DailyQuote(
            sourceTypeEn = "Focus Principle",
            sourceTypeBn = "মনোযোগ নীতি",
            textEn = "Guard your attention as fiercely as your time; what feeds your eyes shapes your heart.",
            textBn = "সময়ের মতোই আপনার মনোযোগকে রক্ষা করুন; চোখ যা দেখে, হৃদয় তা-ই গ্রহণ করে।",
            referenceEn = "Mindful Discipline",
            referenceBn = "সচেতন জীবনচর্চা"
        ),
        DailyQuote(
            sourceTypeEn = "Ayah of Patience",
            sourceTypeBn = "ধৈর্যের বাণী",
            textEn = "Indeed, with hardship comes ease.",
            textBn = "নিশ্চয়ই কষ্টের সাথেই রয়েছে স্বস্তি।",
            referenceEn = "Surah Ash-Sharh 94:6",
            referenceBn = "সূরা আল-ইনশিরাহ ৯৪:৬"
        )
    )

    fun todayQuote(): DailyQuote {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return dailyQuotes[dayOfYear % dailyQuotes.size]
    }

    // Realistic educational prototype reels (no invented real YouTube IDs; gradient visuals used for mock items)
    private val curatedSeedVideos: List<FeedVideo> = listOf(
        FeedVideo(
            videoId = "vid_yaqeen_1",
            sourceId = "src_yaqeen",
            titleEn = "Protecting Your Heart From Digital Distraction",
            titleBn = "ডিজিটাল আসক্তি থেকে হৃদয়কে রক্ষা করার উপায়",
            channelName = "Yaqeen Institute",
            platform = SourcePlatform.YOUTUBE,
            category = ContentCategory.ISLAMIC,
            youtubeVideoId = null,
            videoUrl = "https://youtube.com/@yaqeeninstituteofficial",
            viewsCount = 148_200L,
            likesCount = 14_300L,
            durationSeconds = 68,
            publishedDaysAgo = 2,
            gradientIndex = 0,
            takeawayPromptEn = "Which app drains your quiet morning focus?",
            takeawayPromptBn = "কোন অ্যাপটি আপনার সকালের শান্তি নষ্ট করে?"
        ),
        FeedVideo(
            videoId = "vid_yaqeen_2",
            sourceId = "src_yaqeen",
            titleEn = "Why Small Sincere Duas Outlast Big Plans",
            titleBn = "অল্প কিন্তু আন্তরিক দোয়া কেন দীর্ঘস্থায়ী প্রভাব ফেলে",
            channelName = "Yaqeen Institute",
            platform = SourcePlatform.YOUTUBE,
            category = ContentCategory.ISLAMIC,
            youtubeVideoId = null,
            videoUrl = "https://youtube.com/@yaqeeninstituteofficial",
            viewsCount = 92_400L,
            likesCount = 11_200L,
            durationSeconds = 54,
            publishedDaysAgo = 5,
            gradientIndex = 1,
            takeawayPromptEn = "Write one dua to repeat after every salah today.",
            takeawayPromptBn = "প্রতি নামাজের পর পড়ার জন্য একটি দোয়া লিখুন।"
        ),
        FeedVideo(
            videoId = "vid_bayyinah_1",
            sourceId = "src_bayyinah",
            titleEn = "One Word in Surah Al-Fatiha That Changes Your Day",
            titleBn = "সূরা ফাতিহার একটি শব্দ যা আপনার দিন বদলে দেবে",
            channelName = "Bayyinah Institute",
            platform = SourcePlatform.YOUTUBE,
            category = ContentCategory.ISLAMIC,
            youtubeVideoId = null,
            videoUrl = "https://youtube.com/@bayyinah",
            viewsCount = 210_500L,
            likesCount = 22_100L,
            durationSeconds = 72,
            publishedDaysAgo = 1,
            gradientIndex = 2,
            takeawayPromptEn = "Reflect on Iyyaka na'budu in your next task.",
            takeawayPromptBn = "পরবর্তী কাজে 'ইয়্যাকা নাবুদু'-র অর্থ অনুভব করুন।"
        ),
        FeedVideo(
            videoId = "vid_bayyinah_2",
            sourceId = "src_bayyinah",
            titleEn = "How to Build a 10-Minute Daily Quran Habit",
            titleBn = "দৈনিক ১০ মিনিটের কুরআন অধ্যয়নের অভ্যাস গড়বেন যেভাবে",
            channelName = "Bayyinah Institute",
            platform = SourcePlatform.YOUTUBE,
            category = ContentCategory.ISLAMIC,
            youtubeVideoId = null,
            videoUrl = "https://youtube.com/@bayyinah",
            viewsCount = 87_000L,
            likesCount = 9_800L,
            durationSeconds = 60,
            publishedDaysAgo = 4,
            gradientIndex = 3,
            takeawayPromptEn = "Anchor 10 minutes right after Fajr.",
            takeawayPromptBn = "ফজরের পরপরই ১০ মিনিট কুরআনের জন্য নির্ধারণ করুন।"
        ),
        FeedVideo(
            videoId = "vid_deep_focus_1",
            sourceId = "src_deep_focus",
            titleEn = "The 20-Minute Attention Reset Protocol",
            titleBn = "২০ মিনিটে মনোযোগ ফিরিয়ে আনার বৈজ্ঞানিক নিয়ম",
            channelName = "Deep Focus Lab",
            platform = SourcePlatform.YOUTUBE,
            category = ContentCategory.SELF_BUILDING,
            youtubeVideoId = null,
            videoUrl = "https://youtube.com/@deepfocuslab",
            viewsCount = 64_800L,
            likesCount = 7_450L,
            durationSeconds = 58,
            publishedDaysAgo = 1,
            gradientIndex = 4,
            takeawayPromptEn = "Try one 20-minute phone-free work block.",
            takeawayPromptBn = "ফোন দূরে রেখে ২০ মিনিটের একটি ফোকাস ব্লক চেষ্টা করুন।"
        ),
        FeedVideo(
            videoId = "vid_deep_focus_2",
            sourceId = "src_deep_focus",
            titleEn = "Why Boredom Is Fuel for Creative Clarity",
            titleBn = "নিরবতা ও অবসর কেন সৃজনশীল চিন্তার জ্বালানি",
            channelName = "Deep Focus Lab",
            platform = SourcePlatform.YOUTUBE,
            category = ContentCategory.SELF_BUILDING,
            youtubeVideoId = null,
            videoUrl = "https://youtube.com/@deepfocuslab",
            viewsCount = 41_300L,
            likesCount = 5_100L,
            durationSeconds = 65,
            publishedDaysAgo = 6,
            gradientIndex = 5,
            takeawayPromptEn = "Take a 5-minute walk without audio today.",
            takeawayPromptBn = "আজ হেডফোন ছাড়া ৫ মিনিট হাঁটুন।"
        ),
        FeedVideo(
            videoId = "vid_fajr_1",
            sourceId = "src_fajr_habits",
            titleEn = "Win the First Hour Before the World Wakes Up",
            titleBn = "পৃথিবী জেগে ওঠার আগেই দিনের প্রথম ঘণ্টা জয় করুন",
            channelName = "Fajr & Discipline",
            platform = SourcePlatform.YOUTUBE,
            category = ContentCategory.MOTIVATION,
            youtubeVideoId = null,
            videoUrl = "https://youtube.com/@fajranddiscipline",
            viewsCount = 53_900L,
            likesCount = 6_900L,
            durationSeconds = 49,
            publishedDaysAgo = 2,
            gradientIndex = 6,
            takeawayPromptEn = "Set tomorrow's top 1 priority tonight.",
            takeawayPromptBn = "আগামীকালের সবচেয়ে জরুরি ১টি কাজ আজ রাতে ঠিক করুন।"
        ),
        FeedVideo(
            videoId = "vid_fajr_2",
            sourceId = "src_fajr_habits",
            titleEn = "Replace Doomscrolling With One Deliberate Action",
            titleBn = "অকারণ স্ক্রলিংয়ের বদলে একটি সচেতন অভ্যাস গড়ুন",
            channelName = "Fajr & Discipline",
            platform = SourcePlatform.YOUTUBE,
            category = ContentCategory.MOTIVATION,
            youtubeVideoId = null,
            videoUrl = "https://youtube.com/@fajranddiscipline",
            viewsCount = 38_200L,
            likesCount = 4_700L,
            durationSeconds = 52,
            publishedDaysAgo = 3,
            gradientIndex = 7,
            takeawayPromptEn = "Keep a physical book next to your charger.",
            takeawayPromptBn = "ফোন চার্জারের পাশে একটি বই রাখুন।"
        ),
        FeedVideo(
            videoId = "vid_skill_1",
            sourceId = "src_skill_craft",
            titleEn = "Deliberate Practice: Learn Any Skill 2x Faster",
            titleBn = "সচেতন অনুশীলন: যেকোনো দক্ষতা দ্রুত শেখার কৌশল",
            channelName = "SkillCraft Studio",
            platform = SourcePlatform.FACEBOOK,
            category = ContentCategory.SELF_BUILDING,
            youtubeVideoId = null,
            videoUrl = "https://facebook.com/skillcraftstudio",
            viewsCount = 29_400L,
            likesCount = 3_150L,
            durationSeconds = 62,
            publishedDaysAgo = 2,
            gradientIndex = 3,
            takeawayPromptEn = "Practice the hardest 20% of your skill first.",
            takeawayPromptBn = "আপনার কাজের সবচেয়ে কঠিন ২০% আগে অনুশীলন করুন।"
        ),
        FeedVideo(
            videoId = "vid_hikmah_1",
            sourceId = "src_hikmah_notes",
            titleEn = "Gratitude Shifts Anxiety Into Barakah",
            titleBn = "শুকরিয়া কীভাবে দুশ্চিন্তাকে বরকতে রূপান্তর করে",
            channelName = "Hikmah Daily",
            platform = SourcePlatform.FACEBOOK,
            category = ContentCategory.ISLAMIC,
            youtubeVideoId = null,
            videoUrl = "https://facebook.com/hikmahdaily",
            viewsCount = 34_100L,
            likesCount = 4_850L,
            durationSeconds = 45,
            publishedDaysAgo = 1,
            gradientIndex = 1,
            takeawayPromptEn = "Name 3 quiet blessings before sleeping.",
            takeawayPromptBn = "ঘুমানোর আগে আজকের ৩টি নিয়ামত স্মরণ করুন।"
        )
    )

    fun buildFeedForSources(sources: List<VideoSource>): List<FeedVideo> {
        val result = mutableListOf<FeedVideo>()
        sources.forEach { source ->
            val matching = curatedSeedVideos.filter { it.sourceId == source.id }
            if (matching.isNotEmpty()) {
                result.addAll(matching.map { it.copy(category = source.categoryEnum()) })
            } else {
                // Generate realistic channel reels for user-added channels
                val cat = source.categoryEnum()
                val grad = source.gradientIndex.toInt().coerceIn(0, 7)
                result.add(
                    FeedVideo(
                        videoId = "${source.id}_reel_1",
                        sourceId = source.id,
                        titleEn = "${source.name}: Clarity Over Noise",
                        titleBn = "${source.name}: কোলাহল নয়, স্পষ্টতা",
                        channelName = source.name,
                        platform = source.platformEnum(),
                        category = cat,
                        youtubeVideoId = null,
                        videoUrl = source.handleOrUrl,
                        viewsCount = 24_600L,
                        likesCount = 2_900L,
                        durationSeconds = 56,
                        publishedDaysAgo = 1,
                        gradientIndex = grad,
                        takeawayPromptEn = "Capture one key idea from ${source.name}.",
                        takeawayPromptBn = "${source.name} থেকে একটি মূল শিক্ষা লিখে রাখুন।"
                    )
                )
                result.add(
                    FeedVideo(
                        videoId = "${source.id}_reel_2",
                        sourceId = source.id,
                        titleEn = "${source.name}: Small Steps Every Morning",
                        titleBn = "${source.name}: প্রতি সকালে ছোট কিন্তু দৃঢ় পদক্ষেপ",
                        channelName = source.name,
                        platform = source.platformEnum(),
                        category = cat,
                        youtubeVideoId = null,
                        videoUrl = source.handleOrUrl,
                        viewsCount = 18_200L,
                        likesCount = 2_350L,
                        durationSeconds = 64,
                        publishedDaysAgo = 3,
                        gradientIndex = (grad + 2) % 8,
                        takeawayPromptEn = "Apply this lesson before Dhuhr today.",
                        takeawayPromptBn = "আজ যোহরের আগেই এই শিক্ষাটি বাস্তবায়ন করুন।"
                    )
                )
            }
        }
        return result
    }
}

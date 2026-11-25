package com.example.util

import java.util.Calendar
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.tan

data class CityCoordinate(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val tzOffsetHours: Double
)

enum class PrayerName(val en: String, val bn: String) {
    FAJR("Fajr", "ফজর"),
    DHUHR("Dhuhr", "যোহর"),
    ASR("Asr", "আসর"),
    MAGHRIB("Maghrib", "মাগরিব"),
    ISHA("Isha", "এশা")
}

data class PrayerScheduleEntry(
    val prayer: PrayerName,
    val timeMinutesFromMidnight: Int,
    val formattedTime: String
)

data class PrayerStatus(
    val entries: List<PrayerScheduleEntry>,
    val activePrayerWindow: PrayerName?,
    val nextPrayer: PrayerName,
    val minutesUntilNext: Int
)

/**
 * Astronomical solar position prayer-time calculator (MWL/Karachi standard angles).
 * Uses city timezone offset when coordinate is distant from device timezone so
 * local prayer times always stay in chronological order (Fajr -> Dhuhr -> Asr -> Maghrib -> Isha).
 */
object PrayerTimeCalculator {
    val presetCities = listOf(
        CityCoordinate("Dhaka", 23.8103, 90.4125, 6.0),
        CityCoordinate("Chattogram", 22.3569, 91.7832, 6.0),
        CityCoordinate("Makkah", 21.3891, 39.8579, 3.0),
        CityCoordinate("Madinah", 24.5247, 39.5692, 3.0),
        CityCoordinate("London", 51.5072, -0.1276, 0.0),
        CityCoordinate("New York", 40.7128, -74.0060, -5.0),
        CityCoordinate("Kuala Lumpur", 3.1390, 101.6869, 8.0),
        CityCoordinate("Istanbul", 41.0082, 28.9784, 3.0)
    )

    fun calculate(
        latitude: Double,
        longitude: Double,
        nowCal: Calendar = Calendar.getInstance(),
        explicitTzHours: Double? = null
    ): PrayerStatus {
        val year = nowCal.get(Calendar.YEAR)
        val month = nowCal.get(Calendar.MONTH) + 1
        val day = nowCal.get(Calendar.DAY_OF_MONTH)
        val deviceTzHours = nowCal.timeZone.getOffset(nowCal.timeInMillis) / 3_600_000.0
        val matchedCity = presetCities.firstOrNull {
            abs(it.latitude - latitude) < 0.05 && abs(it.longitude - longitude) < 0.05
        }
        val tzOffsetHours = explicitTzHours
            ?: matchedCity?.tzOffsetHours
            ?: if (abs(deviceTzHours - longitude / 15.0) <= 2.5) deviceTzHours else round(longitude / 15.0)

        val jd = julianDate(year, month, day) - longitude / (15.0 * 24.0)
        val d = jd - 2451545.0

        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sinDeg(g) + 0.020 * sinDeg(2.0 * g))
        val e = 23.439 - 0.00000036 * d

        val ra = radToDeg(atan2(cosDeg(e) * sinDeg(l), cosDeg(l))) / 15.0
        val decl = radToDeg(asin(sinDeg(e) * sinDeg(l)))
        val eqTime = q / 15.0 - fixHour(ra)

        val dhuhrHours = 12.0 + tzOffsetHours - longitude / 15.0 - eqTime
        val fajrHours = dhuhrHours - sunAngleTime(18.0, latitude, decl)
        val maghribHours = dhuhrHours + sunAngleTime(0.833, latitude, decl)
        val ishaHours = dhuhrHours + sunAngleTime(18.0, latitude, decl)
        val asrHours = dhuhrHours + asrTime(1.0, latitude, decl)

        val entries = listOf(
            PrayerName.FAJR to fajrHours,
            PrayerName.DHUHR to dhuhrHours + (2.0 / 60.0),
            PrayerName.ASR to asrHours,
            PrayerName.MAGHRIB to maghribHours,
            PrayerName.ISHA to ishaHours
        ).map { (name, hours) ->
            val mins = ((fixHour(hours) * 60.0).toInt()).coerceIn(0, 23 * 60 + 59)
            PrayerScheduleEntry(
                prayer = name,
                timeMinutesFromMidnight = mins,
                formattedTime = formatMinutes(mins)
            )
        }

        val nowMinutes = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)
        val activeWindow = entries.firstOrNull { entry ->
            nowMinutes >= entry.timeMinutesFromMidnight && nowMinutes < entry.timeMinutesFromMidnight + 15
        }?.prayer

        val nextEntry = entries.firstOrNull { it.timeMinutesFromMidnight > nowMinutes }
        val nextPrayer = nextEntry?.prayer ?: PrayerName.FAJR
        val minutesUntil = if (nextEntry != null) {
            nextEntry.timeMinutesFromMidnight - nowMinutes
        } else {
            (24 * 60 - nowMinutes) + entries.first().timeMinutesFromMidnight
        }

        return PrayerStatus(
            entries = entries,
            activePrayerWindow = activeWindow,
            nextPrayer = nextPrayer,
            minutesUntilNext = minutesUntil.coerceAtLeast(1)
        )
    }

    private fun sunAngleTime(angle: Double, lat: Double, decl: Double): Double {
        val term = (-sinDeg(angle) - sinDeg(lat) * sinDeg(decl)) / (cosDeg(lat) * cosDeg(decl))
        val clamped = term.coerceIn(-0.999, 0.999)
        return radToDeg(acos(clamped)) / 15.0
    }

    private fun asrTime(factor: Double, lat: Double, decl: Double): Double {
        val diff = abs(lat - decl)
        val angle = -radToDeg(atan(1.0 / (factor + tan(degToRad(diff)))))
        return sunAngleTime(angle, lat, decl)
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun formatMinutes(totalMinutes: Int): String {
        val h24 = (totalMinutes / 60) % 24
        val m = totalMinutes % 60
        val amPm = if (h24 < 12) "AM" else "PM"
        val h12 = when {
            h24 == 0 -> 12
            h24 > 12 -> h24 - 12
            else -> h24
        }
        return String.format(java.util.Locale.US, "%d:%02d %s", h12, m, amPm)
    }

    private fun degToRad(d: Double) = d * PI / 180.0
    private fun radToDeg(r: Double) = r * 180.0 / PI
    private fun sinDeg(d: Double) = sin(degToRad(d))
    private fun cosDeg(d: Double) = cos(degToRad(d))
    private fun fixAngle(a: Double): Double {
        var v = a - 360.0 * floor(a / 360.0)
        if (v < 0) v += 360.0
        return v
    }
    private fun fixHour(a: Double): Double {
        var v = a - 24.0 * floor(a / 24.0)
        if (v < 0) v += 24.0
        return v
    }
}

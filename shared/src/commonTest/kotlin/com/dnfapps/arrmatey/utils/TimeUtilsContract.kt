package com.dnfapps.arrmatey.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

// Abstract so each platform supplies its own runner; Android needs Robolectric for android.icu.
abstract class TimeUtilsContract {
    @Test
    fun testGetCurrentSystemTimeMillisReturnsPlausibleEpochMillis() {
        val now = getCurrentSystemTimeMillis()

        // After 2020-01-01 and before 2100-01-01, which catches seconds/millis unit mix-ups
        assertTrue(now > 1_577_836_800_000L, "expected epoch millis, got $now")
        assertTrue(now < 4_102_444_800_000L, "expected epoch millis, got $now")
    }

    @Test
    fun testLocalDateFormatUsesSuppliedPattern() {
        val date = LocalDate(2024, 8, 25)

        assertEquals("2024-08-25", date.format("yyyy-MM-dd"))
    }

    @Test
    fun testLocalDateFormatIsNotShiftedByLocalTimeZone() {
        // LocalDate formatting is pinned to UTC, otherwise dates roll over west of GMT
        assertEquals("2024-01-01", LocalDate(2024, 1, 1).format("yyyy-MM-dd"))
        assertEquals("2024-12-31", LocalDate(2024, 12, 31).format("yyyy-MM-dd"))
    }

    @Test
    fun testFormatLocalDateTimeUsesSuppliedTimeZone() {
        val localDateTime = LocalDateTime(2024, 8, 25, 13, 30, 0)

        assertEquals("2024-08-25 13:30", formatLocalDateTime(localDateTime, "yyyy-MM-dd HH:mm", TimeZone.UTC))
    }

    @Test
    fun testFormatLocalDateTimePreservesWallClockAcrossZones() {
        val localDateTime = LocalDateTime(2024, 8, 25, 13, 30, 0)

        val newYork = formatLocalDateTime(localDateTime, "yyyy-MM-dd HH:mm", TimeZone.of("America/New_York"))
        val tokyo = formatLocalDateTime(localDateTime, "yyyy-MM-dd HH:mm", TimeZone.of("Asia/Tokyo"))

        assertEquals("2024-08-25 13:30", newYork)
        assertEquals("2024-08-25 13:30", tokyo)
    }

    @Test
    fun testInstantFormatProducesRequestedShape() {
        val instant = Instant.fromEpochMilliseconds(1_724_594_400_000L)

        // Exact date depends on the host time zone, so assert structure rather than value
        assertTrue(Regex("""\d{4}-\d{2}-\d{2}""").matches(instant.format("yyyy-MM-dd")))
    }

    @Test
    fun testIs24HourDoesNotThrow() {
        is24Hour()
    }
}

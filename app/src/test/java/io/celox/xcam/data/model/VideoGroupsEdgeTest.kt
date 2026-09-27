package io.celox.xcam.data.model

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VideoGroupsEdgeTest {
    private val zone = ZoneId.of("Europe/Berlin")

    private fun at(
        date: LocalDate,
        time: LocalTime,
        id: Long,
    ) = VideoFile(
        id = id,
        uri = Uri.parse("content://media/external/video/media/$id"),
        name = "$id.mp4",
        size = 0,
        timestamp = LocalDateTime.of(date, time).atZone(zone).toInstant().toEpochMilli(),
    )

    @Test
    fun `yesterday across a year boundary`() {
        val today = LocalDate.of(2027, 1, 1)
        val v = at(LocalDate.of(2026, 12, 31), LocalTime.of(22, 0), 1)
        assertEquals(DayGroup.Yesterday, groupByDay(listOf(v), today, zone).single().first)
    }

    @Test
    fun `a recording dated after today (clock skew) gets its own dated group, not Today`() {
        val today = LocalDate.of(2026, 9, 26)
        val v = at(today.plusDays(1), LocalTime.NOON, 1)
        assertEquals(DayGroup.Day(today.plusDays(1)), groupByDay(listOf(v), today, zone).single().first)
    }

    @Test
    fun `just before and just after midnight land on different days`() {
        val today = LocalDate.of(2026, 9, 26)
        val videos = listOf(at(today, LocalTime.of(0, 0, 1), 1), at(today.minusDays(1), LocalTime.of(23, 59, 59), 2))
        assertEquals(listOf(DayGroup.Today, DayGroup.Yesterday), groupByDay(videos, today, zone).map { it.first })
    }

    @Test
    fun `the DST change night still groups by local day`() {
        // 2026-10-25: Europe/Berlin falls back from 03:00 to 02:00.
        val today = LocalDate.of(2026, 10, 25)
        val videos = listOf(at(today, LocalTime.of(1, 30), 1), at(today, LocalTime.of(4, 0), 2))
        val groups = groupByDay(videos, today, zone)
        assertEquals(1, groups.size)
        assertEquals(listOf(2L, 1L), groups.single().second.map { it.id })
    }

    @Test
    fun `many days come out newest first and nothing is lost`() {
        val today = LocalDate.of(2026, 9, 26)
        val videos = (0L until 30L).map { at(today.minusDays(it * 3 % 17), LocalTime.of((it % 24).toInt(), 0), it) }
        val groups = groupByDay(videos.shuffled(java.util.Random(1)), today, zone)
        assertEquals(videos.size, groups.sumOf { it.second.size })
        val days = groups.map { (_, items) -> java.time.Instant.ofEpochMilli(items.first().timestamp).atZone(zone).toLocalDate() }
        assertEquals(days.sortedDescending(), days)
        groups.forEach { (_, items) -> assertEquals(items.sortedByDescending { it.timestamp }, items) }
    }

    @Test
    fun `the same instant groups differently in different zones`() {
        val today = LocalDate.of(2026, 9, 26)
        val v = at(today, LocalTime.of(1, 0), 1) // 23:00 UTC of the day before
        assertEquals(DayGroup.Today, groupByDay(listOf(v), today, zone).single().first)
        assertEquals(DayGroup.Yesterday, groupByDay(listOf(v), today, ZoneId.of("UTC")).single().first)
    }
}

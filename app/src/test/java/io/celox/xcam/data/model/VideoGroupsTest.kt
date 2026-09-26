package io.celox.xcam.data.model

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VideoGroupsTest {
    private val zone = ZoneId.of("Europe/Berlin")
    private val today = LocalDate.of(2026, 9, 26)

    private fun at(
        date: LocalDate,
        hour: Int,
        id: Long,
    ) = VideoFile(
        id = id,
        uri = Uri.parse("content://media/external/video/media/$id"),
        name = "$id.mp4",
        size = 0,
        timestamp = LocalDateTime.of(date, java.time.LocalTime.of(hour, 0)).atZone(zone).toInstant().toEpochMilli(),
    )

    @Test
    fun `groups into today, yesterday and dated days, newest first`() {
        val videos =
            listOf(
                at(today.minusDays(5), 10, 1),
                at(today, 9, 2),
                at(today.minusDays(1), 23, 3),
                at(today, 18, 4),
            )
        val groups = groupByDay(videos, today, zone)
        assertEquals(
            listOf(DayGroup.Today, DayGroup.Yesterday, DayGroup.Day(today.minusDays(5))),
            groups.map { it.first },
        )
        assertEquals(listOf(4L, 2L), groups[0].second.map { it.id })
    }

    @Test
    fun `day boundaries follow the given time zone, not UTC`() {
        // 00:30 Berlin time is still the previous day in UTC — it must count as today.
        val video = at(today, 0, 7).copy(timestamp = at(today, 0, 7).timestamp + 30 * 60_000)
        assertEquals(DayGroup.Today, groupByDay(listOf(video), today, zone).single().first)
    }

    @Test
    fun `empty input gives no groups`() {
        assertEquals(emptyList<Pair<DayGroup, List<VideoFile>>>(), groupByDay(emptyList(), today, zone))
    }
}

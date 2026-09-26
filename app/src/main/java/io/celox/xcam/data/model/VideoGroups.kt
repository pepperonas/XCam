package io.celox.xcam.data.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** A section header in the recordings list. */
sealed class DayGroup {
    data object Today : DayGroup()

    data object Yesterday : DayGroup()

    data class Day(val date: LocalDate) : DayGroup()
}

/**
 * Groups [videos] by the calendar day they were recorded on in [zone], newest day first and newest
 * video first within a day. Pure (the clock is a parameter) so it can be pinned by unit tests.
 */
fun groupByDay(
    videos: List<VideoFile>,
    today: LocalDate,
    zone: ZoneId,
): List<Pair<DayGroup, List<VideoFile>>> =
    videos
        .sortedByDescending { it.timestamp }
        .groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
        .entries
        .sortedByDescending { it.key }
        .map { (date, items) ->
            val group =
                when (date) {
                    today -> DayGroup.Today
                    today.minusDays(1) -> DayGroup.Yesterday
                    else -> DayGroup.Day(date)
                }
            group to items
        }

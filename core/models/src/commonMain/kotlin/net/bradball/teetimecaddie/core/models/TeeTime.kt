package net.bradball.teetimecaddie.core.models

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.ExperimentalTime

data class TeeTime(
    val id: String?,
    val createdBy: String,
    val course: String,
    val date: LocalDate,
    val time: LocalTime,
    val numberOfPlayers: Int
)

val TeeTime.shortDate: String
    get() = "${date.month.name.take(3)}\n${date.dayOfMonth}"

@OptIn(ExperimentalTime::class)
val previewTeeTime = TeeTime(
    id = "previewTime",
    createdBy = "Brad",
    course = "Persimmon Ridge",
    date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    time = LocalTime(9, 0),
    numberOfPlayers = 4
)

val previewTeeTimeList = listOf(
    previewTeeTime,
    previewTeeTime,
    previewTeeTime,
    previewTeeTime
)
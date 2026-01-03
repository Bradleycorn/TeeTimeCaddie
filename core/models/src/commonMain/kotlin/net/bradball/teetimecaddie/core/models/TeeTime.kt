package net.bradball.teetimecaddie.core.models

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.ExperimentalTime

data class TeeTime(
    val id: String?,
    val createdBy: String,
    val course: String,
    val dateTime: LocalDate
)

val TeeTime.shortDate: String
    get() = "${dateTime.month.name.take(3)}\n${dateTime.dayOfMonth}"

@OptIn(ExperimentalTime::class)
val previewTeeTime = TeeTime(
    id = "previewTime",
    createdBy = "Brad",
    course = "Persimmon Ridge",
    dateTime = Clock.System.todayIn(TimeZone.currentSystemDefault())
)

val previewTeeTimeList = listOf(
    previewTeeTime,
    previewTeeTime,
    previewTeeTime,
    previewTeeTime
)
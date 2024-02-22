package net.bradball.teetimecaddie.core.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class TeeTime(
    val id: String?,
    val createdBy: String,
    val course: String,
    val dateTime: LocalDate
)

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
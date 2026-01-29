package net.bradbal.teetimecaddie.core.storage.documents

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.bradball.teetimecaddie.core.models.TeeTime


@Serializable
data class TeeTimeDocument(
    val createdBy: String,
    val course: String,
    val date: LocalDate,
    val time: LocalTime,
    val numberOfPlayers: Int
) {

    @Transient
    var id: String? = null
}

fun TeeTimeDocument.asModel(): TeeTime {
    return TeeTime(
        id = id,
        createdBy = createdBy,
        course = course,
        date = date,
        time = time,
        numberOfPlayers = numberOfPlayers
    )
}
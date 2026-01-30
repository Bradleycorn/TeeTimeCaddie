package net.bradbal.teetimecaddie.core.storage.documents

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.bradball.teetimecaddie.core.models.TeeTime
import net.bradball.teetimecaddie.core.models.TeeTimeSlot

/**
 * Document model for storing a single tee time slot in Firestore.
 */
@Serializable
data class TeeTimeSlotDocument(
    val time: LocalTime,
    val numberOfPlayers: Int
)

/**
 * Document model for storing a tee time in Firestore.
 * Contains a list of time slots, each with its own time and player count.
 */
@Serializable
data class TeeTimeDocument(
    val createdBy: String,
    val course: String,
    val date: LocalDate,
    val times: List<TeeTimeSlotDocument>
) {

    @Transient
    var id: String? = null
}

/**
 * Converts a TeeTimeSlotDocument to a TeeTimeSlot model.
 */
fun TeeTimeSlotDocument.toModel(): TeeTimeSlot {
    return TeeTimeSlot(
        time = time,
        numberOfPlayers = numberOfPlayers
    )
}

/**
 * Converts a TeeTimeSlot model to a TeeTimeSlotDocument for storage.
 */
fun TeeTimeSlot.toDocument(): TeeTimeSlotDocument {
    return TeeTimeSlotDocument(
        time = time,
        numberOfPlayers = numberOfPlayers
    )
}

/**
 * Converts a TeeTimeDocument to a TeeTime model.
 */
fun TeeTimeDocument.toModel(): TeeTime {
    return TeeTime(
        id = id,
        createdBy = createdBy,
        course = course,
        date = date,
        times = times.map { it.toModel() }.sortedBy { it.time }
    )
}

/**
 * Converts a TeeTime model to a TeeTimeDocument for storage.
 */
fun TeeTime.toDocument(): TeeTimeDocument {
    return TeeTimeDocument(
        createdBy = createdBy,
        course = course,
        date = date,
        times = times.map { it.toDocument() }.sortedBy { it.time }
    )
}
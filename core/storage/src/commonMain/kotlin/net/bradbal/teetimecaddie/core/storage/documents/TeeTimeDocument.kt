package net.bradbal.teetimecaddie.core.storage.documents

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
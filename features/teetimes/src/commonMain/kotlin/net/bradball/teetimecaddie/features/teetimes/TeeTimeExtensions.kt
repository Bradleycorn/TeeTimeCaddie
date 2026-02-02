package net.bradball.teetimecaddie.features.teetimes

import net.bradbal.teetimecaddie.core.storage.documents.TeeTimeDocument
import net.bradbal.teetimecaddie.core.storage.documents.TeeTimeSlotDocument
import net.bradball.teetimecaddie.core.models.TeeTime
import net.bradball.teetimecaddie.core.models.TeeTimeSlot

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
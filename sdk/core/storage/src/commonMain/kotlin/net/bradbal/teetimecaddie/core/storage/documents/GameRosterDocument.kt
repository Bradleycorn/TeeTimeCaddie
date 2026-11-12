package net.bradbal.teetimecaddie.core.storage.documents

import kotlinx.serialization.Serializable

/**
 * The fields of a [GameDocument] that change when an invitee says yes: who holds a slot, and the
 * ids of those slot holders.
 *
 * Written as a partial update, so the fields a yes must leave alone (`organizerId`, `createdAt` and
 * the rest) are never rewritten.
 */
@Serializable
data class GameRosterDocument(
    val teeTimes: List<GameTeeTimeDocument>,
    val playerIds: List<String>
)

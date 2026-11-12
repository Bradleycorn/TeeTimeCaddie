package net.bradbal.teetimecaddie.core.storage.documents

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

/**
 * A player occupying a tee-time slot.
 *
 * @property playerId The seat holder's uid. Null for an invitee with no account whose Yes the
 *   organizer recorded.
 * @property name Copied from the player's profile when they join, or the name the organizer
 *   invited them by when the organizer recorded their Yes, so a game renders without reading any
 *   `players` documents — those hold email and phone numbers.
 * @property phone The seat holder's number, digits only: their profile's, or the one they were
 *   invited by. Identifies a seat that has no [playerId].
 */
@Serializable
data class GamePlayerDocument(
    val playerId: String?,
    val name: String,
    val phone: String
)

/**
 * One tee time within a game.
 *
 * @property time Stored as an ISO string ("09:00"), like every other [LocalTime] in storage.
 */
@Serializable
data class GameTeeTimeDocument(
    val time: LocalTime,
    val slots: Int,
    val players: List<GamePlayerDocument>
)

package net.bradbal.teetimecaddie.core.storage.documents

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

/**
 * A player occupying a tee-time slot.
 *
 * @property name Copied from the player's profile when they join, so a game renders without
 *   reading any `players` documents — those hold email and phone numbers.
 */
@Serializable
data class GamePlayerDocument(
    val playerId: String,
    val name: String
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

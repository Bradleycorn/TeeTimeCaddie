package net.bradball.teetimecaddie.features.games.state.gameDetailsUiState

import net.bradball.teetimecaddie.core.models.LocalizedText

/**
 * One tee time, formatted.
 *
 * @property time When the group tees off, e.g. "9:00 AM".
 * @property filledCount How full it is, e.g. "1/4 filled".
 * @property players One row per person on the tee time's roster: the organizer, then the others
 *   holding a slot in the order they took it (the order they said yes), then those invited who
 *   haven't answered, in the order they were invited. A new answer moves only that person's row.
 *   Never capped: there can be more rows than slots. Someone who declined isn't listed.
 * @property openSlots How many empty slots follow [players]: the slots no row has used, and never
 *   below zero.
 */
data class GameTeeTimeUiState(
    val time: String,
    val filledCount: LocalizedText,
    val players: List<TeeTimePlayerRowUiState>,
    val openSlots: Int
)

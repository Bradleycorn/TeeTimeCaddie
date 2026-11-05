package net.bradball.teetimecaddie.android.feature.games.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.session.SessionState

/**
 * The signed-in player, as the games screens see them, or null while nobody is signed in.
 *
 * Emits again only when something those screens use changes: who is signed in, or their phone
 * number or name. Invitations are matched by phone, and the player's own roster row shows their
 * first name. Other profile edits, such as a new avatar photo, don't restart the screens' listeners.
 */
fun Flow<SessionState>.signedInViewer(): Flow<Player?> = this
    .map { (it as? SessionState.SignedIn)?.player }
    .distinctUntilChanged { old, new ->
        old?.id == new?.id && old?.phone == new?.phone && old?.name == new?.name
    }

package net.bradball.teetimecaddie.features.players

import net.bradbal.teetimecaddie.core.storage.documents.PlayerDocument
import net.bradball.teetimecaddie.core.models.Player

/**
 * Convert a stored document to the model the apps see.
 *
 * `internal` on purpose: `:sdk:core:storage` is an implementation-only dependency of `:sdk`, so a
 * `PlayerDocument` appearing in any public signature would drag storage types across the iOS
 * framework boundary.
 */
internal fun PlayerDocument.toModel(): Player = Player(
    id = requireNotNull(id) {
        "PlayerDocument.id was not populated. Read players through PlayerStorage, which sets it."
    },
    name = name,
    email = email,
    phone = phone,
    photoUrl = photoUrl
)

internal fun Player.toDocument(): PlayerDocument = PlayerDocument(
    name = name,
    email = email,
    phone = phone,
    photoUrl = photoUrl
).also { it.id = id }

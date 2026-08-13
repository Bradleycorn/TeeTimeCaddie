package net.bradbal.teetimecaddie.core.storage

import kotlinx.serialization.encodeToString
import net.bradball.teetimecaddie.core.models.storage.FirestoreClient
import net.bradbal.teetimecaddie.core.storage.documents.PlayerDocument

private const val PLAYERS_COLLECTION = "players"

/**
 * Manage storage of players in the database.
 *
 * Persistence is delegated to the platform-provided [FirestoreClient]; this class owns the
 * mapping between [PlayerDocument] and the JSON representation stored in the cloud, so it remains
 * the single place to change if the storage backend is ever swapped.
 */
class PlayerStorage internal constructor(private val firestore: FirestoreClient) {

    /**
     * Add a Player to the database.
     *
     * @param id The Firebase Auth user id for the player.
     * @param document A [PlayerDocument] with information about the player to add.
     */
    suspend fun addPlayer(id: String, document: PlayerDocument) {
        firestore.set(PLAYERS_COLLECTION, id, storageJson.encodeToString(document))
    }
}

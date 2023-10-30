package net.bradbal.teetimecaddie.core.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.firestore
import net.bradbal.teetimecaddie.core.storage.documents.PlayerDocument

private const val PLAYERS_COLLECTION = "players"

/**
 * Manage storage of players in the database.
 */
class PlayerStorage {
    private val store = Firebase.firestore

    private val playersCollection: CollectionReference
        get() = store.collection(PLAYERS_COLLECTION)

    /**
     * Add a Player to the database.
     *
     * @param document A [PlayerDocument] with information about the player to add.
     *
     * @return The unique id assigned to the player.
     */
    suspend fun addPlayer(id:String, document: PlayerDocument) {
        playersCollection.document(id).set(document)
    }

}
package net.bradbal.teetimecaddie.core.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.bradbal.teetimecaddie.core.storage.documents.PlayerDocument

private const val PLAYERS_COLLECTION = "players"
private const val PHONE_FIELD = "phone"
private const val PHOTO_URL_FIELD = "photoUrl"

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
     * @param id The Firebase Auth user id for the player.
     * @param document A [PlayerDocument] with information about the player to add.
     */
    suspend fun addPlayer(id: String, document: PlayerDocument) {
        playersCollection.document(id).set(document)
    }

    /**
     * Read a single player.
     *
     * @param id The Firebase Auth user id for the player.
     * @return The player's [PlayerDocument], or null if they have no document yet — which is how a
     *   sign-up that was interrupted between creating the account and saving the profile is detected.
     */
    suspend fun getPlayer(id: String): PlayerDocument? {
        val doc = playersCollection.document(id).get()
        return if (doc.exists) {
            doc.data<PlayerDocument>().apply { this.id = doc.id }
        } else {
            null
        }
    }

    /**
     * Observe a single player, emitting again whenever their document changes.
     *
     * This is what lets a newly uploaded avatar appear on the Profile tab without a refresh.
     *
     * @param id The Firebase Auth user id for the player.
     */
    fun playerFlow(id: String): Flow<PlayerDocument?> {
        return playersCollection.document(id)
            .snapshots(includeMetadataChanges = false)
            .map { doc ->
                if (doc.exists) doc.data<PlayerDocument>().apply { this.id = doc.id } else null
            }
    }

    /**
     * Find the player who owns a phone number, if there is one.
     *
     * @param phone A digits-only phone number. Formatted input will never match.
     * @return The matching [PlayerDocument], or null if the number is not in use.
     */
    suspend fun findPlayerByPhone(phone: String): PlayerDocument? =
        playersCollection
            .where { PHONE_FIELD equalTo phone }
            .limit(1)
            .get()
            .documents
            .deserialize<PlayerDocument>(predicate = { doc -> id = doc.id })
            .firstOrNull()

    /**
     * Replace an existing player's document.
     *
     * @param id The Firebase Auth user id for the player.
     * @param document A [PlayerDocument] with the updated information.
     */
    suspend fun updatePlayer(id: String, document: PlayerDocument) {
        playersCollection.document(id).set(document)
    }

    /**
     * Write only the player's avatar URL, leaving every other field untouched.
     *
     * Uses the `mergeFields` overload rather than `merge = true` on purpose: GitLive encodes
     * defaults, so a merge of a document whose other fields are unset would write explicit nulls
     * over them.
     *
     * @param id The Firebase Auth user id for the player.
     * @param photoUrl The download URL of the player's avatar, or null to clear it.
     */
    suspend fun updatePlayerPhotoUrl(id: String, photoUrl: String?) {
        val current = getPlayer(id) ?: return
        playersCollection.document(id).set(current.copy(photoUrl = photoUrl), PHOTO_URL_FIELD)
    }
}

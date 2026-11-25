package net.bradbal.teetimecaddie.core.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
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
     * Emits null only once the **server** has confirmed there is no document. When Firestore can't
     * reach the server it answers from its local cache, and a cache with nothing in it reports the
     * document as missing — after a fresh install, or on any cold start without the on-disk cache.
     * Passing that on would claim the player has no profile when it was simply never read, so
     * nothing is emitted until the server answers.
     *
     * Metadata changes are included so that a server confirming a miss the cache already reported
     * still reaches this flow: without them, Firestore treats that as no change and stays silent.
     * The extra emissions are the same document again, and are dropped.
     *
     * @param id The Firebase Auth user id for the player.
     */
    fun playerFlow(id: String): Flow<PlayerDocument?> {
        return playersCollection.document(id)
            .snapshots(includeMetadataChanges = true)
            .filter { doc -> doc.exists || !doc.metadata.isFromCache }
            .map { doc ->
                if (doc.exists) doc.data<PlayerDocument>().apply { this.id = doc.id } else null
            }
            .distinctUntilChanged()
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

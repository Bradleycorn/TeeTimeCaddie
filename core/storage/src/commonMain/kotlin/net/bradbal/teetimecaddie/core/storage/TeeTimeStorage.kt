package net.bradbal.teetimecaddie.core.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.orderBy
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.bradbal.teetimecaddie.core.storage.documents.PlayerDocument
import net.bradbal.teetimecaddie.core.storage.documents.TeeTimeDocument

private const val TEETIME_COLLECTION = "teetimes"

/**
 * Manage storage of tee times in the database.
 */
class TeeTimeStorage {
    private val store = Firebase.firestore

    private val teeTimesCollection: CollectionReference
        get() = store.collection(TEETIME_COLLECTION)


    /**
     * Add a Tee Time to the database.
     *
     * @param document A [TeeTimeDocument] with information about the tee time to add.
     *
     * @return The unique id assigned to the tee time document.
     */
    suspend fun addTeeTime(document: TeeTimeDocument): String = teeTimesCollection.add(document).id

    suspend fun getTeeTimes(playerId: String): List<TeeTimeDocument> {
        return teeTimesCollection
            .where("createdBy", playerId)
            .get()
            .documents
            .deserialize()


//            .map { snapshot ->
//                snapshot.data<TeeTimeDocument>().apply { id = snapshot.id }
//            }
    }

    fun teeTimesFlow(playerId: String): Flow<List<TeeTimeDocument>> {
        return teeTimesCollection
            .where("createdBy", playerId)
            .orderBy("dateTime", Direction.ASCENDING)
            .snapshots(includeMetadataChanges = false)
            .map { snapshot ->
                snapshot.documents.deserialize()
            }
    }
}

inline fun <reified T : Any> List<DocumentSnapshot>.deserialize(): List<T> {
    return this.map { document ->
        document.data<T>()
    }
}
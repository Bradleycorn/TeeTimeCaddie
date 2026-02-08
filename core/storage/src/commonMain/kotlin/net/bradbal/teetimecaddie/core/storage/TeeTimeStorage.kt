package net.bradbal.teetimecaddie.core.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.QuerySnapshot
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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

    /**
     * Update an existing Tee Time in the database.
     *
     * @param document A [TeeTimeDocument] with updated information. The document's id must be set.
     * @throws IllegalArgumentException if the document id is null.
     */
    suspend fun updateTeeTime(document: TeeTimeDocument) {
        val docId = document.id ?: throw IllegalArgumentException("Document id must be set for update")
        teeTimesCollection.document(docId).set(document)
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getTeeTimes(playerId: String): List<TeeTimeDocument> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return teeTimesCollection
            .where("createdBy", playerId)
            .orderBy("date", Direction.ASCENDING)
            .get()
            .documents
            .deserialize<TeeTimeDocument>(predicate = { doc -> id = doc.id })
            .filter { it.date >= now }
    }

    @OptIn(ExperimentalTime::class)
    fun teeTimesFlow(playerId: String): Flow<List<TeeTimeDocument>> {
        return teeTimesCollection
            .where("createdBy", playerId)
            .orderBy("date", Direction.ASCENDING)
            .snapshots(includeMetadataChanges = false)
            .map { snapshot ->
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                snapshot.documents
                    .deserialize<TeeTimeDocument>(predicate = { doc -> id = doc.id })
                    .filter { it.date >= now }
            }
    }
}

inline fun <reified T : Any> List<DocumentSnapshot>.deserialize(predicate: T.(DocumentSnapshot)->Unit): List<T> {
    return this.map { document ->
        document.data<T>().apply {
            predicate(document)
        }
    }
}
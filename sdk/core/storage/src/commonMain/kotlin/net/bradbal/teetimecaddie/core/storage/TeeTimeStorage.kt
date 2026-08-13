package net.bradbal.teetimecaddie.core.storage

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.bradball.teetimecaddie.core.models.storage.FirestoreClient
import net.bradball.teetimecaddie.core.models.storage.StoredDoc
import net.bradbal.teetimecaddie.core.storage.documents.TeeTimeDocument

private const val TEETIME_COLLECTION = "teetimes"

/**
 * Manage storage of tee times in the database.
 *
 * Persistence is delegated to the platform-provided [FirestoreClient]; this class owns the mapping
 * between [TeeTimeDocument] and the JSON representation stored in the cloud, as well as the reactive
 * orchestration (wrapping the platform's snapshot listener in a Flow). It remains the single place
 * to change if the storage backend is ever swapped.
 */
class TeeTimeStorage internal constructor(private val firestore: FirestoreClient) {

    /**
     * Add a Tee Time to the database.
     *
     * @param document A [TeeTimeDocument] with information about the tee time to add.
     * @return The unique id assigned to the tee time document.
     */
    suspend fun addTeeTime(document: TeeTimeDocument): String =
        firestore.add(TEETIME_COLLECTION, storageJson.encodeToString(document))

    @OptIn(ExperimentalTime::class)
    suspend fun getTeeTimes(playerId: String): List<TeeTimeDocument> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return firestore
            .query(TEETIME_COLLECTION, whereField = "createdBy", whereValue = playerId, orderByField = "date", descending = false)
            .map { it.toTeeTimeDocument() }
            .filter { it.date >= now }
    }

    @OptIn(ExperimentalTime::class)
    fun teeTimesFlow(playerId: String): Flow<List<TeeTimeDocument>> = callbackFlow {
        val cancellable = firestore.observeQuery(
            collection = TEETIME_COLLECTION,
            whereField = "createdBy",
            whereValue = playerId,
            orderByField = "date",
            descending = false,
            onChange = { documents ->
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val teeTimes = documents
                    .map { it.toTeeTimeDocument() }
                    .filter { it.date >= now }
                trySend(teeTimes)
            },
            onError = { message -> close(TeeTimeStorageException(message)) }
        )
        awaitClose { cancellable.cancel() }
    }

    /**
     * Get a single tee time by ID.
     *
     * @param id The unique id of the tee time document.
     * @return The [TeeTimeDocument] if found, null otherwise.
     */
    suspend fun getTeeTime(id: String): TeeTimeDocument? =
        firestore.get(TEETIME_COLLECTION, id)?.toTeeTimeDocument()

    /**
     * Update an existing tee time in the database.
     *
     * @param id The unique id of the tee time document to update.
     * @param document A [TeeTimeDocument] with the updated information.
     */
    suspend fun updateTeeTime(id: String, document: TeeTimeDocument) {
        firestore.set(TEETIME_COLLECTION, id, storageJson.encodeToString(document))
    }
}

/** Thrown into [TeeTimeStorage.teeTimesFlow] when the underlying snapshot listener reports an error. */
class TeeTimeStorageException(message: String) : Exception(message)

private fun StoredDoc.toTeeTimeDocument(): TeeTimeDocument =
    storageJson.decodeFromString<TeeTimeDocument>(json).apply { id = this@toTeeTimeDocument.id }

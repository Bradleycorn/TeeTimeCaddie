package net.bradball.teetimecaddie.core.models.storage

/**
 * A cloud document store binding, implemented by the platform applications against their native
 * Firestore SDK (Android in Kotlin, iOS in Swift) and injected into the SDK during initialization.
 *
 * This is the "native Firestore binding" that replaces the GitLive Firebase Firestore library.
 * It sits **behind** the storage classes ([net.bradbal.teetimecaddie.core.storage.TeeTimeStorage],
 * [net.bradbal.teetimecaddie.core.storage.PlayerStorage]) — repositories never depend on it. That
 * keeps the storage classes the single seam to change if the backend is ever swapped.
 *
 * Documents cross the boundary as JSON strings (see [StoredDoc]) rather than maps. This keeps the
 * shared code's model<->document mapping in one place (kotlinx-serialization) and avoids fragile
 * platform number/type bridging: each platform converts between a JSON string and its native
 * Firestore document representation using its own JSON tooling.
 */
interface FirestoreClient {

    /**
     * Add a new document (with an auto-generated id) to [collection].
     * @param json the document body as a JSON object string.
     * @return the id assigned to the new document.
     */
    suspend fun add(collection: String, json: String): String

    /**
     * Create or overwrite the document with [id] in [collection].
     * @param json the document body as a JSON object string.
     */
    suspend fun set(collection: String, id: String, json: String)

    /**
     * Fetch a single document by [id] from [collection], or null if it does not exist.
     */
    suspend fun get(collection: String, id: String): StoredDoc?

    /**
     * Run a one-shot query over [collection] filtering where [whereField] equals [whereValue],
     * ordered by [orderByField].
     */
    suspend fun query(
        collection: String,
        whereField: String,
        whereValue: String,
        orderByField: String,
        descending: Boolean
    ): List<StoredDoc>

    /**
     * Observe a query (same shape as [query]) in real time.
     *
     * The shared code wraps this in a Flow, so the platform only needs to attach a snapshot
     * listener and push results into [onChange] (or a message into [onError]). The returned
     * [Cancellable] is used to detach the listener when the Flow is closed.
     */
    fun observeQuery(
        collection: String,
        whereField: String,
        whereValue: String,
        orderByField: String,
        descending: Boolean,
        onChange: (List<StoredDoc>) -> Unit,
        onError: (String) -> Unit
    ): Cancellable
}

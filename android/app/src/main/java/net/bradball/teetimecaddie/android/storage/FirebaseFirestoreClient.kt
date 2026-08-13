package net.bradball.teetimecaddie.android.storage

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import kotlinx.coroutines.suspendCancellableCoroutine
import net.bradball.teetimecaddie.core.models.storage.Cancellable
import net.bradball.teetimecaddie.core.models.storage.FirestoreClient
import net.bradball.teetimecaddie.core.models.storage.StoredDoc
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Android [FirestoreClient] implementation backed by the native Firebase Firestore SDK.
 *
 * This is the platform-provided binding that replaces the GitLive Firestore library. Documents
 * cross the shared-code boundary as JSON strings; this class converts between JSON and Firestore's
 * native map representation.
 */
class FirebaseFirestoreClient(useEmulator: Boolean) : FirestoreClient {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance().apply {
        if (useEmulator) {
            useEmulator(EMULATOR_HOST, EMULATOR_PORT)
            firestoreSettings = firestoreSettings {
                // Disable local persistence against the emulator, matching prior behaviour.
                setLocalCacheSettings(memoryCacheSettings {})
            }
        }
    }

    override suspend fun add(collection: String, json: String): String =
        db.collection(collection).add(json.toFirestoreMap()).awaitResult().id

    override suspend fun set(collection: String, id: String, json: String) {
        db.collection(collection).document(id).set(json.toFirestoreMap()).awaitResult()
    }

    override suspend fun get(collection: String, id: String): StoredDoc? {
        val snapshot = db.collection(collection).document(id).get().awaitResult()
        return if (snapshot.exists()) snapshot.toStoredDoc() else null
    }

    override suspend fun query(
        collection: String,
        whereField: String,
        whereValue: String,
        orderByField: String,
        descending: Boolean
    ): List<StoredDoc> {
        val snapshot = db.collection(collection)
            .whereEqualTo(whereField, whereValue)
            .orderBy(orderByField, direction(descending))
            .get()
            .awaitResult()
        return snapshot.documents.map { it.toStoredDoc() }
    }

    override fun observeQuery(
        collection: String,
        whereField: String,
        whereValue: String,
        orderByField: String,
        descending: Boolean,
        onChange: (List<StoredDoc>) -> Unit,
        onError: (String) -> Unit
    ): Cancellable {
        val registration: ListenerRegistration = db.collection(collection)
            .whereEqualTo(whereField, whereValue)
            .orderBy(orderByField, direction(descending))
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> onError(error.message ?: "Unknown Firestore error")
                    snapshot != null -> onChange(snapshot.documents.map { it.toStoredDoc() })
                }
            }
        return object : Cancellable {
            override fun cancel() = registration.remove()
        }
    }

    private fun direction(descending: Boolean): Query.Direction =
        if (descending) Query.Direction.DESCENDING else Query.Direction.ASCENDING

    companion object {
        private const val EMULATOR_HOST = "10.0.2.2"
        private const val EMULATOR_PORT = 9399
    }
}

private fun DocumentSnapshot.toStoredDoc(): StoredDoc =
    StoredDoc(id, JSONObject(data ?: emptyMap<String, Any>()).toString())

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> if (continuation.isActive) continuation.resume(result) }
    addOnFailureListener { error -> if (continuation.isActive) continuation.resumeWithException(error) }
}

private fun String.toFirestoreMap(): Map<String, Any?> = JSONObject(this).toValueMap()

private fun JSONObject.toValueMap(): Map<String, Any?> = buildMap {
    val jsonKeys = keys()
    while (jsonKeys.hasNext()) {
        val key = jsonKeys.next()
        put(key, get(key).unwrapJson())
    }
}

private fun JSONArray.toValueList(): List<Any?> = (0 until length()).map { get(it).unwrapJson() }

private fun Any?.unwrapJson(): Any? = when (this) {
    is JSONObject -> toValueMap()
    is JSONArray -> toValueList()
    JSONObject.NULL -> null
    else -> this
}

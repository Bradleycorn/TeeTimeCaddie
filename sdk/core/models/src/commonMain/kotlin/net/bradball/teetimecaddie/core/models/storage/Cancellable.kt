package net.bradball.teetimecaddie.core.models.storage

/**
 * A handle to an ongoing observation (e.g. a Firestore snapshot listener) that can be cancelled.
 *
 * Returned by [FirestoreClient.observeQuery] and cancelled by the shared code when the wrapping
 * Flow is closed.
 */
interface Cancellable {
    fun cancel()
}

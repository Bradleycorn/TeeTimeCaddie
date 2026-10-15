package net.bradbal.teetimecaddie.core.storage

import dev.gitlive.firebase.firestore.DocumentSnapshot

/**
 * Decode every snapshot to [T], then let [predicate] finish each one off — typically to copy the
 * snapshot's id onto the document's transient `id`, which the data itself does not carry.
 */
inline fun <reified T : Any> List<DocumentSnapshot>.deserialize(predicate: T.(DocumentSnapshot)->Unit): List<T> {
    return this.map { document ->
        document.data<T>().apply {
            predicate(document)
        }
    }
}

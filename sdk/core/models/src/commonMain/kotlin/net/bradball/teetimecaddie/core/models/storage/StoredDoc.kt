package net.bradball.teetimecaddie.core.models.storage

/**
 * A document read from the cloud store: its [id] plus its body as a JSON object string ([json]).
 *
 * The shared storage layer decodes [json] into a `@Serializable` document model (setting the
 * model's id from [id]). See [FirestoreClient] for why the body crosses the boundary as JSON.
 */
data class StoredDoc(val id: String, val json: String)

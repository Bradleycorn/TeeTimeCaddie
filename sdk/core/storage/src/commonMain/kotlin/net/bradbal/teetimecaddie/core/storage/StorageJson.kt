package net.bradbal.teetimecaddie.core.storage

import kotlinx.serialization.json.Json

/**
 * The JSON format used to (de)serialize storage document models to/from the JSON strings that
 * cross the [net.bradball.teetimecaddie.core.models.storage.FirestoreClient] boundary.
 *
 * `ignoreUnknownKeys` is enabled so documents that gain fields over time don't break older readers.
 */
internal val storageJson: Json = Json {
    ignoreUnknownKeys = true
}

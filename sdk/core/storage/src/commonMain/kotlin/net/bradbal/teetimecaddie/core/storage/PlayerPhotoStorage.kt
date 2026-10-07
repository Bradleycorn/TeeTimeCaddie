package net.bradbal.teetimecaddie.core.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.storage.Data
import dev.gitlive.firebase.storage.FirebaseStorageMetadata
import dev.gitlive.firebase.storage.storage

/** Avatars are always JPEG, always at this path, always one per player. */
private const val AVATAR_CONTENT_TYPE = "image/jpeg"
private fun avatarPath(playerId: String) = "players/$playerId/avatar.jpg"

/** The largest avatar the storage rules will accept. Callers should downscale well below this. */
const val MAX_AVATAR_BYTES = 5 * 1024 * 1024

/**
 * Manage storage of player avatars in cloud storage.
 *
 * Avatars are stored at a deterministic path, so re-uploading overwrites rather than accumulating
 * orphaned blobs — which is what makes a failed [uploadAvatar] safe to retry.
 */
class PlayerPhotoStorage {
    private val storage = Firebase.storage

    /**
     * Upload a player's avatar, replacing any existing one.
     *
     * @param playerId The Firebase Auth user id for the player.
     * @param bytes JPEG-encoded image data. Callers are responsible for downscaling and
     *   compressing before calling; anything at or above [MAX_AVATAR_BYTES] is rejected by the
     *   storage rules.
     * @return A long-lived download URL for the uploaded avatar. Each upload mints a new token, so
     *   the player's stored `photoUrl` must be rewritten every time this is called.
     */
    suspend fun uploadAvatar(playerId: String, bytes: ByteArray): String {
        require(bytes.isNotEmpty()) { "Avatar image data was empty." }
        require(bytes.size < MAX_AVATAR_BYTES) {
            "Avatar image is ${bytes.size} bytes, which exceeds the $MAX_AVATAR_BYTES byte limit."
        }

        val ref = storage.reference(avatarPath(playerId))
        ref.putData(bytes.toStorageData(), FirebaseStorageMetadata(contentType = AVATAR_CONTENT_TYPE))
        return ref.getDownloadUrl()
    }

    /**
     * Delete a player's avatar.
     *
     * @param playerId The Firebase Auth user id for the player.
     */
    suspend fun deleteAvatar(playerId: String) {
        storage.reference(avatarPath(playerId)).delete()
    }
}

/**
 * Bridge a [ByteArray] into the platform image container Firebase Storage expects.
 *
 * GitLive's `Data` is an `expect class` with platform-only constructors, so this cannot live in
 * common code.
 */
internal expect fun ByteArray.toStorageData(): Data

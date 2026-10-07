package net.bradbal.teetimecaddie.core.storage

import dev.gitlive.firebase.storage.Data
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create

/**
 * Pinning is required so the Kotlin heap does not move the array while `NSData` copies from it.
 * [PlayerPhotoStorage.uploadAvatar] rejects empty data before reaching here, because `addressOf(0)`
 * on an empty array throws.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal actual fun ByteArray.toStorageData(): Data = usePinned { pinned ->
    Data(NSData.create(bytes = pinned.addressOf(0), length = size.toULong()))
}

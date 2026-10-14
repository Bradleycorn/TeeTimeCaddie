package net.bradbal.teetimecaddie.core.storage

import dev.gitlive.firebase.storage.Data

internal actual fun ByteArray.toStorageData(): Data = Data(this)

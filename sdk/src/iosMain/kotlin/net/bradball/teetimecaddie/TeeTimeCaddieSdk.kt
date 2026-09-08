package net.bradball.teetimecaddie

import net.bradbal.teetimecaddie.core.storage.StorageModule

fun TeeTimeCaddieSdk.Companion.initialize(useLocalResources: Boolean) {
    initialize(useLocalResources, StorageModule())
}
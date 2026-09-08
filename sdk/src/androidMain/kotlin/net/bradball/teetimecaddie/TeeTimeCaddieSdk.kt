package net.bradball.teetimecaddie

import android.content.Context
import net.bradbal.teetimecaddie.core.storage.StorageModule

fun TeeTimeCaddieSdk.Companion.initialize(context: Context, useLocalResources: Boolean) {
    initialize(useLocalResources, StorageModule(context.applicationContext))
}
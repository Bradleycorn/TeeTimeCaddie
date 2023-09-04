package net.bradball.teetimecaddie.android.initializers

import android.app.Application
import kotlin.reflect.KClass

interface AppInitializer {
    val priority: InitializerPriority

    val dependencies: List<KClass<out AppInitializer>>

    suspend fun init(application: Application)
}

fun Set<AppInitializer>.filterBy(priority: InitializerPriority): Set<AppInitializer> = filter {
    it.priority == priority
}.toSet()

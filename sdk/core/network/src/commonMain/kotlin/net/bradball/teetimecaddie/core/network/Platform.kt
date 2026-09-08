package net.bradball.teetimecaddie.core.network

interface Platform {
    val name: String
}

fun getPlatform(): Platform = object: Platform { override val name = "common "}
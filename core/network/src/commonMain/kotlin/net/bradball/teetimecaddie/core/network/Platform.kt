package net.bradball.teetimecaddie.core.network

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
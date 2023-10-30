package net.bradball.teetimecaddie

internal expect object FirebaseConfig {
    val debugHost: String
}

internal val FirebaseConfig.authDebugPort: Int get() = 9099
internal val FirebaseConfig.firestoreDebugPort: Int get() = 8080

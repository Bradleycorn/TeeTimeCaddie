package net.bradball.teetimecaddie

internal expect object FirebaseConfig {
    val emulatorHost: String
}
internal val FirebaseConfig.deviceDebugHost: String get() = "192.168.86.42"
internal val FirebaseConfig.authDebugPort: Int get() = 9099
internal val FirebaseConfig.firestoreDebugPort: Int get() = 9399

internal val FirebaseConfig.debugHost: String
    get() = emulatorHost
//    get() = deviceDebugHost
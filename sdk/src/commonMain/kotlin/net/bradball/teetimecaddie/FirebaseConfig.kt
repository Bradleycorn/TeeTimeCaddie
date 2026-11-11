package net.bradball.teetimecaddie

// This determines which host (IP address) is used by the app to connect to the firebase emulator
// for debug builds. You will need to change this value based on what WIFI network the device
// and hosting computer running the firebase emulator are on.
internal val FirebaseConfig.debugHost: FirebaseDebugHost
    get() = FirebaseDebugHost.ORB

// When using an emulator on the same computer that firebase is running on, the
// IP to use is different between android and ios, so this is an expect/actual.
// See the android/ios source set for the actual value.
internal expect object FirebaseConfig {
    val emulatorHost: String
}

// The list of host IP addresses that could be used, mapped to
// various networks.
internal enum class FirebaseDebugHost(val ip: String) {
    EMULATOR(FirebaseConfig.emulatorHost),
    HOME_WIFI("192.168.86.42"),
    ORB("192.168.91.34"),
    CD_GUEST("172.16.38.12"),
}

internal val FirebaseConfig.authDebugPort: Int get() = 9099
internal val FirebaseConfig.firestoreDebugPort: Int get() = 9399
internal val FirebaseConfig.storageDebugPort: Int get() = 9199


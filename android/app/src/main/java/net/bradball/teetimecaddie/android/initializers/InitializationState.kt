package net.bradball.teetimecaddie.android.initializers


sealed class InitializationState {
    object Pending: InitializationState()
    object Complete: InitializationState()
    class Failed(val error: Exception): InitializationState()
}
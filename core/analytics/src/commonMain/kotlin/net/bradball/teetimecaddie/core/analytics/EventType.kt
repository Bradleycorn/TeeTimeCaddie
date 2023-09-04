package net.bradball.teetimecaddie.core.analytics

/**
 * Defines a set of types that can be used to categorize analytical events that
 * happen in the application.
 */
internal enum class EventType {
    CLICK,
    OPERATION,
    TRANSACTION,
    NAVIGATION,
    MESSAGES,
    APPLICATION
}
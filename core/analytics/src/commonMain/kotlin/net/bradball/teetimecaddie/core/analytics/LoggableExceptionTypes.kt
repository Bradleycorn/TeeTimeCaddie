package net.bradball.teetimecaddie.core.analytics

/**
 * Defines a set of "categories" for [LoggableException]s.
 * These should serve as high level categories for logging errors,
 * on the level of sections of the application.
 * We do not want these to be too detailed or granular.
 */
enum class LoggableExceptionTypes(val displayName: String = toString()) {
    STARTUP("Application Startup"),
    AUTHENTICATION("Authentication"),
    NO_PLUGIN("Analytics-Plugin"),
    INTEROP("Kotlin-Swift-Interop")
}
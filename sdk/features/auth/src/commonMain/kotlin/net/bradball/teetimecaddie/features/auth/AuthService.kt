package net.bradball.teetimecaddie.features.auth

import net.bradball.teetimecaddie.core.models.storage.Cancellable

/**
 * The native authentication binding, implemented by the platform applications against their native
 * Firebase Auth SDK (Android in Kotlin, iOS in Swift) and injected into the SDK during initialization.
 *
 * This is the "native auth binding" that replaces the GitLive Firebase Auth library. It sits behind
 * [AuthRepository] — the repository owns all auth business logic (validation, analytics, player
 * creation) and depends only on this interface, so the shared code carries no Firebase dependency.
 *
 * Fallible operations return an [AuthResult] rather than throwing, so error classification happens on
 * the platform (which can see native Firebase error types) while the shared repository decides what to
 * do with the resulting [AuthErrors].
 */
interface AuthService {

    /** The id of the currently authenticated user, or null if none. */
    val currentUserId: String?

    /** The display name of the currently authenticated user, or null if none/unset. */
    val currentUserDisplayName: String?

    /** Sign in with email/password. */
    suspend fun signIn(email: String, password: String): AuthResult

    /** Create a new account with email/password. On success the result carries the new user's id. */
    suspend fun register(email: String, password: String): AuthResult

    /** Set the current user's display name (best-effort; may throw on failure). */
    suspend fun updateDisplayName(name: String)

    /**
     * Force-refresh the current user's auth token.
     * @return true if the token was refreshed, false if it could not be (caller should sign out).
     */
    suspend fun refreshToken(): Boolean

    /** Sign the current user out. */
    fun signOut()

    /**
     * Observe authentication state. [onChange] is invoked with the current user id (or null when
     * signed out) whenever it changes. The returned [Cancellable] detaches the listener.
     */
    fun observeAuthState(onChange: (userId: String?) -> Unit): Cancellable
}

/** The outcome of a fallible [AuthService] operation. */
sealed interface AuthResult {
    /** The operation succeeded; [userId] is the authenticated user's id. */
    data class Success(val userId: String) : AuthResult

    /** The operation failed; [error] classifies the failure for display and analytics. */
    data class Failure(val error: AuthErrors) : AuthResult
}

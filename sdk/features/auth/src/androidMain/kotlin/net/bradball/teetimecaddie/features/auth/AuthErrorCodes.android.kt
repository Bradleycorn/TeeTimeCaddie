package net.bradball.teetimecaddie.features.auth

import dev.gitlive.firebase.auth.FirebaseAuthException

/**
 * On Android, GitLive's [FirebaseAuthException] is a typealias to the Firebase Android SDK's own,
 * which exposes `errorCode` directly. Going through the typealias rather than importing
 * `com.google.firebase.auth` keeps this off a transitive dependency.
 */
internal actual fun Throwable.firebaseAuthErrorCode(): String? =
    (this as? FirebaseAuthException)?.errorCode
        ?: (cause as? FirebaseAuthException)?.errorCode

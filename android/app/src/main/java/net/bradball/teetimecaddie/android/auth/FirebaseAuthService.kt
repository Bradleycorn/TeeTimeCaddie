package net.bradball.teetimecaddie.android.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import net.bradball.teetimecaddie.core.models.storage.Cancellable
import net.bradball.teetimecaddie.features.auth.AuthErrors
import net.bradball.teetimecaddie.features.auth.AuthResult
import net.bradball.teetimecaddie.features.auth.AuthService
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Android [AuthService] implementation backed by the native Firebase Auth SDK.
 *
 * This is the platform-provided binding that replaces the GitLive Firebase Auth library. It maps
 * native Firebase auth error types to the shared [AuthErrors] taxonomy.
 */
class FirebaseAuthService(useEmulator: Boolean) : AuthService {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance().apply {
        if (useEmulator) useEmulator(EMULATOR_HOST, EMULATOR_PORT)
    }

    override val currentUserId: String? get() = auth.currentUser?.uid
    override val currentUserDisplayName: String? get() = auth.currentUser?.displayName

    override suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).awaitResult()
            AuthResult.Success(result.user?.uid ?: auth.currentUser?.uid.orEmpty())
        } catch (ex: Exception) {
            AuthResult.Failure(AuthErrors.INVALID_CREDENTIALS)
        }
    }

    override suspend fun register(email: String, password: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).awaitResult()
            val uid = result.user?.uid ?: return AuthResult.Failure(AuthErrors.REG_DEFAULT)
            AuthResult.Success(uid)
        } catch (ex: FirebaseAuthUserCollisionException) {
            AuthResult.Failure(AuthErrors.USER_EXISTS)
        } catch (ex: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Failure(AuthErrors.INVALID_EMAIL)
        } catch (ex: FirebaseAuthException) {
            AuthResult.Failure(AuthErrors.REG_DEFAULT)
        }
        // Non-Firebase exceptions (e.g. network) propagate and are classified as UNKNOWN by the repository.
    }

    override suspend fun updateDisplayName(name: String) {
        val user = auth.currentUser ?: return
        val request = UserProfileChangeRequest.Builder().setDisplayName(name).build()
        user.updateProfile(request).awaitResult()
    }

    override suspend fun refreshToken(): Boolean {
        val user = auth.currentUser ?: return false
        return try {
            user.getIdToken(true).awaitResult()
            true
        } catch (ex: Exception) {
            false
        }
    }

    override fun signOut() {
        auth.signOut()
    }

    override fun observeAuthState(onChange: (String?) -> Unit): Cancellable {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            onChange(firebaseAuth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        return object : Cancellable {
            override fun cancel() = auth.removeAuthStateListener(listener)
        }
    }

    companion object {
        private const val EMULATOR_HOST = "10.0.2.2"
        private const val EMULATOR_PORT = 9099
    }
}

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> if (continuation.isActive) continuation.resume(result) }
    addOnFailureListener { error -> if (continuation.isActive) continuation.resumeWithException(error) }
}

package net.bradball.teetimecaddie.features.auth

import dev.gitlive.firebase.auth.FirebaseUser

sealed class RegisterResult {
    class Success(val user: FirebaseUser): RegisterResult()
    class Failure(val message: String, val error: Throwable): RegisterResult()
}
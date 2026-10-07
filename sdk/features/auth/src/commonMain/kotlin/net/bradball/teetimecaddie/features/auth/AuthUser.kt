package net.bradball.teetimecaddie.features.auth

/**
 * The identity Firebase Auth knows about: an id and the address used to sign in.
 *
 * Deliberately thin. Everything else about a person — their name, phone number and avatar — is
 * profile data owned by the players feature, not by authentication.
 */
data class AuthUser(
    val id: String,
    val email: String
)

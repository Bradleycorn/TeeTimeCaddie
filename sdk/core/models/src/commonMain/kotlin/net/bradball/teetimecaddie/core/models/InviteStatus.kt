package net.bradball.teetimecaddie.core.models

/**
 * Where an invitee stands on a game.
 *
 * Every invitee starts [PENDING], and answers once: [ACCEPTED] takes a slot in the tee time,
 * [DECLINED] doesn't.
 */
enum class InviteStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}

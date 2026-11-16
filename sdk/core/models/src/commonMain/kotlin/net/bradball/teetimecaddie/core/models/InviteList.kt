package net.bradball.teetimecaddie.core.models

import net.bradball.teetimecaddie.core.extensions.toPhoneDigits

/**
 * Who a new game will invite: people already [invited], and people [chosen] to invite next.
 *
 * No phone number appears twice, however it's written, and the organizer's never appears.
 *
 * @property organizerPhone The number of the player creating the game.
 */
data class InviteList(
    val organizerPhone: String,
    val invited: List<NewInvitee> = emptyList(),
    val chosen: List<NewInvitee> = emptyList(),
) {
    /** True when [phone] is the organizer's or already in the list, however it's written. */
    fun contains(phone: String): Boolean = phone.toPhoneDigits() in takenPhones()

    /** [people] added to the end of [chosen], skipping any number already in the list. */
    fun choose(people: List<NewInvitee>): InviteList {
        val taken = takenPhones()
        // add() is false for a number already taken, so each number joins once.
        return copy(chosen = chosen + people.filter { taken.add(it.phone.toPhoneDigits()) })
    }

    /** [person] taken out of [chosen]. */
    fun unchoose(person: NewInvitee): InviteList {
        val digits = person.phone.toPhoneDigits()
        return copy(chosen = chosen.filterNot { it.phone.toPhoneDigits() == digits })
    }

    /** [person] taken out of [invited]. */
    fun uninvite(person: NewInvitee): InviteList {
        val digits = person.phone.toPhoneDigits()
        return copy(invited = invited.filterNot { it.phone.toPhoneDigits() == digits })
    }

    /** Everyone [chosen] moved to the end of [invited]. */
    fun inviteChosen(): InviteList = copy(invited = invited + chosen, chosen = emptyList())

    private fun takenPhones(): MutableSet<String> =
        (invited + chosen).mapTo(mutableSetOf(organizerPhone.toPhoneDigits())) { it.phone.toPhoneDigits() }
}

/** Three people invited and one chosen, for an organizer at (502) 555-1234. */
val previewInviteList = InviteList(
    organizerPhone = "5025551234",
    invited = previewNewInviteeList,
    chosen = listOf(NewInvitee(name = "Sam Tanaka", phone = "4155550123"))
)

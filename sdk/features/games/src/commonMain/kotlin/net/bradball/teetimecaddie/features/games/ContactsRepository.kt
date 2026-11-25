package net.bradball.teetimecaddie.features.games

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.LoggableExceptionTypes
import net.bradball.teetimecaddie.core.extensions.toPhoneDigits
import net.bradball.teetimecaddie.core.models.InviteList
import net.bradball.teetimecaddie.core.models.NewInvitee
import net.bradball.teetimecaddie.core.models.TtcResult

/**
 * Adds people to an [InviteList]'s chosen people, either from the device's contacts or from a name
 * and phone number entered by hand, and invites the people chosen.
 *
 * **Nothing here throws.** Failures come back as [TtcResult.Failure] carrying a [GameException].
 */
interface ContactsRepository {

    /**
     * Adds the contacts that [picked] reads to [list]'s chosen people.
     *
     * Each contact is added at one number: its first mobile number, or its first number when none
     * is a mobile. Contacts without a name, or without a number a game can be published with, are
     * skipped. The new people are sorted by name and added after those already chosen, skipping any
     * number already in the list, as [InviteList.choose] does.
     *
     * @param list The invite list to add to.
     * @param picked Reads the contacts to add — usually just the ones the person selected, not
     *   every contact on the device.
     * @return The updated list, or [GameErrors.CONTACTS_LOAD_FAILED] when the contacts can't be
     *   read, leaving [list] as it was.
     */
    suspend fun choosePickedContacts(list: InviteList, picked: DeviceContactsReader): TtcResult<InviteList>

    /**
     * Adds a person entered by name and phone number to [list]'s chosen people, or reports why
     * they can't be added.
     *
     * The name must not be blank, and the number must have exactly 10 digits and not already be in
     * the list. The name is trimmed, with runs of spaces made one, and the number is kept as its 10
     * digits.
     *
     * @param list The invite list to add to.
     * @param name The person's name, as entered.
     * @param phone The person's phone number, in any format.
     * @return [ManualInviteeResult.Chosen] with the person and the updated list, or
     *   [ManualInviteeResult.Rejected] with what's wrong with the name or number.
     */
    fun chooseEnteredPerson(list: InviteList, name: String, phone: String): ManualInviteeResult

    /**
     * Invites everyone chosen in [list]: they move to the end of its invited people, in the order
     * they were chosen, and nobody is left chosen.
     *
     * @param list The invite list whose chosen people to invite.
     * @return The updated list.
     */
    fun inviteChosen(list: InviteList): InviteList
}

/**
 * The [ContactsRepository] the apps use. It also logs:
 * - [AnalyticsEvent.AddInvitees] each time picked contacts are chosen, with how many were picked
 *   and how many were chosen. A failed read logs nothing.
 * - [AnalyticsEvent.AddManualInvitee] each time someone entered is chosen.
 * - [AnalyticsEvent.InvitePlayers] each time the chosen people are invited, with how many there
 *   were.
 */
class ContactsRepositoryImpl(
    private val eventManager: EventManager
) : ContactsRepository {

    override suspend fun choosePickedContacts(
        list: InviteList,
        picked: DeviceContactsReader
    ): TtcResult<InviteList> {
        val contacts = when (val result = readContacts(picked)) {
            is TtcResult.Success -> result.data
            is TtcResult.Failure -> return result
        }
        val updated = list.choose(contacts.toNewInvitees())
        eventManager.logEvent(
            AnalyticsEvent.AddInvitees(pickedCount = contacts.size, addedCount = updated.chosen.size - list.chosen.size)
        )
        return TtcResult.Success(updated)
    }

    override fun chooseEnteredPerson(list: InviteList, name: String, phone: String): ManualInviteeResult {
        val check = list.check(name, phone)
        if (!check.isValid) return ManualInviteeResult.Rejected(check)
        val person = NewInvitee(name = cleanInviteeName(name), phone = phone.toPhoneDigits())
        eventManager.logEvent(AnalyticsEvent.AddManualInvitee)
        return ManualInviteeResult.Chosen(person = person, list = list.choose(listOf(person)))
    }

    override fun inviteChosen(list: InviteList): InviteList {
        eventManager.logEvent(AnalyticsEvent.InvitePlayers(playerCount = list.chosen.size))
        return list.inviteChosen()
    }

    private suspend fun readContacts(reader: DeviceContactsReader): TtcResult<List<DeviceContact>> {
        val contacts = try {
            reader.readContacts()
        } catch (ex: Exception) {
            // Re-throws if this coroutine was cancelled, so cancellation is not swallowed into a
            // Failure. See the note in AuthRepository for why this beats catching the type.
            currentCoroutineContext().ensureActive()
            eventManager.logException(
                ex, LoggableExceptionTypes.GAMES, hashMapOf("action" to "read_contacts")
            )
            return TtcResult.Failure(GameException(GameErrors.CONTACTS_LOAD_FAILED, cause = ex))
        }
        return TtcResult.Success(contacts)
    }
}

/**
 * The contacts as people to invite, sorted by name ignoring case: see
 * [ContactsRepository.choosePickedContacts]. Names and numbers lose surrounding spaces; numbers
 * otherwise keep the form they were written in, for display.
 */
private fun List<DeviceContact>.toNewInvitees(): List<NewInvitee> =
    mapNotNull { contact ->
        val name = contact.name.trim()
        if (name.isEmpty()) return@mapNotNull null
        val usable = contact.phones.filter { it.number.toPhoneDigits().length in STORED_PHONE_LENGTHS }
        val phone = usable.firstOrNull { it.isMobile } ?: usable.firstOrNull() ?: return@mapNotNull null
        NewInvitee(name = name, phone = phone.number.trim())
    }.sortedBy { it.name.lowercase() }

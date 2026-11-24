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
 * Turns contacts the organizer picked, or a name and number they entered, into people chosen on an
 * [InviteList].
 *
 * **Nothing here throws.** Failures come back as [TtcResult.Failure] carrying a [GameException].
 */
interface ContactsRepository {

    /**
     * [list] with the [picked] contacts added to its chosen people, sorted by name, as in
     * [InviteList.choose]. Each contact joins at one number, its first mobile number or else its
     * first number. Contacts without a name or a number a game can be published with are left out.
     *
     * @return The new list, or [GameErrors.CONTACTS_LOAD_FAILED] when the contacts can't be read.
     */
    suspend fun choosePickedContacts(list: InviteList, picked: DeviceContactsReader): TtcResult<InviteList>

    /**
     * [list] with the person entered added to its chosen people, or what's wrong with the entry.
     *
     * The name must not be blank, and the number must have exactly 10 digits and not already be in
     * the list. The person's name is trimmed, with runs of spaces made one, and their number is
     * kept as its 10 digits.
     */
    fun chooseEnteredPerson(list: InviteList, name: String, phone: String): ManualInviteeResult
}

/**
 * The [ContactsRepository] the apps use. It also logs:
 * - [AnalyticsEvent.AddInvitees] each time picked contacts are chosen, with how many were picked
 *   and how many were chosen. A failed read logs nothing.
 * - [AnalyticsEvent.AddManualInvitee] each time someone entered is chosen.
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

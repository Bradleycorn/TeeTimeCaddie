package net.bradball.teetimecaddie.features.games

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.LoggableExceptionTypes
import net.bradball.teetimecaddie.core.extensions.toPhoneDigits
import net.bradball.teetimecaddie.core.models.InvitableContact
import net.bradball.teetimecaddie.core.models.NewInvitee
import net.bradball.teetimecaddie.core.models.TtcResult

/**
 * The device's contacts, as people the organizer can invite to a game.
 *
 * The contacts come from a [DeviceContactsReader] that each app supplies. Everything else — what
 * can be invited, how it is listed, and how the picker's choices become invitees — is decided here,
 * once for both apps.
 *
 * **Nothing here throws.** Failures come back as [TtcResult.Failure] carrying a [GameException].
 */
interface ContactsRepository {

    /**
     * Read the device's contacts as people to invite, sorted by name, ignoring case.
     *
     * A contact with several numbers is listed once per number, in the device's order. A number
     * listed twice on the same contact appears once. Left out: contacts without a name, and
     * numbers a game can't be published with (fewer than 10 or more than 15 digits), so a contact
     * with no other number is left out entirely.
     *
     * Read again each time the picker opens, so it reflects the contacts as they are now.
     *
     * @return The contacts, or [GameErrors.CONTACTS_LOAD_FAILED] when the reader fails.
     */
    suspend fun invitableContacts(): TtcResult<List<InvitableContact>>

    /**
     * The [contacts] whose name contains [query], ignoring case. A blank query matches everyone.
     */
    fun search(contacts: List<InvitableContact>, query: String): List<InvitableContact>

    /**
     * The ids of the [contacts] already in [invitees], which the picker opens with checked.
     *
     * A contact matches an invitee with the same name and number, however the number is written.
     */
    fun invitedContactIds(contacts: List<InvitableContact>, invitees: List<NewInvitee>): Set<String>

    /**
     * The invitee list after the picker closes with Done.
     *
     * Invitees whose contact was unchecked are removed. The rest keep their place, including any
     * that aren't among [contacts]. Contacts newly checked follow, in the order they were checked.
     *
     * @param invitees The invitees before the picker opened.
     * @param contacts The contacts the picker listed.
     * @param checkedIds Every checked contact's id, in the order they were checked.
     */
    fun pickInvitees(
        invitees: List<NewInvitee>,
        contacts: List<InvitableContact>,
        checkedIds: List<String>
    ): List<NewInvitee>
}

class ContactsRepositoryImpl(
    private val eventManager: EventManager,
    private val reader: DeviceContactsReader
) : ContactsRepository {

    override suspend fun invitableContacts(): TtcResult<List<InvitableContact>> {
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
        return TtcResult.Success(contacts.toInvitableContacts())
    }

    override fun search(contacts: List<InvitableContact>, query: String): List<InvitableContact> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return contacts
        return contacts.filter { it.name.contains(trimmed, ignoreCase = true) }
    }

    override fun invitedContactIds(
        contacts: List<InvitableContact>,
        invitees: List<NewInvitee>
    ): Set<String> =
        contacts.filter { contact -> invitees.any { contact.matches(it) } }
            .map { it.id }
            .toSet()

    override fun pickInvitees(
        invitees: List<NewInvitee>,
        contacts: List<InvitableContact>,
        checkedIds: List<String>
    ): List<NewInvitee> {
        val checked = checkedIds.toSet()
        val kept = invitees.filter { invitee ->
            val matches = contacts.filter { it.matches(invitee) }
            matches.isEmpty() || matches.any { it.id in checked }
        }
        val contactsById = contacts.associateBy { it.id }
        val added = checkedIds.distinct()
            .mapNotNull { contactsById[it] }
            .filter { contact -> kept.none { contact.matches(it) } }
            .map { it.toNewInvitee() }
        return kept + added
    }
}

/** True when [invitee] is this contact at this number. */
private fun InvitableContact.matches(invitee: NewInvitee): Boolean =
    invitee.name.trim() == name && invitee.phone.toPhoneDigits() == phone

/** The contacts as people to invite: see [ContactsRepository.invitableContacts]. */
internal fun List<DeviceContact>.toInvitableContacts(): List<InvitableContact> =
    flatMap { contact ->
        val name = contact.name.trim()
        if (name.isEmpty()) return@flatMap emptyList()
        contact.phones
            .map { it.number.toPhoneDigits() to it.label?.trim()?.ifEmpty { null } }
            .filter { (digits, _) -> digits.length in STORED_PHONE_LENGTHS }
            .distinctBy { (digits, _) -> digits }
            .map { (digits, label) ->
                InvitableContact(id = "${contact.id}|$digits", name = name, phone = digits, phoneLabel = label)
            }
    }.sortedBy { it.name.lowercase() }

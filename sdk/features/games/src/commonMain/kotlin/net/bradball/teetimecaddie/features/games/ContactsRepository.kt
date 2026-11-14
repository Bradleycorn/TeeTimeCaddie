package net.bradball.teetimecaddie.features.games

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.LoggableExceptionTypes
import net.bradball.teetimecaddie.core.extensions.PHONE_NUMBER_LENGTH
import net.bradball.teetimecaddie.core.extensions.toPhoneDigits
import net.bradball.teetimecaddie.core.models.InvitableContact
import net.bradball.teetimecaddie.core.models.NewInvitee
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.core.models.TypedInviteeCheck
import net.bradball.teetimecaddie.core.models.TypedPhoneError

/**
 * The people the organizer can invite to a game: the device's contacts, and anyone typed in by
 * name and phone number.
 *
 * The contacts come from a [DeviceContactsReader] that each app supplies. Everything else — what
 * can be invited, how it is listed, how the picker's choices and typed people become invitees, and
 * the Add players sheet's selection — is decided here, once for both apps.
 *
 * The selection is the people chosen in the Add players sheet who aren't on the game yet. Callers
 * keep it and pass it in; [commitSelection] moves it onto the game's invitees.
 *
 * Someone is already invited when an invitee has their phone number, however either is written.
 * Names play no part: the same number under another name is still the same person. The
 * organizer's own number counts as invited too, since they are already playing.
 *
 * **Nothing here throws.** Failures come back as [TtcResult.Failure] carrying a [GameException].
 */
interface ContactsRepository {

    /**
     * Read the device's contacts as people to invite, sorted by name, ignoring case.
     *
     * Each contact is listed once, at one number: its first mobile number, or its first number
     * when none is a mobile. The number keeps the form it was written in, for display. Left out:
     * contacts without a name, and numbers a game can't be published with (fewer than 10 or more
     * than 15 digits), so a contact with no other number is left out entirely.
     *
     * Read again each time the picker opens, so it reflects the contacts as they are now.
     *
     * @return The contacts, or [GameErrors.CONTACTS_LOAD_FAILED] when the reader fails.
     */
    suspend fun invitableContacts(): TtcResult<List<InvitableContact>>

    /**
     * Add every contact the system picker returned to [invitees], once the organizer taps Done.
     *
     * The contacts are read as in [invitableContacts], then added as in [pickInvitees]: at the end,
     * sorted by name among themselves, silently leaving out anyone already invited, the organizer,
     * and a second contact at a number that's already being added.
     *
     * Logs [AnalyticsEvent.AddInvitees] with how many contacts were picked, which is known only
     * once they're read (Android's picker returns just a URI), and how many of them were added. A
     * failed read logs nothing.
     *
     * @param organizer The signed-in player creating the game.
     * @param invitees The invitees before the picker opened.
     * @return The new invitee list, or [GameErrors.CONTACTS_LOAD_FAILED] when the reader fails.
     */
    // TODO(TTC-155): Remove once neither app calls it; both select picked contacts instead.
    suspend fun addPickedContacts(organizer: Player, invitees: List<NewInvitee>): TtcResult<List<NewInvitee>>

    /**
     * Add every contact the system picker returned to the Add players sheet's [selection].
     *
     * The contacts are read as in [invitableContacts] and added at the end of the selection,
     * sorted by name among themselves. Anyone at a number already in [invitees] or [selection], or
     * at the organizer's number, is silently left out, as is a second contact at a number that's
     * already being added.
     *
     * Logs [AnalyticsEvent.AddInvitees] with how many contacts were picked and how many of them
     * were added. A failed read logs nothing.
     *
     * @param organizer The signed-in player creating the game.
     * @param invitees The game's invitees.
     * @param selection The selection before the picker opened.
     * @return The new selection, or [GameErrors.CONTACTS_LOAD_FAILED] when the reader fails.
     */
    suspend fun selectPickedContacts(
        organizer: Player,
        invitees: List<NewInvitee>,
        selection: List<NewInvitee>
    ): TtcResult<List<NewInvitee>>

    /**
     * Check a name and phone number typed in to invite someone, without adding them.
     *
     * The name must not be blank. The number must have exactly 10 digits, however it's written,
     * and must not be in [invitees] or [selection], or be the organizer's own.
     *
     * @param organizer The signed-in player creating the game.
     * @param invitees The game's invitees.
     * @param selection The Add players sheet's selection.
     * @param name The name as typed.
     * @param phone The number as typed.
     * @return What's wrong with each field, if anything.
     */
    fun checkTypedInvitee(
        organizer: Player,
        invitees: List<NewInvitee>,
        selection: List<NewInvitee>,
        name: String,
        phone: String
    ): TypedInviteeCheck

    /**
     * Add someone to the end of the Add players sheet's [selection] by a typed name and number.
     *
     * They're added only if [checkTypedInvitee] finds nothing wrong. The name is trimmed, with runs
     * of spaces inside it made one, and the number is kept as its 10 digits. Logs
     * [AnalyticsEvent.AddTypedInvitee] when someone is added.
     *
     * @param organizer The signed-in player creating the game.
     * @param invitees The game's invitees.
     * @param selection The selection so far.
     * @param name The name as typed.
     * @param phone The number as typed.
     * @return [TypedInviteeResult.Selected] with the person and the new selection, or
     *   [TypedInviteeResult.Rejected] with what's wrong.
     */
    fun selectTypedInvitee(
        organizer: Player,
        invitees: List<NewInvitee>,
        selection: List<NewInvitee>,
        name: String,
        phone: String
    ): TypedInviteeResult

    /**
     * The [selection] without [invitee], and anyone else at their number.
     */
    fun deselect(selection: List<NewInvitee>, invitee: NewInvitee): List<NewInvitee>

    /**
     * The game's invitees once the Add players sheet's [selection] is added to them.
     *
     * The selection follows [invitees] in the order it was chosen. Anyone at a number already
     * invited is left out, so the list never holds a number twice.
     *
     * @param invitees The game's invitees.
     * @param selection The sheet's selection.
     * @return The new invitee list.
     */
    fun commitSelection(invitees: List<NewInvitee>, selection: List<NewInvitee>): List<NewInvitee>

    /**
     * The [contacts] whose name contains [query], ignoring case. A blank query matches everyone.
     */
    fun search(contacts: List<InvitableContact>, query: String): List<InvitableContact>

    /**
     * The ids of the [contacts] already in [invitees], which the picker opens with checked.
     *
     * A contact matches an invitee with the same number, whatever either's name.
     */
    fun invitedContactIds(contacts: List<InvitableContact>, invitees: List<NewInvitee>): Set<String>

    /**
     * The invitee list after the picker closes with Done.
     *
     * Invitees whose contact was unchecked are removed. The rest keep their place, including any
     * that aren't among [contacts]. Contacts newly checked follow, in the order they were checked,
     * except one at the [organizer]'s number or at a number already on the list, which is skipped.
     *
     * @param organizer The signed-in player creating the game.
     * @param invitees The invitees before the picker opened.
     * @param contacts The contacts the picker listed.
     * @param checkedIds Every checked contact's id, in the order they were checked.
     */
    fun pickInvitees(
        organizer: Player,
        invitees: List<NewInvitee>,
        contacts: List<InvitableContact>,
        checkedIds: List<String>
    ): List<NewInvitee>
}

class ContactsRepositoryImpl(
    private val eventManager: EventManager,
    private val reader: DeviceContactsReader
) : ContactsRepository {

    override suspend fun invitableContacts(): TtcResult<List<InvitableContact>> =
        when (val result = readContacts()) {
            is TtcResult.Success -> TtcResult.Success(result.data.toInvitableContacts())
            is TtcResult.Failure -> result
        }

    override suspend fun addPickedContacts(
        organizer: Player,
        invitees: List<NewInvitee>
    ): TtcResult<List<NewInvitee>> {
        val picked = when (val result = readContacts()) {
            is TtcResult.Success -> result.data
            is TtcResult.Failure -> return result
        }
        val contacts = picked.toInvitableContacts()
        val updated = pickInvitees(organizer, invitees, contacts, contacts.map { it.id })
        eventManager.logEvent(
            AnalyticsEvent.AddInvitees(pickedCount = picked.size, addedCount = updated.size - invitees.size)
        )
        return TtcResult.Success(updated)
    }

    override suspend fun selectPickedContacts(
        organizer: Player,
        invitees: List<NewInvitee>,
        selection: List<NewInvitee>
    ): TtcResult<List<NewInvitee>> {
        val picked = when (val result = readContacts()) {
            is TtcResult.Success -> result.data
            is TtcResult.Failure -> return result
        }
        val taken = takenPhones(organizer, invitees + selection)
        val added = picked.toInvitableContacts()
            // add() is false for a number already taken, so each number joins once.
            .filter { contact -> taken.add(contact.phone.toPhoneDigits()) }
            .map { it.toNewInvitee() }
        eventManager.logEvent(AnalyticsEvent.AddInvitees(pickedCount = picked.size, addedCount = added.size))
        return TtcResult.Success(selection + added)
    }

    override fun checkTypedInvitee(
        organizer: Player,
        invitees: List<NewInvitee>,
        selection: List<NewInvitee>,
        name: String,
        phone: String
    ): TypedInviteeCheck {
        val digits = phone.toPhoneDigits()
        val phoneError = when {
            digits.isEmpty() -> TypedPhoneError.MISSING
            digits.length != PHONE_NUMBER_LENGTH -> TypedPhoneError.INCOMPLETE
            digits in takenPhones(organizer, invitees + selection) -> TypedPhoneError.DUPLICATE
            else -> null
        }
        return TypedInviteeCheck(isNameBlank = name.isBlank(), phoneError = phoneError)
    }

    override fun selectTypedInvitee(
        organizer: Player,
        invitees: List<NewInvitee>,
        selection: List<NewInvitee>,
        name: String,
        phone: String
    ): TypedInviteeResult {
        val check = checkTypedInvitee(organizer, invitees, selection, name, phone)
        if (!check.isValid) return TypedInviteeResult.Rejected(check)
        val invitee = NewInvitee(name = cleanInviteeName(name), phone = phone.toPhoneDigits())
        eventManager.logEvent(AnalyticsEvent.AddTypedInvitee)
        return TypedInviteeResult.Selected(invitee = invitee, selection = selection + invitee)
    }

    override fun deselect(selection: List<NewInvitee>, invitee: NewInvitee): List<NewInvitee> {
        val digits = invitee.phone.toPhoneDigits()
        return selection.filterNot { it.phone.toPhoneDigits() == digits }
    }

    override fun commitSelection(
        invitees: List<NewInvitee>,
        selection: List<NewInvitee>
    ): List<NewInvitee> {
        val taken = invitees.map { it.phone.toPhoneDigits() }.toMutableSet()
        return invitees + selection.filter { taken.add(it.phone.toPhoneDigits()) }
    }

    /** The digits of every number in [invitees], plus the [organizer]'s. */
    private fun takenPhones(organizer: Player, invitees: List<NewInvitee>): MutableSet<String> =
        (invitees.map { it.phone } + organizer.phone).map { it.toPhoneDigits() }.toMutableSet()

    private suspend fun readContacts(): TtcResult<List<DeviceContact>> {
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
        organizer: Player,
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
        val taken = takenPhones(organizer, kept)
        val added = checkedIds.distinct()
            .mapNotNull { contactsById[it] }
            // add() is false for a number already taken, so each number joins once.
            .filter { contact -> taken.add(contact.phone.toPhoneDigits()) }
            .map { it.toNewInvitee() }
        return kept + added
    }
}

/** True when [invitee] has this contact's number, however either is written. */
private fun InvitableContact.matches(invitee: NewInvitee): Boolean =
    invitee.phone.toPhoneDigits() == phone.toPhoneDigits()

/** The contacts as people to invite: see [ContactsRepository.invitableContacts]. */
internal fun List<DeviceContact>.toInvitableContacts(): List<InvitableContact> =
    mapNotNull { contact ->
        val name = contact.name.trim()
        if (name.isEmpty()) return@mapNotNull null
        val usable = contact.phones.filter { it.number.toPhoneDigits().length in STORED_PHONE_LENGTHS }
        val phone = usable.firstOrNull { it.isMobile } ?: usable.firstOrNull() ?: return@mapNotNull null
        InvitableContact(
            id = "${contact.id}|${phone.number.toPhoneDigits()}",
            name = name,
            phone = phone.number.trim(),
            phoneLabel = phone.label?.trim()?.ifEmpty { null }
        )
    }.sortedBy { it.name.lowercase() }

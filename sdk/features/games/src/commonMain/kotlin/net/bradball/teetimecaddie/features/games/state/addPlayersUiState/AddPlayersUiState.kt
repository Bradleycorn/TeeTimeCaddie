package net.bradball.teetimecaddie.features.games.state.addPlayersUiState

import dev.icerock.moko.resources.StringResource
import net.bradball.teetimecaddie.core.extensions.toPhoneDigits
import net.bradball.teetimecaddie.core.models.InviteList
import net.bradball.teetimecaddie.core.models.InviteePhoneError
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.core.models.NewInvitee
import net.bradball.teetimecaddie.core.models.previewInviteList
import net.bradball.teetimecaddie.features.games.ContactsRepository
import net.bradball.teetimecaddie.features.games.GMR
import net.bradball.teetimecaddie.features.games.ManualInviteeResult
import net.bradball.teetimecaddie.features.games.check

/**
 * The Add players sheet, while it's open: people chosen from contacts or entered by name and
 * number, waiting to be added to the game.
 *
 * Start one with [open]. Each change returns a new state: [withName], [withPhone], [afterEntry],
 * [afterContactsPicked] and [unchoose]. When *Add N players* is tapped, the game's invitees become
 * `list.inviteChosen().invited`.
 *
 * @property list The game's invitees and the people chosen so far. Pass it to [ContactsRepository].
 * @property name The Name field, as typed.
 * @property phone The Phone number field, as typed.
 */
data class AddPlayersUiState(
    val list: InviteList,
    val name: String = "",
    val phone: String = "",
    private val hasTriedToAdd: Boolean = false,
    private val lastChosenName: String? = null,
    private val fromContacts: Set<String> = emptySet(),
) {
    /** The people selected, in the order chosen, one chip each. */
    val chosen: List<NewInvitee>
        get() = list.chosen

    /** Under *Choose from contacts* once any are picked: "3 selected · Matt, Jeff, Alex". */
    val contactsSummary: LocalizedText?
        get() {
            val picked = list.chosen.filter { it.phone.toPhoneDigits() in fromContacts }
            if (picked.isEmpty()) return null
            val names = picked.take(SUMMARY_NAMES).joinToString(", ") { it.firstName }
            val more = picked.size - SUMMARY_NAMES
            return if (more > 0) {
                LocalizedText(GMR.strings.add_players_contacts_summary_more, listOf(picked.size.toString(), names, more.toString()))
            } else {
                LocalizedText(GMR.strings.add_players_contacts_summary, listOf(picked.size.toString(), names))
            }
        }

    /** "Enter a name", once Add has been tapped with the name blank. */
    val nameError: StringResource?
        get() = if (hasTriedToAdd && name.isBlank()) GMR.strings.add_players_error_no_name else null

    /**
     * What's wrong with the number. A duplicate shows as soon as the number is complete; a missing
     * or incomplete number only once Add has been tapped.
     */
    val phoneError: StringResource?
        get() = when (list.check(name, phone).phoneError) {
            InviteePhoneError.DUPLICATE -> GMR.strings.add_players_error_duplicate_phone
            InviteePhoneError.MISSING -> GMR.strings.add_players_error_no_phone.takeIf { hasTriedToAdd }
            InviteePhoneError.INCOMPLETE -> GMR.strings.add_players_error_invalid_phone.takeIf { hasTriedToAdd }
            null -> null
        }

    /** "Sam Tanaka selected", below the form after someone is added by name and number. */
    val chosenMessage: LocalizedText?
        get() = lastChosenName?.let { LocalizedText(GMR.strings.add_players_person_selected, listOf(it)) }

    /** "Select players", "Add 1 player" or "Add 3 players". */
    val addPlayersLabel: LocalizedText
        get() = when (list.chosen.size) {
            0 -> LocalizedText(GMR.strings.add_players_select_button)
            1 -> LocalizedText(GMR.strings.add_players_add_one_button)
            else -> LocalizedText(GMR.strings.add_players_add_many_button, listOf(list.chosen.size.toString()))
        }

    /** False while nobody is selected; *Add N players* is then disabled. */
    val canAddPlayers: Boolean
        get() = list.chosen.isNotEmpty()

    /** The Name field changed. Clears the "selected" message. */
    fun withName(name: String): AddPlayersUiState = copy(name = name, lastChosenName = null)

    /** The Phone number field changed. Clears the "selected" message. */
    fun withPhone(phone: String): AddPlayersUiState = copy(phone = phone, lastChosenName = null)

    /**
     * Add was tapped and [result] came back from [ContactsRepository.chooseEnteredPerson]. A chosen
     * person clears both fields and shows the "selected" message; a rejected entry shows its
     * field errors.
     */
    fun afterEntry(result: ManualInviteeResult): AddPlayersUiState = when (result) {
        is ManualInviteeResult.Chosen -> copy(
            list = result.list,
            name = "",
            phone = "",
            hasTriedToAdd = false,
            lastChosenName = result.person.name
        )
        is ManualInviteeResult.Rejected -> copy(hasTriedToAdd = true, lastChosenName = null)
    }

    /** Contacts were picked: [updated] came back from [ContactsRepository.choosePickedContacts]. */
    fun afterContactsPicked(updated: InviteList): AddPlayersUiState {
        val before = list.chosen.map { it.phone.toPhoneDigits() }.toSet()
        val picked = updated.chosen.map { it.phone.toPhoneDigits() }.filterNot { it in before }
        return copy(list = updated, fromContacts = fromContacts + picked)
    }

    /** A chip's ✕ was tapped: [person] is no longer selected. */
    fun unchoose(person: NewInvitee): AddPlayersUiState =
        copy(list = list.unchoose(person), fromContacts = fromContacts - person.phone.toPhoneDigits())

    companion object {
        /** How many first names [contactsSummary] lists before "+N". */
        private const val SUMMARY_NAMES = 3

        /** The sheet as it opens, with nothing selected, for a game that already has [invitees]. */
        fun open(organizerPhone: String, invitees: List<NewInvitee>): AddPlayersUiState =
            AddPlayersUiState(InviteList(organizerPhone = organizerPhone, invited = invitees))
    }
}

/** The sheet with one person entered by name and number selected. */
val previewAddPlayersUiState = AddPlayersUiState(list = previewInviteList)

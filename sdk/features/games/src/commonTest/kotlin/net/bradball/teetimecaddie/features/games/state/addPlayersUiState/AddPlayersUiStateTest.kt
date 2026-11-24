package net.bradball.teetimecaddie.features.games.state.addPlayersUiState

import net.bradball.teetimecaddie.core.models.InviteList
import net.bradball.teetimecaddie.core.models.InviteePhoneError
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.core.models.ManualInviteeCheck
import net.bradball.teetimecaddie.core.models.NewInvitee
import net.bradball.teetimecaddie.features.games.GMR
import net.bradball.teetimecaddie.features.games.ManualInviteeResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AddPlayersUiStateTest {

    private val matt = NewInvitee("Matt Chen", "4155550188")
    private val jeff = NewInvitee("Jeff Morrison", "4155550142")
    private val alex = NewInvitee("Alex Kim", "4155550150")
    private val dana = NewInvitee("Dana Park", "4155550117")
    private val sam = NewInvitee("Sam Tanaka", "4155550123")

    private val opened = AddPlayersUiState.open(organizerPhone = "5025551234", invitees = listOf(matt))

    private fun AddPlayersUiState.withChosen(vararg people: NewInvitee) =
        copy(list = list.choose(people.toList()))

    private val rejectedForBlankName =
        ManualInviteeResult.Rejected(ManualInviteeCheck(isNameBlank = true, phoneError = InviteePhoneError.MISSING))

    // MARK: opening

    @Test
    fun theSheetOpensEmptyWithTheGamesInvitees() {
        assertEquals(InviteList("5025551234", invited = listOf(matt)), opened.list)
        assertEquals("", opened.name)
        assertEquals("", opened.phone)
        assertTrue(opened.chosen.isEmpty())
        assertNull(opened.contactsSummary)
        assertNull(opened.chosenMessage)
    }

    // MARK: the bottom button

    @Test
    fun theBottomButtonIsDisabledUntilSomeoneIsSelected() {
        assertEquals(LocalizedText(GMR.strings.add_players_select_button), opened.addPlayersLabel)
        assertFalse(opened.canAddPlayers)
    }

    @Test
    fun theBottomButtonCountsTheSelection() {
        assertEquals(LocalizedText(GMR.strings.add_players_add_one_button), opened.withChosen(sam).addPlayersLabel)

        val three = opened.withChosen(sam, jeff, alex)
        assertEquals(LocalizedText(GMR.strings.add_players_add_many_button, listOf("3")), three.addPlayersLabel)
        assertTrue(three.canAddPlayers)
    }

    // MARK: field errors

    @Test
    fun noErrorsShowBeforeTheFirstAdd() {
        val typing = opened.withName("").withPhone("415555")

        assertNull(typing.nameError)
        assertNull(typing.phoneError)
    }

    @Test
    fun aRejectedAddShowsEachFieldsError() {
        val tried = opened.afterEntry(rejectedForBlankName)

        assertEquals(GMR.strings.add_players_error_no_name, tried.nameError)
        assertEquals(GMR.strings.add_players_error_no_phone, tried.phoneError)
        assertEquals(GMR.strings.add_players_error_invalid_phone, tried.withPhone("(415) 555").phoneError)
    }

    @Test
    fun afterARejectedAddEachErrorClearsOnceItsFieldIsFixed() {
        val tried = opened.afterEntry(rejectedForBlankName)

        assertNull(tried.withName("Sam").nameError)
        assertNull(tried.withPhone("(415) 555-0123").phoneError)
    }

    @Test
    fun aDuplicateNumberShowsAsSoonAsItIsCompleteWithoutAnAdd() {
        assertNull(opened.withPhone("(415) 555-018").phoneError)
        assertEquals(GMR.strings.add_players_error_duplicate_phone, opened.withPhone("(415) 555-0188").phoneError)
        assertEquals(
            GMR.strings.add_players_error_duplicate_phone,
            opened.withChosen(sam).withPhone("4155550123").phoneError
        )
    }

    // MARK: adding someone entered

    @Test
    fun aChosenEntryClearsTheFormAndSaysWhoWasSelected() {
        val list = opened.list.choose(listOf(sam))
        val added = opened.withName("Sam Tanaka").withPhone("(415) 555-0123")
            .afterEntry(rejectedForBlankName)
            .afterEntry(ManualInviteeResult.Chosen(sam, list))

        assertEquals(list, added.list)
        assertEquals("", added.name)
        assertEquals("", added.phone)
        assertNull(added.nameError)
        assertNull(added.phoneError)
        assertEquals(LocalizedText(GMR.strings.add_players_person_selected, listOf("Sam Tanaka")), added.chosenMessage)
    }

    @Test
    fun typingAgainClearsTheSelectedMessage() {
        val added = opened.afterEntry(ManualInviteeResult.Chosen(sam, opened.list.choose(listOf(sam))))

        assertNull(added.withName("D").chosenMessage)
        assertNull(added.withPhone("4").chosenMessage)
    }

    // MARK: contacts

    @Test
    fun theContactsSummaryCountsOnlyPickedContacts() {
        val before = opened.withChosen(sam)
        val state = before.afterContactsPicked(before.list, before.list.choose(listOf(dana, jeff)))

        assertEquals(
            LocalizedText(GMR.strings.add_players_contacts_summary, listOf("2", "Dana, Jeff")),
            state.contactsSummary
        )
    }

    @Test
    fun theContactsSummaryNamesThreeThenCountsTheRest() {
        val five = listOf(dana, jeff, alex, sam, NewInvitee("Priya Shah", "4155550199"))
        val state = opened.afterContactsPicked(opened.list, opened.list.choose(five))

        assertEquals(
            LocalizedText(GMR.strings.add_players_contacts_summary_more, listOf("5", "Dana, Jeff, Alex", "2")),
            state.contactsSummary
        )
    }

    @Test
    fun unchoosingAPickedContactDropsThemFromTheSummaryAndTheTray() {
        val state = opened.afterContactsPicked(opened.list, opened.list.choose(listOf(dana, jeff))).unchoose(dana)

        assertEquals(listOf(jeff), state.chosen)
        assertEquals(LocalizedText(GMR.strings.add_players_contacts_summary, listOf("1", "Jeff")), state.contactsSummary)
        assertNull(state.unchoose(jeff).contactsSummary)
    }

    @Test
    fun someoneEnteredWhileContactsAreReadIsKept() {
        val read = opened.list.choose(listOf(dana, jeff))
        val entered = opened.afterEntry(ManualInviteeResult.Chosen(sam, opened.list.choose(listOf(sam))))

        val state = entered.afterContactsPicked(opened.list, read)

        assertEquals(listOf(sam, dana, jeff), state.chosen)
        assertEquals(
            LocalizedText(GMR.strings.add_players_contacts_summary, listOf("2", "Dana, Jeff")),
            state.contactsSummary
        )
    }

    @Test
    fun someoneUnchosenWhileContactsAreReadStaysUnchosen() {
        val before = opened.withChosen(sam)
        val read = before.list.choose(listOf(dana))

        val state = before.unchoose(sam).afterContactsPicked(before.list, read)

        assertEquals(listOf(dana), state.chosen)
    }

    @Test
    fun aPickedContactAlreadyEnteredDuringTheReadIsNotCountedAsFromContacts() {
        val read = opened.list.choose(listOf(dana))
        val entered = opened.afterEntry(ManualInviteeResult.Chosen(dana, opened.list.choose(listOf(dana))))

        val state = entered.afterContactsPicked(opened.list, read)

        assertEquals(listOf(dana), state.chosen)
        assertNull(state.contactsSummary)
    }
}

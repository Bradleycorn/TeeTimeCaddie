package net.bradball.teetimecaddie.features.games

import kotlinx.coroutines.test.runTest
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.EventPlugin
import net.bradball.teetimecaddie.core.models.NewInvitee
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.core.models.TypedInviteeCheck
import net.bradball.teetimecaddie.core.models.TypedPhoneError
import net.bradball.teetimecaddie.core.models.previewPlayer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** The Add players sheet's selection: typed people, picked contacts, removing and committing. */
class ContactsSelectionTest {

    private var deviceContacts: List<DeviceContact> = emptyList()
    private var readFailure: Exception? = null

    private val events = mutableListOf<AnalyticsEvent>()
    private val eventManager = EventManager.getInstance(enableLogging = false).apply {
        registerPlugin(object : EventPlugin {
            override fun logEvent(event: AnalyticsEvent): Boolean = events.add(event)
            override fun setUserId(userId: String) = Unit
        })
    }

    private val repository = ContactsRepositoryImpl(
        eventManager,
        object : DeviceContactsReader {
            override suspend fun readContacts(): List<DeviceContact> =
                readFailure?.let { throw it } ?: deviceContacts
        }
    )

    private val organizer = previewPlayer.copy(name = "Pat Organizer", phone = "5025551234")
    private val matt = NewInvitee("Matt Chen", "(415) 555-0188")
    private val dana = NewInvitee("Dana Park", "4155550117")

    private fun contact(id: String, name: String, phone: String) =
        DeviceContact(id, name, listOf(DevicePhone(phone, "mobile", isMobile = true)))

    private fun check(
        name: String,
        phone: String,
        invitees: List<NewInvitee> = emptyList(),
        selection: List<NewInvitee> = emptyList()
    ): TypedInviteeCheck = repository.checkTypedInvitee(organizer, invitees, selection, name, phone)

    private fun select(
        name: String,
        phone: String,
        invitees: List<NewInvitee> = emptyList(),
        selection: List<NewInvitee> = emptyList()
    ): TypedInviteeResult = repository.selectTypedInvitee(organizer, invitees, selection, name, phone)

    // MARK: checking a typed person

    @Test
    fun aNameAndACompleteNewNumberAreValid() {
        assertEquals(TypedInviteeCheck(isNameBlank = false, phoneError = null), check("Sam Tanaka", "(415) 555-0123"))
    }

    @Test
    fun aBlankOrSpacesOnlyNameIsBlank() {
        assertTrue(check("", "4155550123").isNameBlank)
        assertTrue(check("   ", "4155550123").isNameBlank)
    }

    @Test
    fun aMissingNumberIsReportedAsMissing() {
        assertEquals(TypedPhoneError.MISSING, check("Sam", "").phoneError)
    }

    @Test
    fun aNumberShortOfTenDigitsIsIncomplete() {
        assertEquals(TypedPhoneError.INCOMPLETE, check("Sam", "(415) 555-012").phoneError)
    }

    @Test
    fun bothFieldsAreCheckedTogether() {
        assertEquals(TypedInviteeCheck(isNameBlank = true, phoneError = TypedPhoneError.MISSING), check(" ", ""))
    }

    @Test
    fun aNumberAlreadyInvitedIsADuplicateWhateverTheName() {
        assertEquals(TypedPhoneError.DUPLICATE, check("Matthew", "4155550188", invitees = listOf(matt)).phoneError)
    }

    @Test
    fun aNumberAlreadySelectedIsADuplicate() {
        assertEquals(TypedPhoneError.DUPLICATE, check("Dana P", "(415) 555-0117", selection = listOf(dana)).phoneError)
    }

    @Test
    fun theOrganizersOwnNumberIsADuplicate() {
        assertEquals(TypedPhoneError.DUPLICATE, check("Me", "(502) 555-1234").phoneError)
    }

    @Test
    fun anIncompleteNumberIsNotYetADuplicate() {
        assertEquals(TypedPhoneError.INCOMPLETE, check("Matt", "415555018", invitees = listOf(matt)).phoneError)
    }

    // MARK: selecting a typed person

    @Test
    fun aValidPersonJoinsTheEndOfTheSelectionWithATidiedName() {
        val result = assertIs<TypedInviteeResult.Selected>(
            select("  Sam   Tanaka ", "(415) 555-0123", selection = listOf(dana))
        )

        val sam = NewInvitee("Sam Tanaka", "4155550123")
        assertEquals(sam, result.invitee)
        assertEquals(listOf(dana, sam), result.selection)
        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.AddTypedInvitee), events)
    }

    @Test
    fun anInvalidPersonIsRejectedWithTheCheckAndNothingLogged() {
        val result = assertIs<TypedInviteeResult.Rejected>(select("", "4155550188", invitees = listOf(matt)))

        assertEquals(TypedInviteeCheck(isNameBlank = true, phoneError = TypedPhoneError.DUPLICATE), result.check)
        assertTrue(events.isEmpty())
    }

    // MARK: selecting picked contacts

    @Test
    fun pickedContactsJoinTheSelectionSkippingInvitedSelectedAndTheOrganizer() = runTest {
        deviceContacts = listOf(
            contact("c1", "Matthew Chen", "4155550188"),  // invited as Matt Chen
            contact("c2", "Dana P", "415.555.0117"),      // already selected
            contact("c3", "Pat Me", "502-555-1234"),      // the organizer
            contact("c4", "Jeff Morrison", "(415) 555-0142"),
            contact("c5", "Alex Kim", "4155550150"),
            contact("c6", "Alexander Kim", "4155550150")  // same number as Alex
        )

        val result = repository.selectPickedContacts(organizer, invitees = listOf(matt), selection = listOf(dana))

        val selection = assertIs<TtcResult.Success<List<NewInvitee>>>(result).data
        assertEquals(listOf("Dana Park", "Alex Kim", "Jeff Morrison"), selection.map { it.name })
        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.AddInvitees(pickedCount = 6, addedCount = 2)), events)
    }

    @Test
    fun aFailedReadLeavesTheSelectionAloneAndLogsNoAdd() = runTest {
        readFailure = IllegalStateException("denied")

        val result = repository.selectPickedContacts(organizer, invitees = emptyList(), selection = listOf(dana))

        val error = assertIs<GameException>(assertIs<TtcResult.Failure>(result).error)
        assertEquals(GameErrors.CONTACTS_LOAD_FAILED, error.error)
        assertTrue(events.none { it is AnalyticsEvent.AddInvitees })
    }

    // MARK: removing and committing

    @Test
    fun deselectRemovesThePersonByNumber() {
        val jeff = NewInvitee("Jeff Morrison", "4155550142")

        assertEquals(listOf(matt, jeff), repository.deselect(listOf(matt, dana, jeff), dana.copy(phone = "415-555-0117")))
    }

    @Test
    fun commitAddsTheSelectionAfterTheInviteesInTheOrderChosen() {
        val jeff = NewInvitee("Jeff Morrison", "4155550142")

        assertEquals(listOf(matt, jeff, dana), repository.commitSelection(listOf(matt), listOf(jeff, dana)))
    }

    @Test
    fun commitNeverAddsANumberThatsAlreadyInvited() {
        val again = NewInvitee("Matthew Chen", "4155550188")

        assertEquals(listOf(matt, dana), repository.commitSelection(listOf(matt), listOf(again, dana)))
    }
}

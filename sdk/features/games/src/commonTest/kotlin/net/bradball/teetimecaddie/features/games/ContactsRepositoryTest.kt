package net.bradball.teetimecaddie.features.games

import kotlinx.coroutines.test.runTest
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.EventPlugin
import net.bradball.teetimecaddie.core.models.InvitableContact
import net.bradball.teetimecaddie.core.models.NewInvitee
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.core.models.previewPlayer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ContactsRepositoryTest {

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

    private suspend fun read(vararg contacts: DeviceContact): List<InvitableContact> {
        deviceContacts = contacts.toList()
        return assertIs<TtcResult.Success<List<InvitableContact>>>(repository.invitableContacts()).data
    }

    private fun contact(id: String, name: String, vararg phones: Pair<String, String?>) =
        DeviceContact(id, name, phones.map { (number, label) -> DevicePhone(number, label, isMobile = label == "mobile") })

    // MARK: reading

    @Test
    fun aContactWithSeveralNumbersIsListedOnceAtItsMobileNumber() = runTest {
        val rows = read(contact("c1", "Jeff Morrison", "502-555-0199" to "work", "(415) 555-0142" to "mobile"))

        assertEquals(listOf(InvitableContact("c1|4155550142", "Jeff Morrison", "(415) 555-0142", "mobile")), rows)
    }

    @Test
    fun aContactWithNoMobileNumberIsListedAtItsFirstNumber() = runTest {
        val rows = read(contact("c1", "Matt Chen", "415.555.0188" to "home", "502-555-0199" to "work"))

        assertEquals(listOf(InvitableContact("c1|4155550188", "Matt Chen", "415.555.0188", "home")), rows)
    }

    @Test
    fun anUnusableMobileNumberIsPassedOverForAUsableOne() = runTest {
        val rows = read(contact("c1", "Dana Park", "611" to "mobile", "4155550117" to "home"))

        assertEquals(listOf(InvitableContact("c1|4155550117", "Dana Park", "4155550117", "home")), rows)
    }

    @Test
    fun theSameNumberOnTwoContactsIsListedForEach() = runTest {
        val rows = read(
            contact("c1", "Dana Park", "4155550117" to "home"),
            contact("c2", "Sam Park", "4155550117" to "home")
        )

        assertEquals(listOf("c1|4155550117", "c2|4155550117"), rows.map { it.id })
    }

    @Test
    fun contactsWithoutANameOrAUsableNumberAreLeftOut() = runTest {
        val rows = read(
            contact("c1", "No Number"),
            contact("c2", "   ", "4155550101" to "mobile"),
            contact("c3", "Short Code", "611" to null),
            contact("c4", "Too Long", "1234567890123456" to null),
            contact("c5", "Priya Shah", "611" to "other", "+44 20 7946 0958" to "mobile")
        )

        assertEquals(listOf(InvitableContact("c5|442079460958", "Priya Shah", "+44 20 7946 0958", "mobile")), rows)
    }

    @Test
    fun namesAreTrimmedAndBlankLabelsAreNull() = runTest {
        val rows = read(contact("c1", "  Alex Rivera ", "4155550163" to "  "))

        assertEquals(listOf(InvitableContact("c1|4155550163", "Alex Rivera", "4155550163", null)), rows)
    }

    @Test
    fun rowsAreSortedByNameIgnoringCase() = runTest {
        val rows = read(
            contact("c1", "sam Tanaka", "4155550124" to null),
            contact("c2", "Jeff Morrison", "5025550199" to "work"),
            contact("c3", "Alex Rivera", "4155550163" to null)
        )

        assertEquals(listOf("c3|4155550163", "c2|5025550199", "c1|4155550124"), rows.map { it.id })
    }

    @Test
    fun aFailedReadIsReportedNotThrown() = runTest {
        readFailure = IllegalStateException("contacts store unavailable")

        val result = assertIs<TtcResult.Failure>(repository.invitableContacts())

        assertEquals(GameErrors.CONTACTS_LOAD_FAILED, assertIs<GameException>(result.error).error)
    }

    // MARK: search

    private val jeff = InvitableContact("c1|4155550142", "Jeff Morrison", "(415) 555-0142", "mobile")
    private val jeffWork = InvitableContact("c1|5025550199", "Jeff Morrison", "502-555-0199", "work")
    private val matt = InvitableContact("c2|4155550188", "Matt Chen", "4155550188", "mobile")
    private val dana = InvitableContact("c3|4155550117", "Dana Park", "4155550117", "home")
    private val contacts = listOf(dana, jeff, jeffWork, matt)

    /** The signed-in player creating the game, at a number none of the contacts above have. */
    private val organizer = previewPlayer.copy(name = "Pat Organizer", phone = "5025551234")

    @Test
    fun searchMatchesAnyPartOfTheNameIgnoringCase() {
        assertEquals(listOf(jeff, jeffWork), repository.search(contacts, "MORR"))
        assertEquals(listOf(dana), repository.search(contacts, " park "))
        assertEquals(emptyList(), repository.search(contacts, "4155"))
    }

    @Test
    fun aBlankSearchMatchesEveryone() {
        assertEquals(contacts, repository.search(contacts, "  "))
    }

    // MARK: picking

    @Test
    fun invitedContactsAreThoseWithTheSameNumberWhateverTheName() {
        val invitees = listOf(
            NewInvitee("Jeff Morrison", "(415) 555-0142"),
            NewInvitee("Someone Else", "+1 415-555-0188")
        )

        assertEquals(setOf(jeff.id, matt.id), repository.invitedContactIds(contacts, invitees))
    }

    @Test
    fun pickingKeepsCheckedInviteesInPlaceAndAppendsNewOnesInTheOrderChecked() {
        val invitees = listOf(matt.toNewInvitee(), jeff.toNewInvitee())

        val picked = repository.pickInvitees(organizer, invitees, contacts, listOf(matt.id, jeff.id, dana.id, jeffWork.id))

        assertEquals(
            listOf(matt.toNewInvitee(), jeff.toNewInvitee(), dana.toNewInvitee(), jeffWork.toNewInvitee()),
            picked
        )
    }

    @Test
    fun uncheckingAContactRemovesItsInvitee() {
        val invitees = listOf(matt.toNewInvitee(), jeff.toNewInvitee())

        assertEquals(listOf(jeff.toNewInvitee()), repository.pickInvitees(organizer, invitees, contacts, listOf(jeff.id)))
        assertEquals(emptyList(), repository.pickInvitees(organizer, invitees, contacts, emptyList()))
    }

    @Test
    fun inviteesNotAmongTheContactsAreKept() {
        val typedIn = NewInvitee("Riley O'Neil", "4155550170")

        assertEquals(
            listOf(typedIn, dana.toNewInvitee()),
            repository.pickInvitees(organizer, listOf(typedIn), contacts, listOf(dana.id))
        )
    }

    @Test
    fun anInviteeWrittenDifferentlyIsStillMatchedAndNotAddedTwice() {
        val invitee = NewInvitee("Jeff Morrison", "+1 415 555 0142")

        assertEquals(listOf(invitee), repository.pickInvitees(organizer, listOf(invitee), contacts, listOf(jeff.id)))
    }

    @Test
    fun aContactAtANumberAlreadyInvitedUnderAnotherNameIsNotAdded() {
        val invitee = NewInvitee("Matty", "(415) 555-0188")
        val mattAtHome = InvitableContact("c9|4155550188", "Matthew Chen", "415.555.0188", "home")

        assertEquals(
            listOf(invitee),
            repository.pickInvitees(organizer, listOf(invitee), contacts + mattAtHome, listOf(matt.id, mattAtHome.id))
        )
    }

    @Test
    fun aContactAtTheOrganizersNumberIsNotAdded() {
        val me = InvitableContact("c9|5025551234", "Me", "+1 (502) 555-1234", "mobile")

        assertEquals(
            listOf(dana.toNewInvitee()),
            repository.pickInvitees(organizer, emptyList(), contacts + me, listOf(me.id, dana.id))
        )
    }

    @Test
    fun twoCheckedContactsAtTheSameNumberAddOnlyTheFirst() {
        val sam = InvitableContact("c9|4155550117", "Sam Park", "(415) 555-0117", "home")

        assertEquals(
            listOf(sam.toNewInvitee()),
            repository.pickInvitees(organizer, emptyList(), contacts + sam, listOf(sam.id, dana.id))
        )
    }

    // MARK: adding picked contacts

    @Test
    fun pickedContactsJoinTheEndSortedByNameWithoutInvitingAnyoneTwice() = runTest {
        deviceContacts = listOf(
            contact("c2", "Matt Chen", "+1 415 555 0188" to "mobile"),
            contact("c1", "Jeff Morrison", "(415) 555-0142" to "mobile"),
            contact("c3", "Dana Park", "4155550117" to "home")
        )
        val invitees = listOf(NewInvitee("Matt Chen", "4155550188"))

        val result = assertIs<TtcResult.Success<List<NewInvitee>>>(repository.addPickedContacts(organizer, invitees))

        assertEquals(
            listOf(NewInvitee("Matt Chen", "4155550188"), dana.toNewInvitee(), jeff.toNewInvitee()),
            result.data
        )
    }

    @Test
    fun pickedDuplicatesAndTheOrganizerAreSkippedSilently() = runTest {
        deviceContacts = listOf(
            contact("c1", "Matthew Chen", "415.555.0188" to "home"),
            contact("c2", "Pat (me)", "502-555-1234" to "mobile"),
            contact("c3", "Dana Park", "4155550117" to "home"),
            contact("c4", "Sam Park", "(415) 555-0117" to "home")
        )
        val invitees = listOf(NewInvitee("Matt Chen", "4155550188"))

        val result = assertIs<TtcResult.Success<List<NewInvitee>>>(repository.addPickedContacts(organizer, invitees))

        assertEquals(listOf(NewInvitee("Matt Chen", "4155550188"), dana.toNewInvitee()), result.data)
        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.AddInvitees(pickedCount = 4, addedCount = 1)), events)
    }

    @Test
    fun addingPickedContactsLogsHowManyWerePickedAndAdded() = runTest {
        deviceContacts = listOf(
            contact("c1", "Jeff Morrison", "(415) 555-0142" to "mobile"),
            contact("c2", "Matt Chen", "4155550188" to "mobile"),
            contact("c3", "Pizza Place", "611" to null)
        )

        repository.addPickedContacts(organizer, listOf(matt.toNewInvitee()))

        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.AddInvitees(pickedCount = 3, addedCount = 1)), events)
    }

    @Test
    fun aFailedReadOfPickedContactsIsReportedAndLogsNothing() = runTest {
        readFailure = IllegalStateException("grant expired")

        val result = assertIs<TtcResult.Failure>(repository.addPickedContacts(organizer, emptyList()))

        assertEquals(GameErrors.CONTACTS_LOAD_FAILED, assertIs<GameException>(result.error).error)
        assertEquals(emptyList(), events)
    }
}

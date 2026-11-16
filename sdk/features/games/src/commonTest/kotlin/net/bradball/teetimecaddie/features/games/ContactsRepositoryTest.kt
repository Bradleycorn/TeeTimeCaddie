package net.bradball.teetimecaddie.features.games

import kotlinx.coroutines.test.runTest
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.EventPlugin
import net.bradball.teetimecaddie.core.models.InviteList
import net.bradball.teetimecaddie.core.models.InviteePhoneError
import net.bradball.teetimecaddie.core.models.ManualInviteeCheck
import net.bradball.teetimecaddie.core.models.NewInvitee
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.core.models.previewPlayer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

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

    private val organizer = previewPlayer.copy(name = "Pat Organizer", phone = "5025551234")
    private val empty = InviteList(organizerPhone = organizer.phone)
    private val matt = NewInvitee("Matt Chen", "4155550188")
    private val dana = NewInvitee("Dana Park", "4155550117")

    private fun contact(id: String, name: String, vararg phones: Pair<String, String?>) =
        DeviceContact(id, name, phones.map { (number, label) -> DevicePhone(number, label, isMobile = label == "mobile") })

    /** The people chosen when [contacts] are picked onto [list]. */
    private suspend fun choose(vararg contacts: DeviceContact, list: InviteList = empty): List<NewInvitee> {
        deviceContacts = contacts.toList()
        return assertIs<TtcResult.Success<InviteList>>(repository.choosePickedContacts(list)).data.chosen
    }

    // MARK: choosing picked contacts

    @Test
    fun aContactWithSeveralNumbersIsChosenAtItsMobileNumber() = runTest {
        val chosen = choose(contact("c1", "Jeff Morrison", "502-555-0199" to "work", "(415) 555-0142" to "mobile"))

        assertEquals(listOf(NewInvitee("Jeff Morrison", "(415) 555-0142")), chosen)
    }

    @Test
    fun aContactWithNoMobileNumberIsChosenAtItsFirstNumber() = runTest {
        val chosen = choose(contact("c1", "Matt Chen", "415.555.0188" to "home", "502-555-0199" to "work"))

        assertEquals(listOf(NewInvitee("Matt Chen", "415.555.0188")), chosen)
    }

    @Test
    fun anUnusableMobileNumberIsPassedOverForAUsableOne() = runTest {
        val chosen = choose(contact("c1", "Dana Park", "611" to "mobile", "4155550117" to "home"))

        assertEquals(listOf(dana), chosen)
    }

    @Test
    fun contactsWithoutANameOrAUsableNumberAreLeftOut() = runTest {
        val chosen = choose(
            contact("c1", "No Number"),
            contact("c2", "   ", "4155550101" to "mobile"),
            contact("c3", "Short Code", "611" to null),
            contact("c4", "Too Long", "1234567890123456" to null),
            contact("c5", "Priya Shah", "611" to "other", "+44 20 7946 0958" to "mobile")
        )

        assertEquals(listOf(NewInvitee("Priya Shah", "+44 20 7946 0958")), chosen)
    }

    @Test
    fun namesAndNumbersLoseSurroundingSpaces() = runTest {
        val chosen = choose(contact("c1", "  Alex Rivera ", " 4155550163 " to null))

        assertEquals(listOf(NewInvitee("Alex Rivera", "4155550163")), chosen)
    }

    @Test
    fun pickedContactsAreChosenSortedByNameIgnoringCase() = runTest {
        val chosen = choose(
            contact("c1", "sam Tanaka", "4155550124" to null),
            contact("c2", "Jeff Morrison", "5025550199" to "work"),
            contact("c3", "Alex Rivera", "4155550163" to null)
        )

        assertEquals(listOf("Alex Rivera", "Jeff Morrison", "sam Tanaka"), chosen.map { it.name })
    }

    @Test
    fun pickedContactsAlreadyInTheListOrTheOrganizerAreSkippedAndLogged() = runTest {
        val chosen = choose(
            contact("c1", "Matthew Chen", "415.555.0188" to "home"),
            contact("c2", "Pat (me)", "502-555-1234" to "mobile"),
            contact("c3", "Dana P", "(415) 555-0117" to "home"),
            contact("c4", "Jeff Morrison", "(415) 555-0142" to "mobile"),
            list = InviteList(organizer.phone, invited = listOf(matt), chosen = listOf(dana))
        )

        assertEquals(listOf(dana, NewInvitee("Jeff Morrison", "(415) 555-0142")), chosen)
        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.AddInvitees(pickedCount = 4, addedCount = 1)), events)
    }

    @Test
    fun aFailedReadIsReportedAndLogsNothing() = runTest {
        readFailure = IllegalStateException("grant expired")

        val result = assertIs<TtcResult.Failure>(repository.choosePickedContacts(empty))

        assertEquals(GameErrors.CONTACTS_LOAD_FAILED, assertIs<GameException>(result.error).error)
        assertTrue(events.none { it is AnalyticsEvent.AddInvitees })
    }

    // MARK: choosing someone entered

    @Test
    fun anEnteredPersonIsChosenWithATidiedNameAndDigitsOnly() {
        val result = assertIs<ManualInviteeResult.Chosen>(
            repository.chooseEnteredPerson(empty.copy(chosen = listOf(dana)), "  Sam   Tanaka ", "(415) 555-0123")
        )

        val sam = NewInvitee("Sam Tanaka", "4155550123")
        assertEquals(sam, result.person)
        assertEquals(listOf(dana, sam), result.list.chosen)
        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.AddManualInvitee), events)
    }

    @Test
    fun anInvalidEntryIsRejectedWithTheCheckAndNothingLogged() {
        val result = assertIs<ManualInviteeResult.Rejected>(
            repository.chooseEnteredPerson(empty.copy(invited = listOf(matt)), "", "415-555-0188")
        )

        assertEquals(ManualInviteeCheck(isNameBlank = true, phoneError = InviteePhoneError.DUPLICATE), result.check)
        assertTrue(events.isEmpty())
    }

    // MARK: adding picked contacts straight to the invitees

    @Test
    fun addPickedContactsAppendsThemToTheInviteesSkippingDuplicates() = runTest {
        deviceContacts = listOf(
            contact("c1", "Matthew Chen", "+1 415 555 0188" to "mobile"),
            contact("c2", "Pat (me)", "502-555-1234" to "mobile"),
            contact("c3", "Dana Park", "4155550117" to "home")
        )

        val result = assertIs<TtcResult.Success<List<NewInvitee>>>(repository.addPickedContacts(organizer, listOf(matt)))

        assertEquals(listOf(matt, dana), result.data)
        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.AddInvitees(pickedCount = 3, addedCount = 1)), events)
    }
}

package net.bradball.teetimecaddie.features.games.state.gameDetailsUiState

import net.bradball.teetimecaddie.core.extensions.formattedTime
import net.bradball.teetimecaddie.core.extensions.toPhoneDigits
import net.bradball.teetimecaddie.core.extensions.weekdayMonthDay
import net.bradball.teetimecaddie.core.models.AvatarTone
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.GamePlayer
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.core.models.previewGame
import net.bradball.teetimecaddie.core.models.previewGameInvitee
import net.bradball.teetimecaddie.core.models.previewGameInviteeList
import net.bradball.teetimecaddie.core.models.previewGamePlayerList
import net.bradball.teetimecaddie.core.models.previewPlayer
import net.bradball.teetimecaddie.core.models.previewPlayerList
import net.bradball.teetimecaddie.features.games.GMR
import net.bradball.teetimecaddie.features.games.GameErrors
import net.bradball.teetimecaddie.features.games.GameException

/**
 * A game's details, already formatted, as the signed-in player sees them. The case is the view:
 * the organizer's, or an invitee's.
 *
 * @property course The course name.
 * @property date The game's date, e.g. "Sat, Jun 13".
 * @property teeTimes The game's tee times, earliest first.
 */
sealed interface GameUiState {
    val course: String
    val date: String
    val teeTimes: List<GameTeeTimeUiState>

    /**
     * The organizer's view, with the "You are the organizer" chip and the Declined section.
     *
     * @property declined Everyone who said no, in the order they were invited. Empty when nobody
     *   has, and the section is left out.
     */
    data class Organizer(
        override val course: String,
        override val date: String,
        override val teeTimes: List<GameTeeTimeUiState>,
        val declined: List<DeclinedInviteeUiState> = emptyList()
    ) : GameUiState {
        /** The Declined section's heading, with how many declined, e.g. "Declined · 2". */
        val declinedHeading: LocalizedText
            get() = LocalizedText(GMR.strings.game_details_declined_heading, listOf(declined.size.toString()))
    }

    /**
     * An invitee's view, with the RSVP card.
     *
     * @property rsvp The invitee's answer to their invitation, for the RSVP card. Null only if no
     *   invitation is found for them, which shouldn't happen; the card is then left out.
     * @property organizedBy Shown in place of [date]: the date and who organized the game, e.g.
     *   "Sat, Jun 13 · Organized by Brad". Null if the organizer isn't known.
     */
    data class Invitee(
        override val course: String,
        override val date: String,
        override val teeTimes: List<GameTeeTimeUiState>,
        val rsvp: RsvpUiState?,
        val organizedBy: LocalizedText? = null
    ) : GameUiState
}

/**
 * This game's details as [viewer] sees them, or null if [viewer] has no part in the game: they
 * neither organized it nor were invited to it.
 *
 * Invitees belong to the game rather than to a tee time, so those who haven't answered are listed
 * under the first tee time — for now every game has exactly one. Someone who said yes holds a slot
 * and is listed with the slot holders; someone who said no is listed only in the organizer's
 * Declined section. Only the tee time's players count toward how full it is.
 */
fun Game.toUiState(viewer: Player): GameUiState? {
    val viewerPhone = viewer.phone.toPhoneDigits()
    val isOrganizer = isOrganizer(viewer.id)
    val rsvp = rsvpFor(viewerPhone)
    if (!isOrganizer && rsvp == null) return null

    val viewerName = LocalizedText(GMR.strings.game_details_viewer_name, listOf(viewer.firstName))
        .takeUnless { isOrganizer }
    // Where each invited player falls in invite order. Anyone holding a slot uninvited sorts last.
    val inviteOrder = invitees
        .mapIndexedNotNull { index, invitee -> invitee.playerId?.let { it to index } }
        .toMap()

    fun GamePlayer.toRow(): TeeTimePlayerRowUiState {
        val organizedGame = playerId == organizerId
        return TeeTimePlayerRowUiState(
            initial = initial,
            name = name,
            tone = if (organizedGame) AvatarTone.SECONDARY else AvatarTone.forName(name),
            status = if (organizedGame) TeeTimePlayerStatus.ORGANIZER else TeeTimePlayerStatus.CONFIRMED,
            viewerName = viewerName.takeIf { playerId == viewer.id }
        )
    }

    val pending = invitees.filter { it.status == InviteStatus.PENDING }
    val teeTimes = teeTimes.mapIndexed { index, teeTime ->
        val holders = teeTime.players
            .sortedWith(
                compareBy<GamePlayer> { it.playerId != organizerId }
                    .thenBy { inviteOrder[it.playerId] ?: Int.MAX_VALUE }
            )
            .map { it.toRow() }
        val invited = if (index == 0) {
            pending.map {
                TeeTimePlayerRowUiState(
                    initial = it.initial,
                    name = it.name,
                    tone = it.avatarTone,
                    status = TeeTimePlayerStatus.INVITED,
                    viewerName = viewerName.takeIf { _ -> it.phone == viewerPhone }
                )
            }
        } else {
            emptyList()
        }
        GameTeeTimeUiState(
            time = teeTime.time.formattedTime,
            filledCount = LocalizedText(
                GMR.strings.game_details_filled_count,
                listOf(teeTime.filled.toString(), teeTime.slots.toString())
            ),
            players = holders + invited,
            openSlots = (teeTime.openSlots - invited.size).coerceAtLeast(0)
        )
    }

    return if (isOrganizer) {
        GameUiState.Organizer(
            course = course.name,
            date = date.weekdayMonthDay,
            teeTimes = teeTimes,
            declined = invitees
                .filter { it.status == InviteStatus.DECLINED }
                .map { DeclinedInviteeUiState(initial = it.initial, name = it.name) }
        )
    } else {
        GameUiState.Invitee(
            course = course.name,
            date = date.weekdayMonthDay,
            teeTimes = teeTimes,
            rsvp = rsvp,
            organizedBy = organizerFirstName()
                ?.let { LocalizedText(GMR.strings.game_details_organized_by, listOf(date.weekdayMonthDay, it)) }
        )
    }
}

/** The organizer's first name, from the slot they hold, e.g. "Brad". Null if they hold none. */
internal fun Game.organizerFirstName(): String? = teeTimes
    .flatMap { it.players }
    .firstOrNull { it.playerId == organizerId }
    ?.name?.trim()?.substringBefore(' ')

/** [previewGame] as its organizer sees it: one player and three open slots. */
val previewGameUiState = previewGame.toUiState(viewer = previewPlayer) as GameUiState.Organizer

/**
 * [previewGame] with three invitees who haven't answered and one who declined, as its organizer
 * sees it: no open slots left to show, and a Declined section.
 */
val previewGameWithInviteesUiState = previewGame
    .copy(
        invitees = previewGameInviteeList +
            previewGameInvitee.copy(name = "Sam Ortiz", phone = "4155550163", status = InviteStatus.DECLINED)
    )
    .toUiState(viewer = previewPlayer) as GameUiState.Organizer

// The invitee the invitee-view previews are seen by: invited by the number on their profile.
private val previewInviteeViewer = previewPlayerList.last()
private val previewInvitation = previewGameInvitee.copy(name = previewInviteeViewer.name, phone = previewInviteeViewer.phone)

/** [previewGame] as an invitee sees it before answering: the RSVP card asks. */
val previewInvitedGameUiState = previewGame
    .copy(invitees = listOf(previewInvitation) + previewGameInviteeList.drop(1))
    .toUiState(viewer = previewInviteeViewer) as GameUiState.Invitee

/** [previewGame] as an invitee sees it after saying yes: they hold a slot, labelled "Confirmed". */
val previewAcceptedGameUiState = previewGame
    .copy(
        teeTimes = listOf(
            previewGame.teeTimes.single().let {
                it.copy(players = it.players + GamePlayer(previewInviteeViewer.id, previewInviteeViewer.name))
            }
        ),
        invitees = listOf(previewInvitation.copy(playerId = previewInviteeViewer.id, status = InviteStatus.ACCEPTED))
    )
    .toUiState(viewer = previewInviteeViewer) as GameUiState.Invitee

/** [previewGame] as an invitee sees it once confirmed players fill every slot. */
val previewFullGameUiState = previewGame
    .copy(
        teeTimes = listOf(previewGame.teeTimes.single().copy(slots = 2, players = previewGamePlayerList.take(2))),
        invitees = listOf(previewInvitation)
    )
    .toUiState(viewer = previewInviteeViewer) as GameUiState.Invitee

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
import kotlin.collections.plus

/**
 * A game's details, already formatted.
 *
 * @property course The course name.
 * @property date The game's date, e.g. "Sat, Jun 13".
 * @property organizedBy Shown in place of [date] in the invitee view: the date and who organized
 *   the game, e.g. "Sat, Jun 13 · Organized by Brad". Null in the organizer's view.
 * @property isOrganizer Whether the signed-in player organized the game. The organizer gets the
 *   "You are the organizer" chip; everyone else gets the invitee view, without it.
 * @property rsvp The signed-in player's answer to their invitation, for the RSVP card. Null when
 *   they weren't invited — the organizer, say.
 * @property teeTimes The game's tee times, earliest first.
 */
data class GameUiState(
    val course: String,
    val date: String,
    val isOrganizer: Boolean,
    val teeTimes: List<GameTeeTimeUiState>,
    val rsvp: RsvpUiState? = null,
    val organizedBy: LocalizedText? = null
)

/**
 * This game's details as [viewer] sees them. A null [viewer] is never the organizer, and has no
 * invitation to answer.
 *
 * Invitees belong to the game rather than to a tee time, so they are listed under the first tee
 * time — for now every game has exactly one. Someone who said yes holds a slot and is listed with
 * the slot holders; someone who said no isn't listed at all. Only the tee time's players count
 * toward how full it is.
 */
fun Game.toUiState(viewer: Player?): GameUiState {
    val viewerPhone = viewer?.phone?.toPhoneDigits()
    val isOrganizer = viewer != null && isOrganizer(viewer.id)
    val viewerName = viewer
        ?.takeUnless { isOrganizer }
        ?.let { LocalizedText(GMR.strings.game_details_viewer_name, listOf(it.firstName)) }
    val acceptedIds = invitees
        .filter { it.status == InviteStatus.ACCEPTED }
        .mapNotNull { it.playerId }
        .toSet()

    fun GamePlayer.toRow(): TeeTimePlayerRowUiState {
        val organizedGame = playerId == organizerId
        return TeeTimePlayerRowUiState(
            initial = initial,
            name = name,
            tone = if (organizedGame) AvatarTone.SECONDARY else AvatarTone.forName(name),
            status = when {
                organizedGame -> TeeTimePlayerStatus.ORGANIZER
                playerId in acceptedIds -> TeeTimePlayerStatus.ACCEPTED
                else -> TeeTimePlayerStatus.CONFIRMED
            },
            viewerName = viewerName.takeIf { playerId == viewer?.id }
        )
    }

    val pending = invitees.filter { it.status == InviteStatus.PENDING }
    return GameUiState(
        course = course.name,
        date = date.weekdayMonthDay,
        isOrganizer = isOrganizer,
        teeTimes = teeTimes.mapIndexed { index, teeTime ->
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
                players = teeTime.players.map { it.toRow() } + invited,
                openSlots = (teeTime.openSlots - invited.size).coerceAtLeast(0)
            )
        },
        rsvp = viewerPhone?.let { rsvpFor(it) },
        organizedBy = organizerFirstName()
            ?.takeUnless { isOrganizer }
            ?.let { LocalizedText(GMR.strings.game_details_organized_by, listOf(date.weekdayMonthDay, it)) }
    )
}

/** The organizer's first name, from the slot they hold, e.g. "Brad". Null if they hold none. */
internal fun Game.organizerFirstName(): String? = teeTimes
    .flatMap { it.players }
    .firstOrNull { it.playerId == organizerId }
    ?.name?.trim()?.substringBefore(' ')

/** [previewGame] as its organizer sees it: one player and three open slots. */
val previewGameUiState = previewGame.toUiState(viewer = previewPlayer)

/** [previewGame] with three invitees, as its organizer sees it: no open slots left to show. */
val previewGameWithInviteesUiState = previewGame
    .copy(invitees = previewGameInviteeList)
    .toUiState(viewer = previewPlayer)

// The invitee the invitee-view previews are seen by: invited by the number on their profile.
private val previewInviteeViewer = previewPlayerList.last()
private val previewInvitation = previewGameInvitee.copy(name = previewInviteeViewer.name, phone = previewInviteeViewer.phone)

/** [previewGame] as an invitee sees it before answering: the RSVP card asks. */
val previewInvitedGameUiState = previewGame
    .copy(invitees = listOf(previewInvitation) + previewGameInviteeList.drop(1))
    .toUiState(viewer = previewInviteeViewer)

/** [previewGame] as an invitee sees it after saying yes: they hold a slot, labelled "Accepted". */
val previewAcceptedGameUiState = previewGame
    .copy(
        teeTimes = listOf(
            previewGame.teeTimes.single().let {
                it.copy(players = it.players + GamePlayer(previewInviteeViewer.id, previewInviteeViewer.name))
            }
        ),
        invitees = listOf(previewInvitation.copy(playerId = previewInviteeViewer.id, status = InviteStatus.ACCEPTED))
    )
    .toUiState(viewer = previewInviteeViewer)

/** [previewGame] as an invitee sees it once confirmed players fill every slot. */
val previewFullGameUiState = previewGame
    .copy(
        teeTimes = listOf(previewGame.teeTimes.single().copy(slots = 2, players = previewGamePlayerList.take(2))),
        invitees = listOf(previewInvitation)
    )
    .toUiState(viewer = previewInviteeViewer)

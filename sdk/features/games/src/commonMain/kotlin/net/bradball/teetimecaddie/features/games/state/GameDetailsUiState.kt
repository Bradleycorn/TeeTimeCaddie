package net.bradball.teetimecaddie.features.games.state

import net.bradball.teetimecaddie.core.extensions.formattedTime
import net.bradball.teetimecaddie.core.extensions.toPhoneDigits
import net.bradball.teetimecaddie.core.extensions.weekdayMonthDay
import net.bradball.teetimecaddie.core.models.AvatarTone
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.GamePlayer
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.core.models.exceptions.TeeTimeCaddieException
import net.bradball.teetimecaddie.core.models.previewGame
import net.bradball.teetimecaddie.core.models.previewGameInvitee
import net.bradball.teetimecaddie.core.models.previewGameInviteeList
import net.bradball.teetimecaddie.core.models.previewGamePlayerList
import net.bradball.teetimecaddie.core.models.previewPlayer
import net.bradball.teetimecaddie.core.models.previewPlayerList
import net.bradball.teetimecaddie.features.games.GMR

/** State for a game's details. */
sealed interface GameDetailsUiState {
    data object Loading : GameDetailsUiState

    data class Content(val game: GameUiState) : GameDetailsUiState

    /** The game could not be read, or no longer exists. */
    data class Error(val title: LocalizedText, val message: LocalizedText) : GameDetailsUiState {
        /** The failure's own title and message, with the message's placeholders filled. */
        constructor(error: TeeTimeCaddieException) : this(
            title = LocalizedText(error.title),
            message = LocalizedText(error.displayMessage, error.messageArgs)
        )
    }
}

/**
 * A game's details, already formatted.
 *
 * @property course The course name.
 * @property date The game's date, e.g. "Sat, Jun 13".
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
    val rsvp: RsvpUiState? = null
)

/** Where the signed-in player stands on their invitation, which decides the RSVP card. */
sealed interface RsvpUiState {
    /**
     * Not answered yet: the card asks, with Yes and No.
     *
     * @property prompt "Can you make it?", or, once confirmed players fill every slot, "All 4
     *   spots are taken."
     * @property canAccept False once every slot is taken. Yes is then disabled; No still works.
     */
    data class Pending(val prompt: LocalizedText, val canAccept: Boolean) : RsvpUiState

    /** Said yes, and holds a slot. The card collapses to "You're in". */
    data object Accepted : RsvpUiState {
        val message = LocalizedText(GMR.strings.game_details_rsvp_accepted)
    }

    /** Said no. The card collapses to "You declined". */
    data object Declined : RsvpUiState {
        val message = LocalizedText(GMR.strings.game_details_rsvp_declined)
    }
}

/**
 * One tee time, formatted.
 *
 * @property time When the group tees off, e.g. "9:00 AM".
 * @property filledCount How full it is, e.g. "1/4 filled".
 * @property players One row per person on the tee time's roster: those holding a slot, in slot
 *   order, then those invited who haven't answered, in the order they were added. Never capped:
 *   there can be more rows than slots. Someone who declined isn't listed.
 * @property openSlots How many empty slots follow [players]: the slots no row has used, and never
 *   below zero.
 */
data class GameTeeTimeUiState(
    val time: String,
    val filledCount: LocalizedText,
    val players: List<TeeTimePlayerRowUiState>,
    val openSlots: Int
)

/** Where a person on a tee time's roster stands, which decides how their row is labelled. */
enum class TeeTimePlayerStatus {
    /** Organized the game, and holds a slot. Labelled "Organizer". */
    ORGANIZER,

    /** Holds a slot without having been invited. Shown without a label. */
    CONFIRMED,

    /** Said yes to their invitation, and holds a slot. Labelled "Accepted". */
    ACCEPTED,

    /** Invited, and hasn't answered. Labelled "Invited". */
    INVITED
}

/**
 * One row of a tee time's roster.
 *
 * @property initial The letter in the person's avatar.
 * @property name The person's full name, as shown for everyone but the signed-in player.
 * @property tone The avatar's color: the organizer's gold, or the person's own color from their
 *   name, which matches the one they had on the New game screen.
 * @property status Where they stand, which decides the row's label.
 * @property viewerName Set only on the signed-in player's own row in the invitee view, which
 *   shows this in place of [name]: their first name, marked "Dana (you)". The organizer's own row
 *   keeps their name and the "Organizer" label.
 */
data class TeeTimePlayerRowUiState(
    val initial: String,
    val name: String,
    val tone: AvatarTone,
    val status: TeeTimePlayerStatus,
    val viewerName: LocalizedText? = null
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
        rsvp = viewerPhone?.let { rsvpFor(it) }
    )
}

/** The RSVP card for whoever was invited by [phone], or null if nobody was. */
private fun Game.rsvpFor(phone: String): RsvpUiState? {
    val invitee = inviteeFor(phone) ?: return null
    return when (invitee.status) {
        InviteStatus.ACCEPTED -> RsvpUiState.Accepted
        InviteStatus.DECLINED -> RsvpUiState.Declined
        InviteStatus.PENDING -> {
            // Every game has exactly one tee time for now, and it is the one an invitee joins.
            val teeTime = teeTimes.firstOrNull()
            if (teeTime == null || teeTime.openSlots == 0) {
                RsvpUiState.Pending(
                    prompt = LocalizedText(GMR.strings.game_details_rsvp_full, listOf(teeTime?.slots?.toString().orEmpty())),
                    canAccept = false
                )
            } else {
                RsvpUiState.Pending(prompt = LocalizedText(GMR.strings.game_details_rsvp_prompt), canAccept = true)
            }
        }
    }
}

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

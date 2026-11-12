package net.bradball.teetimecaddie.features.games.state

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.core.models.Course
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.GameInvitee
import net.bradball.teetimecaddie.core.models.GamePlayer
import net.bradball.teetimecaddie.core.models.GameTeeTime
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.Player

// Fixtures shared by the Games list and Game details UI-state tests.

internal val organizer = GamePlayer(playerId = "brad", name = "brad Ball", phone = "5025551234")
internal val guest = GamePlayer(playerId = "dana", name = "Dana Park", phone = "5025552345")

// The players as signed in, for the viewer-dependent state.
internal val bradViewer = Player(id = "brad", name = "brad Ball", email = "brad@golf.app", phone = "5025551234")
internal val danaViewer = Player(id = "dana", name = "Dana Park", email = "dana@golf.app", phone = "5025552345")
internal val samViewer = Player(id = "sam", name = "Sam Ortiz", email = "sam@golf.app", phone = "5025553456")

/** Brad's game at Pebble Beach, with Dana holding the second of four slots. */
internal val game = Game(
    id = "game1",
    organizerId = bradViewer.id,
    course = Course(id = "pebble beach", name = "Pebble Beach"),
    date = LocalDate(2026, 6, 13),
    teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 4, players = listOf(organizer, guest))),
    invitees = emptyList()
)

/** Brad's game with only Brad holding a slot. Sam was invited and hasn't answered. */
internal val invitedGame = game.copy(
    teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 4, players = listOf(organizer))),
    invitees = listOf(invitee("Sam", phone = samViewer.phone))
)

internal fun invitee(
    name: String,
    phone: String = "4155550188",
    status: InviteStatus = InviteStatus.PENDING,
    playerId: String? = null
) = GameInvitee(name = name, phone = phone, playerId = playerId, status = status)

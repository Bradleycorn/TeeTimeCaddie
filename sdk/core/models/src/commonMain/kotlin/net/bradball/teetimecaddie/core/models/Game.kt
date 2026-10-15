package net.bradball.teetimecaddie.core.models

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/**
 * A player holding one of a tee time's slots.
 *
 * Carries the player's name as well as their id, so a game can be shown without looking each
 * player up. The name is a copy taken when the player joined the game.
 *
 * @property playerId The player's Firebase Auth user id, the same as [Player.id].
 * @property name The player's name, e.g. "Brad Ball".
 */
data class GamePlayer(
    val playerId: String,
    val name: String
) {
    /** The single uppercase letter shown in the player's avatar. */
    val initial: String
        get() = name.trim().take(1).uppercase()
}

/**
 * One tee time within a game, and the players booked into it.
 *
 * @property time When the group tees off.
 * @property slots How many players the tee time holds, from 1 to 4.
 * @property players The players occupying slots, in slot order. Never more than [slots].
 */
data class GameTeeTime(
    val time: LocalTime,
    val slots: Int,
    val players: List<GamePlayer>
) {
    /** How many slots are taken. */
    val filled: Int
        get() = players.size

    /** How many slots are still free. */
    val openSlots: Int
        get() = (slots - filled).coerceAtLeast(0)
}

/**
 * An outing an organizer has set up: a course, a date and the tee times booked for it.
 *
 * A game holds a list of tee times so that later work can add more than one. For now every game
 * has exactly one.
 *
 * @property id The game's unique id.
 * @property organizerId The player who created the game.
 * @property course Where the game is played.
 * @property date The day of the game.
 * @property teeTimes The game's tee times, earliest first.
 */
data class Game(
    val id: String,
    val organizerId: String,
    val course: Course,
    val date: LocalDate,
    val teeTimes: List<GameTeeTime>
) {
    /** Whether [playerId] organized this game. */
    fun isOrganizer(playerId: String): Boolean = playerId == organizerId
}

val previewGamePlayer = GamePlayer(
    playerId = previewPlayer.id,
    name = previewPlayer.name
)

val previewGamePlayerList = previewPlayerList.map { GamePlayer(playerId = it.id, name = it.name) }

val previewGameTeeTime = GameTeeTime(
    time = LocalTime(9, 0),
    slots = 4,
    players = listOf(previewGamePlayer)
)

@OptIn(ExperimentalTime::class)
val previewGame = Game(
    id = "previewGame",
    organizerId = previewPlayer.id,
    course = previewCourse,
    date = Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(DatePeriod(days = 1)),
    teeTimes = listOf(previewGameTeeTime)
)

val previewGameList = listOf(
    previewGame,
    previewGame.copy(
        id = "previewGame2",
        course = previewCourseList[1],
        date = previewGame.date.plus(DatePeriod(days = 3)),
        teeTimes = listOf(previewGameTeeTime.copy(time = LocalTime(8, 10), slots = 2))
    ),
    previewGame.copy(
        id = "previewGame3",
        course = previewCourseList[2],
        date = previewGame.date.plus(DatePeriod(days = 7)),
        teeTimes = listOf(previewGameTeeTime.copy(time = LocalTime(13, 30), slots = 1))
    )
)

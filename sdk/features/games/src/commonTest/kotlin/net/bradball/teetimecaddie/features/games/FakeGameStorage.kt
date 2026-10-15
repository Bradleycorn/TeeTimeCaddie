package net.bradball.teetimecaddie.features.games

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import net.bradbal.teetimecaddie.core.storage.GameStorage
import net.bradbal.teetimecaddie.core.storage.documents.CourseDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameDocument

/**
 * An in-memory [GameStorage] that behaves like the Firestore one: courses are keyed by their
 * normalised name, and a publish either writes everything or nothing.
 */
class FakeGameStorage(vararg existingCourses: CourseDocument) : GameStorage {

    val courses: MutableMap<String, CourseDocument> =
        existingCourses.associateBy { it.nameKey }.toMutableMap()

    private val games = MutableStateFlow<List<GameDocument>>(emptyList())

    /** Every game written so far. */
    val writtenGames: List<GameDocument>
        get() = games.value

    /** When set, the next storage call throws it instead of doing anything. */
    var failure: Exception? = null

    private var nextId = 1

    override suspend fun publishGame(
        courseKey: String,
        newCourse: CourseDocument,
        buildGame: (CourseDocument) -> GameDocument
    ): GameDocument {
        failure?.let { throw it }

        val course = courses[courseKey] ?: newCourse
        val game = buildGame(course).also { it.id = "game-${nextId++}" }

        courses[courseKey] = course
        games.value = games.value + game
        return game
    }

    override suspend fun getGame(id: String): GameDocument? {
        failure?.let { throw it }
        return games.value.firstOrNull { it.id == id }
    }

    override fun gamesFlow(playerId: String, from: LocalDate): Flow<List<GameDocument>> =
        games.map { all ->
            failure?.let { throw it }
            all.filter { playerId in it.playerIds && it.date >= from }.sortedBy { it.date }
        }
}

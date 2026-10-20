package net.bradbal.teetimecaddie.core.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import net.bradbal.teetimecaddie.core.storage.documents.CourseDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameDocument

private const val GAMES_COLLECTION = "games"
private const val COURSES_COLLECTION = "courses"
private const val PLAYER_IDS_FIELD = "playerIds"
private const val DATE_FIELD = "date"

/**
 * Storage for games and the courses they are played at.
 *
 * An interface, unlike the older storage classes, so `GamesRepository` can be tested against an
 * in-memory fake without Firebase.
 */
interface GameStorage {

    /**
     * Write a new game, creating its course if no course with [courseKey] exists yet.
     *
     * Atomic: the course read, the course write (if any) and the game write happen in one
     * transaction, so a failed game write never leaves an orphan course behind, and two organizers
     * publishing the same new course at once cannot create it twice.
     *
     * @param courseKey The normalised course name. Also the course document's id.
     * @param newCourse The course to create if [courseKey] is not taken.
     * @param buildGame Builds the game from the course it will be played at — the existing one when
     *   [courseKey] is taken, so the game carries the stored spelling, otherwise [newCourse].
     * @return The game as written, with its id set.
     */
    suspend fun publishGame(
        courseKey: String,
        newCourse: CourseDocument,
        buildGame: (CourseDocument) -> GameDocument
    ): GameDocument

    /**
     * Read a single game.
     *
     * @return The game, or null if no game has [id].
     */
    suspend fun getGame(id: String): GameDocument?

    /**
     * Observe the games [playerId] holds a slot in, on or after [from], earliest first.
     */
    fun gamesFlow(playerId: String, from: LocalDate): Flow<List<GameDocument>>
}

/**
 * The Firestore-backed [GameStorage].
 */
class FirestoreGameStorage : GameStorage {
    private val store = Firebase.firestore

    private val gamesCollection: CollectionReference
        get() = store.collection(GAMES_COLLECTION)

    private val coursesCollection: CollectionReference
        get() = store.collection(COURSES_COLLECTION)

    override suspend fun publishGame(
        courseKey: String,
        newCourse: CourseDocument,
        buildGame: (CourseDocument) -> GameDocument
    ): GameDocument {
        val courseRef = coursesCollection.document(courseKey)
        // An auto-id reference, made up front because a transaction can only write to a known path.
        val gameRef = gamesCollection.document

        return store.runTransaction {
            val existing = get(courseRef)
            val course = if (existing.exists) {
                existing.data<CourseDocument>()
            } else {
                set(courseRef, newCourse)
                newCourse
            }

            val game = buildGame(course)
            set(gameRef, game)
            game.also { it.id = gameRef.id }
        }
    }

    override suspend fun getGame(id: String): GameDocument? {
        val doc = gamesCollection.document(id).get()
        return if (doc.exists) {
            doc.data<GameDocument>().apply { this.id = doc.id }
        } else {
            null
        }
    }

    // array-contains plus a range on `date` needs the composite index declared in
    // firebase/firestore.indexes.json. Filtering on the server keeps reads from growing with every
    // game a player has ever played.
    override fun gamesFlow(playerId: String, from: LocalDate): Flow<List<GameDocument>> =
        gamesCollection
            .where {
                all(
                    PLAYER_IDS_FIELD contains playerId,
                    // Dates are stored as ISO strings, so compare against the same form.
                    DATE_FIELD greaterThanOrEqualTo from.toString()
                )
            }
            .orderBy(DATE_FIELD, Direction.ASCENDING)
            .snapshots(includeMetadataChanges = false)
            .map { snapshot ->
                snapshot.documents.deserialize<GameDocument>(predicate = { doc -> id = doc.id })
            }
}

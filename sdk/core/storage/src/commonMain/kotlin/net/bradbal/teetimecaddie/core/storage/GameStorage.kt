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
import net.bradbal.teetimecaddie.core.storage.documents.GameRosterDocument

private const val GAMES_COLLECTION = "games"
private const val COURSES_COLLECTION = "courses"
private const val PLAYER_IDS_FIELD = "playerIds"
private const val INVITED_PHONES_FIELD = "invitedPhones"
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
     * Observe a single game.
     *
     * Emits null if no game has [id], or once it is deleted.
     */
    fun gameFlow(id: String): Flow<GameDocument?>

    /**
     * Observe the games [playerId] holds a slot in, on or after [from], earliest first.
     */
    fun gamesFlow(playerId: String, from: LocalDate): Flow<List<GameDocument>>

    /**
     * Observe the games [phone] was invited to, on or after [from], earliest first, whether or not
     * the invitation has been answered.
     *
     * @param phone Digits only, as stored in `invitedPhones`.
     */
    fun invitationsFlow(phone: String, from: LocalDate): Flow<List<GameDocument>>

    /**
     * Change who is invited to and playing in a game, in one transaction.
     *
     * Reads the game, then writes whatever [change] returns. A transaction is retried when the game
     * changes under it, so [change] can run more than once and must only compute.
     *
     * @param change Builds the game's new roster from the game as read, or returns null to leave it
     *   unchanged.
     * @return The game as written, or as read when [change] returned null. Null if no game has [id].
     */
    suspend fun updateRoster(id: String, change: (GameDocument) -> GameRosterDocument?): GameDocument?
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

    override fun gameFlow(id: String): Flow<GameDocument?> =
        gamesCollection.document(id)
            .snapshots(includeMetadataChanges = false)
            .map { doc ->
                if (doc.exists) doc.data<GameDocument>().apply { this.id = doc.id } else null
            }

    override fun gamesFlow(playerId: String, from: LocalDate): Flow<List<GameDocument>> =
        gamesContaining(PLAYER_IDS_FIELD, playerId, from)

    override fun invitationsFlow(phone: String, from: LocalDate): Flow<List<GameDocument>> =
        gamesContaining(INVITED_PHONES_FIELD, phone, from)

    override suspend fun updateRoster(
        id: String,
        change: (GameDocument) -> GameRosterDocument?
    ): GameDocument? {
        val gameRef = gamesCollection.document(id)

        return store.runTransaction {
            val snapshot = get(gameRef)
            if (!snapshot.exists) return@runTransaction null

            val game = snapshot.data<GameDocument>().apply { this.id = snapshot.id }
            val roster = change(game) ?: return@runTransaction game

            update(gameRef, GameRosterDocument.serializer(), roster)
            game.copy(teeTimes = roster.teeTimes, playerIds = roster.playerIds, invitees = roster.invitees)
                .also { it.id = snapshot.id }
        }
    }

    // array-contains plus a range on `date` needs a composite index per array field, declared in
    // firebase/firestore.indexes.json. Filtering on the server keeps reads from growing with every
    // game a player has ever played.
    /**
     * Observe the games whose [arrayField] list holds [value], on or after [from].
     *
     * @param arrayField The name of a list field on the game, such as `playerIds`.
     * @param value The entry to look for in that list, such as a player's id.
     * @param from The earliest game date to include.
     * @return The matching games, earliest first, updated whenever they change.
     */
    private fun gamesContaining(
        arrayField: String,
        value: String,
        from: LocalDate
    ): Flow<List<GameDocument>> =
        gamesCollection
            .where {
                all(
                    arrayField contains value,
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

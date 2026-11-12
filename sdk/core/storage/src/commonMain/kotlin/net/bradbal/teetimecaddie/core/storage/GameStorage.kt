package net.bradbal.teetimecaddie.core.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.QuerySnapshot
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import net.bradbal.teetimecaddie.core.storage.documents.CourseDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameInviteeDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameRosterDocument
import net.bradbal.teetimecaddie.core.storage.documents.InviteeAnswerDocument

private const val GAMES_COLLECTION = "games"
private const val COURSES_COLLECTION = "courses"
private const val INVITEES_COLLECTION = "invitees"
private const val PLAYER_IDS_FIELD = "playerIds"
private const val INVITED_PHONES_FIELD = "invitedPhones"
private const val DATE_FIELD = "date"

/**
 * Storage for games, the people invited to them, and the courses they are played at.
 *
 * Each invitee is stored as their own document under the game. Every game read here comes back
 * with its [GameDocument.invitees] filled in from those documents, and publishing writes them.
 *
 * An interface, unlike the older storage classes, so `GamesRepository` can be tested against an
 * in-memory fake without Firebase.
 */
interface GameStorage {

    /**
     * Write a new game, creating its course if no course with [courseKey] exists yet.
     *
     * Atomic: the course read, the course write (if any), the game write and one write per invitee
     * in [GameDocument.invitees] happen in one transaction. A failed game write never leaves an
     * orphan course or invitee behind, and two organizers publishing the same new course at once
     * cannot create it twice.
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
     * Answer the invitation [phone] received to a game, in one transaction.
     *
     * Reads the game and that invitation, then writes whatever [answer] returns: the invitation as
     * answered and, for a yes, the game's new roster. A transaction is retried when either document
     * changes under it, so [answer] can run more than once and must only compute.
     *
     * @param phone Digits only: the id of the invitee's document.
     * @param answer Builds the answer from the game as read, or returns null to write nothing.
     * @return The game as written, or as read when [answer] returned null. Null if no game has [id].
     */
    suspend fun answerInvitation(
        id: String,
        phone: String,
        answer: (GameDocument) -> InviteeAnswerDocument?
    ): GameDocument?
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
            game.invitees.forEach { (phone, invitee) ->
                set(inviteesCollection(gameRef.id).document(phone), invitee)
            }
            game.also { it.id = gameRef.id }
        }
    }

    // The invitees can only be read once the game exists (their read rule checks the game), so
    // their listener starts when the game appears rather than alongside it.
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun gameFlow(id: String): Flow<GameDocument?> {
        val game = gamesCollection.document(id)
            .snapshots(includeMetadataChanges = false)
            .map { doc ->
                if (doc.exists) doc.data<GameDocument>().apply { this.id = doc.id } else null
            }

        return game.map { it != null }
            .distinctUntilChanged()
            .flatMapLatest { exists ->
                if (exists) {
                    combine(game.filterNotNull(), inviteesFlow(id)) { document, invitees ->
                        document.withInvitees(invitees)
                    }
                } else {
                    flowOf(null)
                }
            }
    }

    override fun gamesFlow(playerId: String, from: LocalDate): Flow<List<GameDocument>> =
        gamesContaining(PLAYER_IDS_FIELD, playerId, from).withInvitees()

    override fun invitationsFlow(phone: String, from: LocalDate): Flow<List<GameDocument>> =
        gamesContaining(INVITED_PHONES_FIELD, phone, from).withInvitees()

    override suspend fun answerInvitation(
        id: String,
        phone: String,
        answer: (GameDocument) -> InviteeAnswerDocument?
    ): GameDocument? {
        val gameRef = gamesCollection.document(id)
        val inviteeRef = inviteesCollection(id).document(phone)
        // A transaction can't run queries, so the other invitees are read first. Only the game and
        // the invitation being answered need the transaction's guarantees; the rest are carried
        // along so the game comes back whole.
        val others = inviteesCollection(id).get().toInvitees() - phone

        return store.runTransaction {
            val gameSnapshot = get(gameRef)
            if (!gameSnapshot.exists) return@runTransaction null
            val inviteeSnapshot = get(inviteeRef)

            val invitees = if (inviteeSnapshot.exists) {
                others + (phone to inviteeSnapshot.data<GameInviteeDocument>())
            } else {
                others
            }
            val game = gameSnapshot.data<GameDocument>()
                .copy(invitees = invitees)
                .also { it.id = gameSnapshot.id }
            val write = answer(game) ?: return@runTransaction game

            update(inviteeRef, GameInviteeDocument.serializer(), write.invitee)
            write.seat?.let { update(gameRef, GameRosterDocument.serializer(), it) }

            game.copy(
                teeTimes = write.seat?.teeTimes ?: game.teeTimes,
                playerIds = write.seat?.playerIds ?: game.playerIds,
                invitees = invitees + (phone to write.invitee)
            ).also { it.id = gameSnapshot.id }
        }
    }

    private fun inviteesCollection(gameId: String): CollectionReference =
        gamesCollection.document(gameId).collection(INVITEES_COLLECTION)

    /** Observe a game's invitees, keyed by the phone number each was invited by. */
    private fun inviteesFlow(gameId: String): Flow<Map<String, GameInviteeDocument>> =
        inviteesCollection(gameId)
            .snapshots(includeMetadataChanges = false)
            .map { it.toInvitees() }

    private fun QuerySnapshot.toInvitees(): Map<String, GameInviteeDocument> =
        documents.associate { doc -> doc.id to doc.data<GameInviteeDocument>() }

    /**
     * Fill in each game's invitees, updating whenever a game or any of its invitees change.
     *
     * The invitee listeners restart only when the set of games changes, not on every change to a
     * game, so a seat being taken doesn't re-read every invitee. That means collecting this flow
     * twice: once for the set of games and once, alongside the invitees, for the games themselves.
     * Firestore serves both from the same listener. A game whose invitees haven't loaded yet is
     * left out until they have, so no game is ever shown without its invitees.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun Flow<List<GameDocument>>.withInvitees(): Flow<List<GameDocument>> {
        val games = this
        return games
            .map { list -> list.mapNotNull { it.id }.toSet() }
            .distinctUntilChanged()
            .flatMapLatest { ids ->
                if (ids.isEmpty()) return@flatMapLatest flowOf(emptyList())

                val inviteesById = combine(ids.map { id -> inviteesFlow(id).map { id to it } }) {
                    it.toMap()
                }
                combine(games, inviteesById) { list, invitees ->
                    list.mapNotNull { game -> invitees[game.id]?.let { game.withInvitees(it) } }
                }
            }
    }

    private fun GameDocument.withInvitees(invitees: Map<String, GameInviteeDocument>): GameDocument =
        copy(invitees = invitees).also { it.id = id }

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

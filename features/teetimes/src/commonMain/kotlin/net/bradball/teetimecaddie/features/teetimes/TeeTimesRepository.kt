package net.bradball.teetimecaddie.features.teetimes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.LocalDate
import net.bradbal.teetimecaddie.core.storage.TeeTimeStorage
import net.bradbal.teetimecaddie.core.storage.documents.TeeTimeDocument
import net.bradbal.teetimecaddie.core.storage.documents.TeeTimeSlotDocument
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.models.TeeTime
import net.bradball.teetimecaddie.core.models.TeeTimeSlot
import kotlin.coroutines.cancellation.CancellationException

class TeeTimesRepository(
    private val eventManager: EventManager,
    private val teeTimeStorage: TeeTimeStorage
) {

    /**
     * Creates a new tee time with multiple time slots.
     *
     * @param createdBy The ID of the player creating the tee time.
     * @param course The name of the golf course.
     * @param date The date of the tee time.
     * @param times A list of time slots, each containing a time and number of players.
     *              Times will be stored in ascending order.
     * @return The created TeeTime with its assigned ID.
     */
    @Throws(CancellationException::class)
    suspend fun createTeeTime(
        createdBy: String,
        course: String,
        date: LocalDate,
        times: List<TeeTimeSlot>
    ): TeeTime {
        val sortedTimes = times.sortedBy { it.time }
        val doc = TeeTimeDocument(
            createdBy = createdBy,
            course = course,
            date = date,
            times = sortedTimes.map { slot ->
                TeeTimeSlotDocument(
                    time = slot.time,
                    numberOfPlayers = slot.numberOfPlayers
                )
            }
        )
        doc.id = teeTimeStorage.addTeeTime(doc)

        val totalPlayers = times.fold(0) { total, slot ->
            total + slot.numberOfPlayers
        }

        eventManager.logEvent(AnalyticsEvent.AddTeeTime(times.count(), totalPlayers))

        return doc.toModel()
    }

    fun getTeeTimes(player: String): Flow<List<TeeTime>> = teeTimeStorage
        .teeTimesFlow(player)
        .mapLatest { list ->
            list.map { it.toModel() }
        }

    /**
     * Gets a single tee time by ID.
     *
     * @param teeTimeId The ID of the tee time to retrieve.
     * @return The TeeTime if found, null otherwise.
     */
    @Throws(CancellationException::class)
    suspend fun getTeeTime(teeTimeId: String): TeeTime? {
        return teeTimeStorage.getTeeTime(teeTimeId)?.toModel()
    }

    /**
     * Updates an existing tee time.
     *
     * @param teeTime The tee time to update. Must have a non-null id.
     * @return The updated TeeTime.
     * @throws IllegalArgumentException if the tee time id is null.
     */
    @Throws(CancellationException::class)
    suspend fun updateTeeTime(teeTime: TeeTime): TeeTime {
        requireNotNull(teeTime.id) { "Cannot update a tee time without an id" }

        val sortedTimes = teeTime.times.sortedBy { it.time }
        val doc = teeTime.toDocument().apply {
            id = teeTime.id
        }

        teeTimeStorage.updateTeeTime(doc)

        val totalPlayers = sortedTimes.fold(0) { total, slot ->
            total + slot.numberOfPlayers
        }

        eventManager.logEvent(AnalyticsEvent.EditTeeTime(sortedTimes.count(), totalPlayers))

        return teeTime.copy(times = sortedTimes)
    }
}
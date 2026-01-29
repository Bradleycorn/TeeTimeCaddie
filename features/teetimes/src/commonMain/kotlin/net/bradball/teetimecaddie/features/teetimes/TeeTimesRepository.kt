package net.bradball.teetimecaddie.features.teetimes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradbal.teetimecaddie.core.storage.TeeTimeStorage
import net.bradbal.teetimecaddie.core.storage.documents.TeeTimeDocument
import net.bradbal.teetimecaddie.core.storage.documents.asModel
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.models.TeeTime
import kotlin.coroutines.cancellation.CancellationException

class TeeTimesRepository(
    private val eventManager: EventManager,
    private val teeTimeStorage: TeeTimeStorage
) {

    @Throws(CancellationException::class)
    suspend fun createTeeTime(
        createdBy: String,
        course: String,
        date: LocalDate,
        time: LocalTime,
        numberOfPlayers: Int
    ): TeeTime {
        eventManager.logEvent(AnalyticsEvent.AddTeeTime)

        val doc = TeeTimeDocument(
            createdBy = createdBy,
            course = course,
            date = date,
            time = time,
            numberOfPlayers = numberOfPlayers
        )
        doc.id = teeTimeStorage.addTeeTime(doc)
        return doc.asModel()
    }

    fun getTeeTimes(player: String): Flow<List<TeeTime>> = teeTimeStorage
        .teeTimesFlow(player)
        .mapLatest { list ->
            list.map {
                it.asModel()
            }
        }

    fun test(): Int = 1
}
package net.bradball.teetimecaddie.features.teetimes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import net.bradbal.teetimecaddie.core.storage.TeeTimeStorage
import net.bradbal.teetimecaddie.core.storage.documents.TeeTimeDocument
import net.bradbal.teetimecaddie.core.storage.documents.asModel
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.models.TeeTime

class TeeTimesRepository(
    private val eventManager: EventManager,
    private val teeTimeStorage: TeeTimeStorage
) {

    suspend fun createTeeTime(createdBy: String, course: String, dateTime: LocalDate): TeeTime {
        val doc = TeeTimeDocument(
            createdBy = createdBy,
            course = course,
            dateTime = dateTime
        )

        doc.id = teeTimeStorage.addTeeTime(doc)
        return doc.asModel()
    }

    fun getTeeTimes(player: String): Flow<List<TeeTime>> = teeTimeStorage
        .teeTimesFlow(player)
        .mapLatest { list ->
            list.map { it.asModel() }
        }
}
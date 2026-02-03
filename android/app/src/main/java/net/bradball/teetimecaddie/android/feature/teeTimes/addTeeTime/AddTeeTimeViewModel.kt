package net.bradball.teetimecaddie.android.feature.teeTimes.addTeeTime

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.models.TeeTimeSlot
import net.bradball.teetimecaddie.features.auth.AuthRepository
import net.bradball.teetimecaddie.features.teetimes.TeeTimesRepository
import javax.inject.Inject

@HiltViewModel
class AddTeeTimeViewModel @Inject constructor(
    private val teeTimesRepo: TeeTimesRepository,
    private val authRepo: AuthRepository,
    private val eventManager: EventManager
): ViewModel() {

    // UI state
    var showLoadingProgress: Boolean by mutableStateOf(false)
        private set

    var saveSuccess: Boolean by mutableStateOf(false)
        private set

    // List of tee time slots
    private val _timeSlots = mutableStateListOf<TeeTimeSlot>()
    val timeSlots: List<TeeTimeSlot> get() = _timeSlots.sortedBy { it.time }

    /**
     * Logs the analytics event when the user clicks the "Add Time" button.
     */
    fun onAddTimeClick() {
        eventManager.logEvent(AnalyticsEvent.AddTimeClick)
    }

    /**
     * Adds a new time slot with the default number of players (4).
     * If the time already exists in the list, it will not be added.
     *
     * @param time The time to add.
     * @return true if the time was added, false if it already existed.
     */
    fun addTimeSlot(time: LocalTime): Boolean {
        if (_timeSlots.any { it.time == time }) {
            return false
        }
        _timeSlots.add(TeeTimeSlot(time = time, numberOfPlayers = 4))
        eventManager.logEvent(AnalyticsEvent.AddTime)
        return true
    }

    /**
     * Updates the number of players for a specific time slot.
     *
     * @param time The time of the slot to update.
     * @param numberOfPlayers The new number of players (1-4).
     */
    fun updatePlayerCount(time: LocalTime, numberOfPlayers: Int) {
        val index = _timeSlots.indexOfFirst { it.time == time }
        if (index != -1) {
            val clampedPlayers = numberOfPlayers.coerceIn(1, 4)
            _timeSlots[index] = _timeSlots[index].copy(numberOfPlayers = clampedPlayers)
            eventManager.logEvent(AnalyticsEvent.NumberOfPlayersClick(clampedPlayers))
        }
    }

    /**
     * Removes a time slot from the list and logs the analytics event.
     *
     * @param time The time of the slot to remove.
     */
    fun removeTimeSlot(time: LocalTime) {
        eventManager.logEvent(AnalyticsEvent.RemoveTimeClick)
        _timeSlots.removeAll { it.time == time }
    }

    /**
     * Saves the tee time with all added time slots.
     *
     * @param courseName The name of the golf course.
     * @param date The date of the tee time.
     */
    fun saveTeeTime(courseName: String, date: LocalDate?) {
        if (date == null || _timeSlots.isEmpty()) return
        viewModelScope.launch {
            showLoadingProgress = true
            try {
                teeTimesRepo.createTeeTime(
                    createdBy = authRepo.currentUser.id,
                    course = courseName,
                    date = date,
                    times = _timeSlots.toList()
                )
                saveSuccess = true
            } finally {
                showLoadingProgress = false
            }
        }
    }
}

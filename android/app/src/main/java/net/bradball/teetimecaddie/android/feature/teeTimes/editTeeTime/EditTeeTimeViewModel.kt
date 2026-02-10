package net.bradball.teetimecaddie.android.feature.teeTimes.editTeeTime

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.models.TeeTime
import net.bradball.teetimecaddie.core.models.TeeTimeSlot
import net.bradball.teetimecaddie.features.teetimes.TeeTimesRepository

@AssistedFactory
interface EditTeeTimeViewModelFactory {
    fun create(teeTimeId: String): EditTeeTimeViewModel
}

@HiltViewModel(assistedFactory = EditTeeTimeViewModelFactory::class)
class EditTeeTimeViewModel @AssistedInject constructor(
    @Assisted private val teeTimeId: String,
    private val teeTimesRepo: TeeTimesRepository,
    private val eventManager: EventManager
): ViewModel() {

    // UI state
    var isLoading: Boolean by mutableStateOf(true)
        private set

    var showLoadingProgress: Boolean by mutableStateOf(false)
        private set

    var saveSuccess: Boolean by mutableStateOf(false)
        private set

    var loadError: Boolean by mutableStateOf(false)
        private set

    // Original tee time data (for change detection)
    private var originalTeeTime: TeeTime? = null

    // Editable form state
    var courseName: String by mutableStateOf("")
        private set

    var date: LocalDate? by mutableStateOf(null)
        private set

    private val _timeSlots = mutableStateListOf<TeeTimeSlot>()
    val timeSlots: List<TeeTimeSlot> get() = _timeSlots.sortedBy { it.time }

    /**
     * Whether the form has been modified from the original tee time.
     */
    val hasChanges: Boolean
        get() {
            val original = originalTeeTime ?: return false
            return courseName != original.course ||
                    date != original.date ||
                    _timeSlots.sortedBy { it.time } != original.times.sortedBy { it.time }
        }

    init {
        loadTeeTime()
    }

    private fun loadTeeTime() {
        viewModelScope.launch {
            isLoading = true
            loadError = false
            try {
                val teeTime = teeTimesRepo.getTeeTime(teeTimeId)
                if (teeTime != null) {
                    originalTeeTime = teeTime
                    courseName = teeTime.course
                    date = teeTime.date
                    _timeSlots.clear()
                    _timeSlots.addAll(teeTime.times)
                } else {
                    loadError = true
                }
            } catch (e: Exception) {
                loadError = true
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Updates the course name.
     */
    fun updateCourseName(name: String) {
        courseName = name
    }

    /**
     * Updates the date.
     */
    fun updateDate(newDate: LocalDate?) {
        date = newDate
    }

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
     * Saves the tee time with all changes.
     */
    fun saveTeeTime() {
        val currentDate = date
        val original = originalTeeTime
        if (currentDate == null || _timeSlots.isEmpty() || original == null) return

        viewModelScope.launch {
            showLoadingProgress = true
            try {
                val updatedTeeTime = TeeTime(
                    id = teeTimeId,
                    createdBy = original.createdBy,
                    course = courseName,
                    date = currentDate,
                    times = _timeSlots.toList()
                )
                teeTimesRepo.updateTeeTime(updatedTeeTime)
                saveSuccess = true
            } finally {
                showLoadingProgress = false
            }
        }
    }
}

package net.bradball.teetimecaddie.android.feature.teeTimes.addTeeTime

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.ScreenType
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

    fun saveTeeTime(courseName: String, date: LocalDate?, time: LocalTime?, players: Int) {
        if (date == null || time == null) return
        viewModelScope.launch {
            showLoadingProgress = true
            try {
                teeTimesRepo.createTeeTime(
                    createdBy = authRepo.currentUser.id,
                    course = courseName,
                    date = date,
                    time = time,
                    numberOfPlayers = players
                )
                saveSuccess = true
            } finally {
                showLoadingProgress = false
            }
        }
    }
}

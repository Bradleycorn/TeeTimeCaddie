package net.bradball.teetimecaddie.android.feature.teeTimes.teeTimeEntry

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import net.bradball.teetimecaddie.features.auth.AuthRepository
import net.bradball.teetimecaddie.features.teetimes.TeeTimesRepository
import javax.inject.Inject

@HiltViewModel
class TeeTimeEntryViewModel @Inject constructor(
    private val teeTimesRepository: TeeTimesRepository,
    private val authRepository: AuthRepository
): ViewModel() {
    var errorMessage: Int? by mutableStateOf(null)
        private set

    var showLoadingProgress: Boolean by mutableStateOf(false)
        private set

    var teeTimeAdded: Boolean by mutableStateOf(false)
        private set

    fun createTeeTime(course: String, date: LocalDate) {
        viewModelScope.launch {
            try {
                errorMessage = null
                showLoadingProgress = true

                val user = authRepository.currentUser.id

                teeTimesRepository.createTeeTime(user, course, date)
                teeTimeAdded = true
            } catch (ex: Exception) {
                Log.e("ADD_TEE_TIME","Error Adding Tee Time.", ex)
            } finally {
                showLoadingProgress = false
            }
        }
    }

    fun reset() { teeTimeAdded = false }
}
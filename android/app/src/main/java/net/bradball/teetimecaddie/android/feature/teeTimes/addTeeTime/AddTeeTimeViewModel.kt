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


}
